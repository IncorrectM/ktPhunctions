package tech.zzhdev.phunctions

import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.TerminalBuilder
import tech.zzhdev.phunctions.expression.Expression
import tech.zzhdev.phunctions.expression.SymbolExpression
import tech.zzhdev.phunctions.parser.Parser
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.contains("repl")) {
        repl()
    } else {
//        val source = """
//        (*
//            (+ 1 1)
//            (* 2 2)
//            4
//            10
//        )
//    """.trimIndent()
        val source = """
            ( do
                ( def
                    :kto
                    100
                )
                ( *
                    ( + 1 1)
                    4
                    :kto
                )
             )
        """.trimIndent()

        val parser = Parser(source)
        val expression = parser.parse()
        println(expression)
//        println(expression.getOrNull()?.eval())
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