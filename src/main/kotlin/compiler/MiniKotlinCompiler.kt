package org.example.compiler

import compiler.MiniKotlinBaseVisitor
import compiler.MiniKotlinParser

class MiniKotlinCompiler : MiniKotlinBaseVisitor<String>() {

    fun compile(program: MiniKotlinParser.ProgramContext, className: String = "MiniProgram"): String {
        return "";
    }

}
