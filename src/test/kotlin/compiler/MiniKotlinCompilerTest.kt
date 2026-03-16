package org.example.compiler

import compiler.MiniKotlinLexer
import compiler.MiniKotlinParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MiniKotlinCompilerTest {

    @TempDir
    lateinit var tempDir: Path

    private fun parseString(source: String): MiniKotlinParser.ProgramContext {
        val input = CharStreams.fromString(source)
        val lexer = MiniKotlinLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = MiniKotlinParser(tokens)
        return parser.program()
    }

    private fun parseFile(path: Path): MiniKotlinParser.ProgramContext {
        val input = CharStreams.fromPath(path)
        val lexer = MiniKotlinLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = MiniKotlinParser(tokens)
        return parser.program()
    }

    private fun resolveStdlibPath(): Path? {
        val devPath = Paths.get("build", "stdlib")
        if (devPath.toFile().exists()) {
            val stdlibJar = devPath.toFile().listFiles()
                ?.firstOrNull { it.name.startsWith("stdlib") && it.name.endsWith(".jar") }
            if (stdlibJar != null) return stdlibJar.toPath()
        }
        return null
    }

    private fun compileAndRun(path: String) : String {
        val examplePath = Paths.get(path)
        val program = parseFile(examplePath)
        val compiler = MiniKotlinCompiler()
        val javaCode = compiler.compile(program)
        val javaFile = tempDir.resolve("MiniProgram.java")
        Files.writeString(javaFile, javaCode)
        val javaCompiler = JavaRuntimeCompiler()
        val stdlibPath = resolveStdlibPath()
        val (compilationResult, executionResult) = javaCompiler.compileAndExecute(javaFile, stdlibPath)
        assertIs<CompilationResult.Success>(compilationResult)
        assertIs<ExecutionResult.Success>(executionResult)
        return executionResult.stdout
    }

    @Test
    fun `compile example_mini outputs 120 and 15`() {
        val output = compileAndRun("samples/example.mini")
        assertTrue(output.contains("120"), "Expected output to contain factorial result 120, but got: $output")
        assertTrue(output.contains("15"), "Expected output to contain arithmetic result 15, but got: $output")
    }

    @Test
    fun `compile while outputs 8`() {
        val output = compileAndRun("samples/while.mini")
        assertTrue(output.contains("8"), "Expected output to contain result 8, but got: $output")
    }

    @Test
    fun `compile operators outputs correct values`() {
        val output = compileAndRun("samples/operators.mini")
        val lines = output.trim().lines().map { it.trim() }.filter { it.isNotEmpty() }
        val expectedLines = listOf("40", "true", "true", "true", "true", "true")
        assertEquals(lines.size, 6, "Expected 6 lines of output, but got ${lines.size}. Output: $output")
        for (i in expectedLines.indices) {
            assertEquals(
                lines[i],
                expectedLines[i],
                "Line ${i+1} mismatch! Expected '${expectedLines[i]}' but got '${lines[i]}'"
            )
        }
    }

    @Test
    fun `compile literals outputs correct literal values`() {
        val output = compileAndRun("samples/literals.mini")

        val lines = output.trim().lines().map { it.trim() }.filter { it.isNotEmpty() }
        val expectedLines = listOf("42", "Hello MiniKotlin", "true", "false")

        assertEquals(lines.size, 4, "Expected exactly 4 lines of output, but got ${lines.size}. Output: $output")

        for (i in expectedLines.indices) {
            assertEquals(expectedLines[i], lines[i], "Mismatch at line ${i+1}")
        }
    }

}
