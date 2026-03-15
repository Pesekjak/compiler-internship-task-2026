package compiler.ast.statement;

import compiler.ast.Expression;
import compiler.ast.Statement;

public record ExprStmt(Expression expression) implements Statement {
}
