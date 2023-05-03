package tech.zzhdev.phunctions

import org.jline.reader.EndOfFileException
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.TerminalBuilder
import tech.zzhdev.phunctions.expression.*
import tech.zzhdev.phunctions.parser.Parser

fun main(args: Array<String>) {
    if (args.map { it.lowercase() }.contains("repl")) {
        repl()
    } else {
//        val source = """
//            (def
//                :addTwo
//                (args :a :b)
//                (+ :a :b)
//            )
//        """.trimIndent()
//
//        val parser = Parser(source)
//        // val tokens = parser.getTokens().getOrElse {
//        //    println(it)
//        //    return
//        // }
//        // tokens.forEach {
//        //     println(it)
//        // }
//        val expression = parser.parse()
//        println(expression.getOrNull()?.eval())
//        val env = GlobalEnvironment
//        println(GlobalEnvironment)
//        // println(expression.getOrNull()?.eval())
        val innerFunction = FunctionExpression(
            Environment(),
            args = ArgsDefinitionExpression(
                args = arrayListOf(IdentifierExpression("a"))
            ),
            expression = SymbolExpression(arrayOf(
                OperatorExpression("+"),
                SymbolExpression(arrayOf(
                    OperatorExpression("+"),
                    IdentifierExpression("a"),
                    IdentifierExpression("a"),
                    IdentifierExpression("a"),
                )),
                IdentifierExpression("a"),
            )),
        )
        innerFunction.environment.putVar("a", ConstantIntExpression(0))
        innerFunction.environment.pushGeneralExpression(ConstantIntExpression(10))
        println(innerFunction.eval())
    }
}

fun repl() {
    val terminal = TerminalBuilder.builder()
        .system(true)
        .build()
    val lineReader = LineReaderBuilder.builder()
        .terminal(terminal)
        .build()

    val prompt = "phunction > "
    while (true) {
        try {
            val line = lineReader.readLine(prompt)
            val parser = Parser(line)
            val expression = parser.parse().getOrElse {
                throw it
            }
            println("\n>>> " + expression.eval().getOrElse {
                throw it
            })
        } catch (uie: UserInterruptException) {
            // user quit
        } catch (eof: EndOfFileException) {
            println("\nBye~")
            return
        } catch (ex: Exception) {
            println("<stdin>: $ex")
        }
    }
}