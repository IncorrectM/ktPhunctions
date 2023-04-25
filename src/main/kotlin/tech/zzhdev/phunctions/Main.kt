package tech.zzhdev.phunctions

import tech.zzhdev.phunctions.expression.SymbolExpression
import tech.zzhdev.phunctions.parser.Parser

fun main(args: Array<String>) {
    val source = """
        (*
            (+ 1 1)
            (* 2 2)
            4
        )
    """.trimIndent()

    val parser = Parser(source)
    val expression = parser.parse()
    println(expression.getOrNull())
}