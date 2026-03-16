package org.example.compiler

import compiler.MiniKotlinBaseVisitor
import compiler.MiniKotlinParser
import compiler.ast.AstBuilder
import compiler.CpsGenerator
import compiler.ast.ProgramNode

class MiniKotlinCompiler : MiniKotlinBaseVisitor<String>() {

    fun compile(program: MiniKotlinParser.ProgramContext, className: String = "MiniProgram"): String {
        val astBuilder = AstBuilder()
        val ast = astBuilder.visitProgram(program) as ProgramNode
        val generator = CpsGenerator()
        val generatedMethods = generator.generate(ast)
        val code = """
            public class $className {
            
                public static void main(String[] args) {
                  main((end) -> {});
                  return;
                }

                $generatedMethods

            }
        """.trimIndent()
        return code
    }

}
