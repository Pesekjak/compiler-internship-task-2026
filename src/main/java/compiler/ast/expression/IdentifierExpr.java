package compiler.ast.expression;

import compiler.ast.Expression;

public record IdentifierExpr(String name) implements Expression {
}
