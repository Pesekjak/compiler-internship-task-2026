package compiler.ast.statement;

import compiler.ast.Expression;
import compiler.ast.Statement;
import org.jetbrains.annotations.Nullable;

public record ReturnStmt(@Nullable Expression value) implements Statement {
}
