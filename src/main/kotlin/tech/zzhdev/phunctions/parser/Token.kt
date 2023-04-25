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

    private val operators = HashMap<String, BasicToken>()
    init {
        operators["("] = LEFT_PARENT
        operators[")"] = RIGHT_PARENT
        operators["+"] = PLUS
        operators["-"] = MINUS
        operators["*"] = MULTIPLY
        operators["/"] = DIVIDE
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