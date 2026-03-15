package compiler.ast.statement;

import compiler.ast.Expression;
import compiler.ast.Statement;

import java.util.List;

public record WhileStmt(Expression condition, List<Statement> body) implements Statement {

    public WhileStmt {
        body = List.copyOf(body);
    }

}
