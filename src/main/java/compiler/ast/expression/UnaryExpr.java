package compiler.ast.expression;

import compiler.ast.Expression;

public record UnaryExpr(String operator, Expression expression) implements Expression {
}
