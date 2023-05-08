import tech.zzhdev.phunctions.parser.IdentifierToken
import tech.zzhdev.phunctions.parser.OperatorTokens
import tech.zzhdev.phunctions.parser.Parser
import tech.zzhdev.phunctions.parser.Token
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TestBoolean {
    @Test
    fun testParsingTrueAndFalse() {
        val source = """
            (do
                (+ :True :False)
                (+ True False)
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
            OperatorTokens.DO,
                OperatorTokens.LEFT_PARENT,
                    OperatorTokens.PLUS,
                    IdentifierToken("True"),
                    IdentifierToken("False"),
                OperatorTokens.RIGHT_PARENT,
                OperatorTokens.LEFT_PARENT,
                    OperatorTokens.PLUS,
                    IdentifierToken("True"),
                    IdentifierToken("False"),
                OperatorTokens.RIGHT_PARENT,
            OperatorTokens.RIGHT_PARENT
        ), tokens.toArray())
    }

    @Test
    fun testEvaluatingTrueAndFalse() {
        val source = """
            (+
                (+ :True :False)
                (+ True False)
            )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val expression = parser.parse().getOrElse {
            return
        }
        val result = expression.eval()
        assert(result.isSuccess)
        assertEquals(2, result.getOrNull()!!)
    }

    @Test
    fun testEvaluatingComparison() {
        val source = """
            (=
                (> 1 0)
                (< 0 1)
            )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val expression = parser.parse().getOrElse {
            return
        }
        val result = expression.eval()
        assert(result.isSuccess)
        assertEquals(1, result.getOrNull()!!)
    }
}