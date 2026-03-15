package compiler.ast.expression;

import compiler.ast.Expression;

import java.util.List;

public record FunctionCallExpr(String functionName, List<Expression> arguments) implements Expression {

    public FunctionCallExpr {
        arguments = List.copyOf(arguments);
    }

}
