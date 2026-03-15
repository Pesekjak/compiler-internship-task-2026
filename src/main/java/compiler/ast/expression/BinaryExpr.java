package compiler.ast.expression;

import compiler.ast.Expression;

public record BinaryExpr(Expression left, String operator, Expression right) implements Expression {
}
