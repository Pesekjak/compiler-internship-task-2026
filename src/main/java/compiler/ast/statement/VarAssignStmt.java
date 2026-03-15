package compiler.ast.statement;

import compiler.ast.Expression;
import compiler.ast.Statement;

public record VarAssignStmt(String name, Expression value) implements Statement {
}
