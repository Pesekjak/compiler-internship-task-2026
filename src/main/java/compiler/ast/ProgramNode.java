package compiler.ast;

import java.util.List;

public record ProgramNode(List<FunctionNode> functions) implements AstNode {

    public ProgramNode {
        functions = List.copyOf(functions);
    }

}
