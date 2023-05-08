import tech.zzhdev.phunctions.expression.*
import tech.zzhdev.phunctions.parser.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TestConditional {
    @Test
    fun testParsingIfTokens() {
        val source = """
            (if
                True
                (if 
                    True 
                    1 
                    (if True 0 12))
                (+ 2 0)
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
            OperatorTokens.IF,
            IdentifierToken("True"),
            OperatorTokens.LEFT_PARENT,
            OperatorTokens.IF,
            IdentifierToken("True"),
            IntToken(1),
            OperatorTokens.LEFT_PARENT,
            OperatorTokens.IF,
            IdentifierToken("True"),
            IntToken(0),
            IntToken(12),
            OperatorTokens.RIGHT_PARENT,
            OperatorTokens.RIGHT_PARENT,
            OperatorTokens.LEFT_PARENT,
            OperatorTokens.PLUS,
            IntToken(2),
            IntToken(0),
            OperatorTokens.RIGHT_PARENT,
            OperatorTokens.RIGHT_PARENT
        ), tokens.toArray())
    }

    @Test
    fun testParsingIfExpression() {
        val source = """
            (if
                True
                (if True 1 0)
                (+ 2 0)
            )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val expression = parser.parse()
        if (expression.isFailure) {
            println(expression.exceptionOrNull())
        }
        assert(expression.isSuccess)

        assertEquals(
            SymbolExpression(arrayOf(
            IfExpression(
                condition = IdentifierExpression("True"),
                trueBranch = SymbolExpression(arrayOf(
                    IfExpression(
                        condition = IdentifierExpression("True"),
                        trueBranch = ConstantIntExpression(1),
                        falseBranch = ConstantIntExpression(0),
                    )
                )),
                falseBranch = SymbolExpression(arrayOf(
                    OperatorExpression("+"),
                    ConstantIntExpression(2),
                    ConstantIntExpression(0)
                )),
            )
        )), expression.getOrNull()!!)
    }

    @Test
    fun testEvaluatingIfExpression() {
        val source = """
            (do
                (def
                    :a
                    1
                )
                (if
                    True
                    (if False 2 :a)
                    (+ 10082 0)
                )
            )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val expression = parser.parse()
        if (expression.isFailure) {
            println(expression.exceptionOrNull())
        }
        assert(expression.isSuccess)

        val result = expression.getOrNull()!!.eval()
        if (result.isFailure) {
            println(result.exceptionOrNull())
        }
        assert(result.isSuccess)

        assertEquals(1, result.getOrNull()!!)
    }

    @Test
    fun testFibonacci() {
        /**
         * The ultimate goal of this branch.
         * UNDER DEVELOPMENT
         * */
        val source = """
            (do
                (def
                    :fib
                    (args :n)
                    (if
                        (or (= :n 0) (= :n 1))
                        1
                        (+ (:fib (- 1 :n)) (:fib (- 2 :n)) )
                    )
                )
                (:fib 20)
            )
        """.trimIndent()
    }
}