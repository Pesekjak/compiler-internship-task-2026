package compiler.ast.expression;

import compiler.ast.Expression;

public record StringLiteral(String value) implements Expression {
}
