package compiler.ast;

import compiler.MiniKotlinParser;

public record ParameterNode(String name, String type) implements AstNode {

    public ParameterNode(MiniKotlinParser.ParameterContext ctx) {
        this(ctx.IDENTIFIER().getText(), ctx.type().getText());
    }

}
