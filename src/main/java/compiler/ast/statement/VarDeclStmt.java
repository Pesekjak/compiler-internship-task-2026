package compiler.ast.statement;

import compiler.MiniKotlinParser;
import compiler.ast.Expression;
import compiler.ast.Statement;

public record VarDeclStmt(String name, String type, Expression initializer) implements Statement {

    public VarDeclStmt(MiniKotlinParser.VariableDeclarationContext ctx, Expression expression) {
        this(ctx.IDENTIFIER().getText(), ctx.type().getText(), expression);
    }

}
