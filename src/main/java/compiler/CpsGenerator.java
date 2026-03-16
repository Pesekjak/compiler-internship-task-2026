package compiler;

import compiler.ast.*;
import compiler.ast.statement.*;
import compiler.ast.expression.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generator of continuation-passing style Java code from a {@link ProgramNode}.
 */
public class CpsGenerator {

    // TODO some util for simpler variable manipulation as now
    //  some need to be accessed via array, so they are effectively
    //  final in lambdas (or rather their array wrappers are)

    private int argCounter = 0;
    private final Set<String> localVars = new HashSet<>();

    /**
     * Generates a valid continuation-passing style Java code from
     * a program node of a mini kotlin program.
     *
     * @param program program to translate into Java
     * @return Java code
     */
    public String generate(ProgramNode program) {
        StringBuilder sb = new StringBuilder();
        for (FunctionNode func : program.functions()) {
            sb.append(generateFunction(func)).append("\n\n");
        }
        return sb.toString();
    }

    private String generateFunction(FunctionNode func) {
        localVars.clear();

        StringBuilder sb = new StringBuilder();
        sb.append("public static void ").append(func.name()).append("(");

        for (ParameterNode param : func.parameters()) {
            sb.append(TypeUtil.asJavaType(param.type())).append(" ").append(param.name()).append(", ");
        }

        // TODO the param name should be constant
        sb.append("Continuation<").append(TypeUtil.asJavaType(func.returnType())).append("> __continuation) {\n");
        sb.append(generateStatements(func.body()));
        sb.append("}\n");

        return sb.toString();
    }

    private String generateStatements(VarDeclStmt varDecl, List<Statement> rest) {
        localVars.add(varDecl.name());
        String type = TypeUtil.asJavaType(varDecl.type());

        return evaluateCps(varDecl.initializer(), (finalExpr) ->
                type + "[] " + varDecl.name() + " = new " + type + "[]{"
                        + generateExpression(finalExpr) + "};\n" + generateStatements(rest));
    }

    private String generateStatements(VarAssignStmt assign, List<Statement> rest) {
        return evaluateCps(assign.value(), (finalExpr) ->
                assign.name() + "[0] = " + generateExpression(finalExpr) + ";\n"
                        + generateStatements(rest));
    }

    private String generateStatements(ReturnStmt ret) {
        if (ret.value() != null) {
            return evaluateCps(ret.value(), (finalExpr) ->
                    "__continuation.accept(" + generateExpression(finalExpr) + ");\nreturn;\n");
        } else {
            return "__continuation.accept(null);\nreturn;\n";
        }
    }

    private String generateStatements(ExprStmt exprStmt, List<Statement> rest) {
        return evaluateCps(exprStmt.expression(), (finalExpr) -> {
            // end of while expression
            // TODO possibly improve this
            if (finalExpr instanceof IdentifierExpr id
                    && id.name().endsWith(".accept(null)")
                    && rest.isEmpty() /* this "jump" statement is always last in the body */) {
                return id.name() + ";\n";
            }

            return generateStatements(rest);
        });
    }

    private String generateStatements(IfStmt ifStmt, List<Statement> rest) {
        List<Statement> thenBlock = new ArrayList<>(ifStmt.thenBlock());
        thenBlock.addAll(rest);

        List<Statement> elseBlock = new ArrayList<>(ifStmt.elseBlock());
        elseBlock.addAll(rest);

        return evaluateCps(ifStmt.condition(), (finalExpr) ->
                "if (" + generateExpression(finalExpr) + ") {\n"
                        + generateStatements(thenBlock) + "}\nelse {\n"
                        + generateStatements(elseBlock) + "}\n");
    }

