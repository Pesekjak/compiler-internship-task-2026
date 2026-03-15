package compiler.ast;

import java.util.List;

public record FunctionNode(String name,
                           List<ParameterNode> parameters,
                           String returnType,
                           List<Statement> body) implements AstNode {

    public FunctionNode {
        parameters = List.copyOf(parameters);
        body = List.copyOf(body);
    }

}
