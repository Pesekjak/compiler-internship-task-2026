package compiler.ast.expression;

import compiler.ast.Expression;

public record IntLiteral(int value) implements Expression {
}