    private String generateStatements(WhileStmt whileStmt, List<Statement> rest) {
        String loopId = "loop" + (argCounter++);
        String loopRef = loopId + "[0]";

        // prepare the loop body with recursive call to itself
        List<Statement> bodyWithContinue = new ArrayList<>(whileStmt.body());
        // at the end call itself recursively
        // TODO possibly improve this, use special expr statement for the jump?
        bodyWithContinue.add(new ExprStmt(new IdentifierExpr(loopRef + ".accept(null)")));

        StringBuilder sb = new StringBuilder();
        sb.append("Continuation[] ").append(loopId).append(" = new Continuation[1];\n");
        String dummyParam = emptyArg();
        sb.append(loopRef).append(" = (").append(dummyParam).append(") -> {\n");

        String innerIf = evaluateCps(whileStmt.condition(), (finalExpr) ->
                "if (" + generateExpression(finalExpr) + ") {\n"
                        + generateStatements(bodyWithContinue)
                        + "}\nelse {\n"
                        + generateStatements(rest)
                        + "}\n");

        sb.append(innerIf);
        sb.append("};\n");
        sb.append(loopRef).append(".accept(null);\n");

        return sb.toString();
    }

    private String generateStatements(List<Statement> statements) {
        if (statements.isEmpty()) return "";

        Statement current = statements.get(0);
        List<Statement> rest = statements.subList(1, statements.size());

        if (current instanceof VarDeclStmt varDecl) return generateStatements(varDecl, rest);
        if (current instanceof VarAssignStmt assign) return generateStatements(assign, rest);
        if (current instanceof ReturnStmt ret) return generateStatements(ret);
        if (current instanceof ExprStmt exprStmt) return generateStatements(exprStmt, rest);
        if (current instanceof IfStmt ifStmt) return generateStatements(ifStmt, rest);
        if (current instanceof WhileStmt whileStmt) return generateStatements(whileStmt, rest);
        throw new IllegalStateException("Unknown statement: " + current.getClass().getSimpleName());
    }

    private String generateExpression(IntLiteral il) {
        return String.valueOf(il.value());
    }

    private String generateExpression(StringLiteral sl) {
        // NOTE: this is already quoted
        return sl.value();
    }

    private String generateExpression(BoolLiteral bl) {
        return String.valueOf(bl.value());
    }

    private String generateExpression(IdentifierExpr id) {
        return localVars.contains(id.name()) ? id.name() + "[0]" : id.name();
    }

    private String generateExpression(BinaryExpr bin) {
        String leftCode = generateExpression(bin.left());
        String rightCode = generateExpression(bin.right());
        // TODO Objects is technically not in stdlib
        if (bin.operator().equals("==")) {
            return "java.util.Objects.equals(" + leftCode + ", " + rightCode + ")";
        } else if (bin.operator().equals("!=")) {
            return "!java.util.Objects.equals(" + leftCode + ", " + rightCode + ")";
        } else {
            return "(" + leftCode + " " + bin.operator() + " " + rightCode + ")";
        }
    }

    private String generateExpression(UnaryExpr un) {
        return "(" + un.operator() + generateExpression(un.expression()) + ")";
    }

    private String generateExpression(FunctionCallExpr call) {
        // This should never happen, if it does,
        // some function call was not properly handled.
        // All expressions we generate needs to be cleaned from
        // function calls using #evaluateCps.
        throw new IllegalStateException("Function call " + call.functionName() + "() slipped through extraction");
    }

    private String generateExpression(Expression expr) {
        if (expr instanceof IntLiteral il) return generateExpression(il);
        if (expr instanceof StringLiteral sl) return generateExpression(sl);
        if (expr instanceof BoolLiteral bl) return generateExpression(bl);
        if (expr instanceof IdentifierExpr id) return generateExpression(id);
        if (expr instanceof BinaryExpr bin) return generateExpression(bin);
        if (expr instanceof UnaryExpr un) return generateExpression(un);
        if (expr instanceof FunctionCallExpr call) return generateExpression(call);
        throw new IllegalStateException("Unknown expression: " + expr.getClass().getSimpleName());
    }

