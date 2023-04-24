package tech.zzhdev.phunctions

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
    while (parser.hasNext()) {
        println(parser.nextToken())
    }
}