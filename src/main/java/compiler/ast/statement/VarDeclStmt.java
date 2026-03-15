package compiler.ast.statement;

import compiler.ast.Expression;
import compiler.ast.Statement;

public record VarDeclStmt(String name, String type, Expression initializer) implements Statement {
}
