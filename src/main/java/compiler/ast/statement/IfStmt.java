package compiler.ast.statement;

import compiler.ast.Expression;
import compiler.ast.Statement;

import java.util.List;

public record IfStmt(Expression condition, List<Statement> thenBlock, List<Statement> elseBlock) implements Statement {

    public IfStmt {
        thenBlock = List.copyOf(thenBlock);
        elseBlock = List.copyOf(elseBlock);
    }

}
