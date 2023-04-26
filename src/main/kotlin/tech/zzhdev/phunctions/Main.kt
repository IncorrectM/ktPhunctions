package tech.zzhdev.phunctions

import tech.zzhdev.phunctions.expression.Expression
import tech.zzhdev.phunctions.expression.SymbolExpression
import tech.zzhdev.phunctions.parser.Parser
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.contains("repl")) {
        val scanner = Scanner(System.`in`)
        print(">")
        var value = scanner.nextLine()
        while (value.trim() != "q") {
            val p = Parser(value)
            val expr: Expression = p.parse().getOrElse {
                println(it)
                exitProcess(-1)
            }
            println(expr.eval().getOrElse {
                println(it)
                exitProcess(-1)
            })
            print(">")
            value = scanner.next()
        }
    } else {
        val source = """
        (*
            (+ 1 1)
            (* 2 2)
            4
            10
        )
    """.trimIndent()

        val parser = Parser(source)
        val expression = parser.parse()
        println(expression.getOrNull())
        println(expression.getOrNull()?.eval())
    }
}