package tech.zzhdev.phunctions.expression

import tech.zzhdev.phunctions.parser.Token
import kotlin.math.max

interface Expression { }

data class OperatorExpression(val symbol: String): Expression {}
data class ConstantIntExpression(val value: Int): Expression {}

class SymbolExpression(): Expression {
    val children = ArrayList<Expression>()

    constructor(
        children: Array<Expression>
    ): this() {
        this.children.addAll(children)
    }

    private fun toStringWithIndent(indent: Int = 1, indentChar: Char = ' '): String {
        val builder = StringBuilder()
        builder.append("SymbolExpression: \n")

        for (child in children) {
            repeat(indent + 2) {
                builder.append(indentChar)
            }
            if (child is SymbolExpression) {
                builder.append(child.toStringWithIndent(indent + 2, indentChar))
            } else {
                builder.append(child)
                builder.append("\n")
            }
        }

        return builder.toString()
    }

    override fun toString(): String {
        return toStringWithIndent()
    }

    fun appendChild(expr: Expression) {
        children.add(expr)
    }
}