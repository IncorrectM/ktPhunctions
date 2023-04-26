import org.junit.jupiter.api.Test
import tech.zzhdev.phunctions.expression.ConstantIntExpression
import tech.zzhdev.phunctions.expression.OperatorExpression
import tech.zzhdev.phunctions.expression.SymbolExpression
import tech.zzhdev.phunctions.parser.IntToken
import tech.zzhdev.phunctions.parser.OperatorTokens
import tech.zzhdev.phunctions.parser.Parser
import tech.zzhdev.phunctions.parser.Token
import kotlin.test.AfterTest
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TestBasicParser {
    @Test
    fun testStringToTokens() {
        val source = """
            (*
                (+ 1 1)
                (* 2 2)
                4
            )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val tokens = ArrayList<Token>()
        while (parser.hasNext()) {
            val result = parser.nextToken()
            assert(result.isSuccess)
            tokens.add(result.getOrNull()!!)
        }

        assertContentEquals(arrayOf(
            OperatorTokens.LEFT_PARENT,
            OperatorTokens.MULTIPLY,
            OperatorTokens.LEFT_PARENT,
            OperatorTokens.PLUS,
            IntToken(1),
            IntToken(1),
            OperatorTokens.RIGHT_PARENT,
            OperatorTokens.LEFT_PARENT,
            OperatorTokens.MULTIPLY,
            IntToken(2),
            IntToken(2),
            OperatorTokens.RIGHT_PARENT,
            IntToken(4),
            OperatorTokens.RIGHT_PARENT
        ), tokens.toArray())
    }

    @Test
    fun testTokensToExpression() {
        val source = """
            (*
                (+ 1 1)
                (* 2 2)
                4
            )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val expressionResult = parser.parse()
        assert(expressionResult.isSuccess)

        val expression = expressionResult.getOrNull()!!
        val expected = SymbolExpression(arrayOf(
            OperatorExpression("*"),
            SymbolExpression(arrayOf(
                OperatorExpression("+"),
                ConstantIntExpression(1),
                ConstantIntExpression(1),
            )),
            SymbolExpression(arrayOf(
                OperatorExpression("*"),
                ConstantIntExpression(2),
                ConstantIntExpression(2),
            )),
            ConstantIntExpression(4),
        ))
        assertEquals(expected.toString(), expression.toString())
    }
}