    /**
     * Recursively extracts all function calls from expression and wraps them in lambdas.
     * Once the expression is free of function calls it is passed to {@code onComplete}.
     *
     * @param expr the expression to process
     * @param onComplete a callback that generates the final code using the clean expression
     * @return generated Java code
     */
    private String evaluateCps(Expression expr, Function<Expression, String> onComplete) {
        String argName = emptyArg();
        ExtractionResult ext = extractCall(expr, argName);

        if (ext != null) {
            // function call found. extract it, wrap it in generateCall, and process the rest
            return generateCall(ext.call(), argName, evaluateCps(ext.replacedExpr(), onComplete));
        } else {
            // no function calls left. generate the final synchronous statement
            return onComplete.apply(expr);
        }
    }

    /**
     * Represents an extracted function call from an expression.
     * <p>
     * The result of the call is stored in a separate, temporary variable
     * used to evaluate the initial expression.
     *
     * @param call function call to execute
     * @param replacedExpr original expression but with the function call replaced by the
     *                     temporary variable
     */
    private record ExtractionResult(FunctionCallExpr call, Expression replacedExpr) {
    }

    private ExtractionResult extractCall(FunctionCallExpr call, String argName) {
        List<Expression> args = call.arguments();
        for (int i = 0; i < args.size(); i++) {
            ExtractionResult argExt = extractCall(args.get(i), argName);
            if (argExt != null) {
                List<Expression> newArgs = new ArrayList<>(args);
                newArgs.set(i, argExt.replacedExpr());
                FunctionCallExpr newCall = new FunctionCallExpr(call.functionName(), newArgs);
                return new ExtractionResult(argExt.call(), newCall);
            }
        }
        return new ExtractionResult(call, new IdentifierExpr(argName));
    }

    private ExtractionResult extractCall(BinaryExpr bin, String argName) {
        ExtractionResult leftExt = extractCall(bin.left(), argName);
        if (leftExt != null) {
            return new ExtractionResult(leftExt.call(),
                    new BinaryExpr(leftExt.replacedExpr(), bin.operator(), bin.right()));
        }

        ExtractionResult rightExt = extractCall(bin.right(), argName);
        if (rightExt != null) {
            return new ExtractionResult(rightExt.call(),
                    new BinaryExpr(bin.left(), bin.operator(), rightExt.replacedExpr()));
        }
        return null;
    }

    private ExtractionResult extractCall(UnaryExpr un, String argName) {
        ExtractionResult ext = extractCall(un.expression(), argName);
        if (ext != null) {
            return new ExtractionResult(ext.call(),
                    new UnaryExpr(un.operator(), ext.replacedExpr()));
        }
        return null;
    }

    private ExtractionResult extractCall(Expression expr, String argName) {
        if (expr instanceof FunctionCallExpr call) return extractCall(call, argName);
        if (expr instanceof BinaryExpr bin) return extractCall(bin, argName);
        if (expr instanceof UnaryExpr un) return extractCall(un, argName);
        return null;
    }

    /**
     * Generates Java code for a function call in CPS style.
     * It appends arguments and adds trailing lambda for the continuation.
     * <p>
     * e.g. translates {@code foo(1)} into {@code foo(1, (arg0) -> { innerCode });}
     *
     * @param call the function call expression
     * @param argName the name of the parameter in the continuation lambda
     * @param innerCode code inside the lambda
     * @return Java code for the call
     */
    private String generateCall(FunctionCallExpr call, String argName, String innerCode) {
        String argsCode = call.arguments().stream()
                .map(this::generateExpression)
                .collect(Collectors.joining(", "));

        String prefix = argsCode.isEmpty() ? "" : argsCode + ", ";

        // TODO this shouldn't be hardcoded but low priority
        String funcName = call.functionName().equals("println") ? "Prelude.println" : call.functionName();

        return funcName + "(" + prefix + "(" + argName + ") -> {\n" + innerCode + "});\n";
    }

    private String emptyArg() {
        return "_arg" + (argCounter++);
    }

}
