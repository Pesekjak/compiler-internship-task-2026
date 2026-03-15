package compiler.ast;

import compiler.MiniKotlinBaseVisitor;
import compiler.MiniKotlinParser;
import compiler.ast.expression.*;
import compiler.ast.statement.*;

import java.util.ArrayList;
import java.util.List;

public class AstBuilder extends MiniKotlinBaseVisitor<AstNode> {

    @Override
    public ProgramNode visitProgram(MiniKotlinParser.ProgramContext ctx) {
        List<FunctionNode> functions = ctx.functionDeclaration().stream()
                .map(this::visitFunctionDeclaration)
                .toList();
        return new ProgramNode(functions);
    }

    @Override
    public FunctionNode visitFunctionDeclaration(MiniKotlinParser.FunctionDeclarationContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        String returnType = ctx.type().getText();

        List<ParameterNode> params = new ArrayList<>();
        if (ctx.parameterList() != null)
            params = ctx.parameterList().parameter().stream()
                    .map(ParameterNode::new)
                    .toList();

        List<Statement> body = ctx.block().statement().stream()
                .map(this::visit)
                .map(Statement.class::cast)
                .toList();

        return new FunctionNode(name, params, returnType, body);
    }

    @Override
    public Statement visitStatement(MiniKotlinParser.StatementContext ctx) {
        AstNode node = super.visitStatement(ctx);
        if (node instanceof Expression expr)
            return new ExprStmt(expr);
        return (Statement) node;
    }

    @Override
    public VarDeclStmt visitVariableDeclaration(MiniKotlinParser.VariableDeclarationContext ctx) {
        return new VarDeclStmt(ctx, (Expression) visit(ctx.expression()));
    }

    @Override
    public VarAssignStmt visitVariableAssignment(MiniKotlinParser.VariableAssignmentContext ctx) {
        return new VarAssignStmt(ctx.IDENTIFIER().getText(), (Expression) visit(ctx.expression()));
    }

    @Override
    public IfStmt visitIfStatement(MiniKotlinParser.IfStatementContext ctx) {
        Expression condition = (Expression) visit(ctx.expression());

        List<Statement> thenBlock = ctx.block(0).statement().stream()
                .map(this::visit)
                .map(Statement.class::cast)
                .toList();

        List<Statement> elseBlock = new ArrayList<>();
        if (ctx.block().size() > 1) {
            elseBlock = ctx.block(1).statement().stream()
                    .map(this::visit)
                    .map(Statement.class::cast)
                    .toList();
        }

        return new IfStmt(condition, thenBlock, elseBlock);
    }

    @Override
    public WhileStmt visitWhileStatement(MiniKotlinParser.WhileStatementContext ctx) {
        Expression condition = (Expression) visit(ctx.expression());
        List<Statement> body = ctx.block().statement().stream()
                .map(this::visit)
                .map(Statement.class::cast)
                .toList();
        return new WhileStmt(condition, body);
    }

    @Override
    public ReturnStmt visitReturnStatement(MiniKotlinParser.ReturnStatementContext ctx) {
        Expression expr = ctx.expression() != null ? (Expression) visit(ctx.expression()) : null;
        return new ReturnStmt(expr);
    }

    @Override
    public FunctionCallExpr visitFunctionCallExpr(MiniKotlinParser.FunctionCallExprContext ctx) {
        String funcName = ctx.IDENTIFIER().getText();
        List<Expression> args = new ArrayList<>();
        if (ctx.argumentList() != null)
            args = ctx.argumentList().expression().stream()
                    .map(this::visit)
                    .map(Expression.class::cast)
                    .toList();
        return new FunctionCallExpr(funcName, args);
    }

    @Override
    public BinaryExpr visitMulDivExpr(MiniKotlinParser.MulDivExprContext ctx) {
        return buildBinaryExpr(ctx.expression(0), ctx.expression(1), ctx.getChild(1).getText());
    }

    @Override
    public BinaryExpr visitAddSubExpr(MiniKotlinParser.AddSubExprContext ctx) {
        return buildBinaryExpr(ctx.expression(0), ctx.expression(1), ctx.getChild(1).getText());
    }

    @Override
    public BinaryExpr visitComparisonExpr(MiniKotlinParser.ComparisonExprContext ctx) {
        return buildBinaryExpr(ctx.expression(0), ctx.expression(1), ctx.getChild(1).getText());
    }

    @Override
    public BinaryExpr visitEqualityExpr(MiniKotlinParser.EqualityExprContext ctx) {
        return buildBinaryExpr(ctx.expression(0), ctx.expression(1), ctx.getChild(1).getText());
    }

    @Override
    public BinaryExpr visitAndExpr(MiniKotlinParser.AndExprContext ctx) {
        return buildBinaryExpr(ctx.expression(0), ctx.expression(1), "&&");
    }

    @Override
    public BinaryExpr visitOrExpr(MiniKotlinParser.OrExprContext ctx) {
        return buildBinaryExpr(ctx.expression(0), ctx.expression(1), "||");
    }

    private BinaryExpr buildBinaryExpr(MiniKotlinParser.ExpressionContext leftCtx,
                                       MiniKotlinParser.ExpressionContext rightCtx,
                                       String op) {
        return new BinaryExpr((Expression) visit(leftCtx), op, (Expression) visit(rightCtx));
    }

    @Override
    public UnaryExpr visitNotExpr(MiniKotlinParser.NotExprContext ctx) {
        return new UnaryExpr("!", (Expression) visit(ctx.expression()));
    }

    @Override
    public Expression visitPrimaryExpr(MiniKotlinParser.PrimaryExprContext ctx) {
        return (Expression) visit(ctx.primary());
    }

    @Override
    public Expression visitParenExpr(MiniKotlinParser.ParenExprContext ctx) {
        return (Expression) visit(ctx.expression());
    }

    @Override
    public IntLiteral visitIntLiteral(MiniKotlinParser.IntLiteralContext ctx) {
        return new IntLiteral(Integer.parseInt(ctx.getText()));
    }

    @Override
    public StringLiteral visitStringLiteral(MiniKotlinParser.StringLiteralContext ctx) {
        return new StringLiteral(ctx.getText());
    }

    @Override
    public BoolLiteral visitBoolLiteral(MiniKotlinParser.BoolLiteralContext ctx) {
        return new BoolLiteral(Boolean.parseBoolean(ctx.getText()));
    }

    @Override
    public IdentifierExpr visitIdentifierExpr(MiniKotlinParser.IdentifierExprContext ctx) {
        return new IdentifierExpr(ctx.getText());
    }

}
