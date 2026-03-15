package compiler.ast.expression;

import compiler.ast.Expression;

public record BoolLiteral(boolean value) implements Expression {
}
