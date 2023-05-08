package tech.zzhdev.phunctions.parser

interface Token { }

data class BasicToken(val symbol: String): Token { }
object OperatorTokens {
    val LEFT_PARENT = BasicToken("(")
    val RIGHT_PARENT = BasicToken(")")

    val PLUS = BasicToken("+")
    val MINUS = BasicToken("-")
    val MULTIPLY = BasicToken("*")
    val DIVIDE = BasicToken("/")

    val EQUAL = BasicToken("=")
    val GREATER = BasicToken(">")
    val LESS = BasicToken("<")

    val EXCLAMATION = BasicToken("!")

    val ARGS = BasicToken("args")
    val DEF = BasicToken("def")
    val DO = BasicToken("do")
    val IF = BasicToken("if")

    private val operators = HashMap<String, BasicToken>()
    init {
        operators["("] = LEFT_PARENT
        operators[")"] = RIGHT_PARENT

        operators["+"] = PLUS
        operators["-"] = MINUS
        operators["*"] = MULTIPLY
        operators["/"] = DIVIDE

        operators["="] = EQUAL
        operators[">"] = GREATER
        operators["<"] = LESS

        operators["!"] = EXCLAMATION

        operators["args"] = ARGS
        operators["def"] = DEF
        operators["do"] = DO
        operators["if"] = IF
    }
    private val registeredOperators
        get() = operators.values

    /// Single Char Operators will not contain letters
    fun isSingleCharOperator(symbol: Char): Boolean = isOperator("" + symbol)

    fun isOperator(symbol: String): Boolean = operators.keys.contains(symbol)

    fun getOperatorToken(symbol: String): BasicToken? = operators.get(symbol)
}
data class IdentifierToken(val identifier: String): Token { }
data class IntToken(val value: Int): Token { }