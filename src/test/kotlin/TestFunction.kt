import tech.zzhdev.phunctions.expression.*
import tech.zzhdev.phunctions.parser.IdentifierToken
import tech.zzhdev.phunctions.parser.OperatorTokens
import tech.zzhdev.phunctions.parser.Parser
import tech.zzhdev.phunctions.parser.Token
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TestFunction {
    @Test
    fun testParsingArgsToTokens() {
        val source = """
            (args :a :b)
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
            OperatorTokens.ARGS,
            IdentifierToken("a"),
            IdentifierToken("b"),
            OperatorTokens.RIGHT_PARENT
        ), tokens.toArray())
    }

    @Test
    fun testParsingArgsDefinitionExpression() {
        val source = """
            (args :a :b)
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val expression = parser.parse()
        assert(expression.isSuccess)

        assertEquals(SymbolExpression(arrayOf(
            ArgsDefinitionExpression(args = arrayListOf(
                IdentifierExpression("a"),
                IdentifierExpression("b")
            ))
        )), expression.getOrNull()!!)
    }

    @Test
    fun testParsingFunctionDefinitionExpression() {
        val source = """
            (def
                :addTwo
                (args :a :b)
                (+ :a :b)
            )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val expression = parser.parse()
        assert(expression.isSuccess)

        assertEquals(SymbolExpression(arrayOf(
            FunctionDefinitionExpression(
                id = IdentifierExpression("addTwo"),
                args = ArgsDefinitionExpression(arrayListOf(
                        IdentifierExpression("a"),
                        IdentifierExpression("b")
                )),
                expression = SymbolExpression(arrayOf(
                    OperatorExpression("+"),
                    IdentifierExpression("a"),
                    IdentifierExpression("b")
                )),
            ),
        )), expression.getOrNull()!!)
    }

    @Test
    fun testEvaluatingFunctionDefinitionExpression() {
        val source = """
            (def
                :addTwo
                (args :a :b)
                (+ :a :b)
            )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val expression = parser.parse()
        assert(expression.isSuccess)

        val exprResult = expression.getOrNull()!!
        assertEquals(0, exprResult.eval().getOrElse {
            assert(false)
        })
    }
}