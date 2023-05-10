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
                    BinaryOperatorExpression("+"),
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
    fun testFibonacciTotTokens() {
        val source = """
            (do
                (def
                    :fib
                    (args :n)
                    (if
                        (< :n 2)
                        1
                        (+ (:fib (- :n 1)) (:fib (- :n 2)))
                    )
                )
                (:fib 9)
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

        val expected = arrayOf(
            OperatorTokens.LEFT_PARENT,
            OperatorTokens.DO,
            OperatorTokens.LEFT_PARENT,
            OperatorTokens.DEF,
            IdentifierToken("fib"),
            OperatorTokens.LEFT_PARENT,
            OperatorTokens.ARGS,
            IdentifierToken("n"),
            OperatorTokens.RIGHT_PARENT,
            OperatorTokens.LEFT_PARENT,
            OperatorTokens.IF,
            OperatorTokens.LEFT_PARENT,
            OperatorTokens.LESS,
            IdentifierToken("n"),
            IntToken(2),
            OperatorTokens.RIGHT_PARENT,
            IntToken(1),
            OperatorTokens.LEFT_PARENT,
            OperatorTokens.PLUS,
            OperatorTokens.LEFT_PARENT,
            IdentifierToken("fib"),
            OperatorTokens.LEFT_PARENT,
            OperatorTokens.MINUS,
            IdentifierToken("n"),
            IntToken(1),
            OperatorTokens.RIGHT_PARENT,
            OperatorTokens.RIGHT_PARENT,
            OperatorTokens.LEFT_PARENT,
            IdentifierToken("fib"),
            OperatorTokens.LEFT_PARENT,
            OperatorTokens.MINUS,
            IdentifierToken("n"),
            IntToken(2),
            OperatorTokens.RIGHT_PARENT,
            OperatorTokens.RIGHT_PARENT,
            OperatorTokens.RIGHT_PARENT,
            OperatorTokens.RIGHT_PARENT,
            OperatorTokens.RIGHT_PARENT,
            OperatorTokens.LEFT_PARENT,
            IdentifierToken("fib"),
            IntToken(9),
            OperatorTokens.RIGHT_PARENT,
            OperatorTokens.RIGHT_PARENT
        )

        assertEquals(expected.size, tokens.size)
        for (i in 0 until tokens.size) {
            assertEquals(expected[i], tokens[i])
        }
    }

    @Test
    fun testFibonacciToExpression() {
        val source = """
            (do
                (def
                    :fib
                    (args :n)
                    (if
                        (< :n 2)
                        1
                        (+ 
                            (:fib (- :n 1)) 
                            (:fib (- :n 2))
                        )
                    )
                )
                (:fib 9)
            )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())
        parser.parse().onSuccess {
            assertEquals(SymbolExpression(arrayOf(
                BinaryOperatorExpression("do"),
                SymbolExpression(arrayOf(
                    FunctionDefinitionExpression(
                        id = IdentifierExpression("fib"),
                        args = ArgsDefinitionExpression(arrayListOf(IdentifierExpression("n"))),
                        expression = SymbolExpression(arrayOf(
                            IfExpression(
                                condition = SymbolExpression(arrayOf(
                                    BinaryOperatorExpression("<"),
                                    IdentifierExpression("n"),
                                    ConstantIntExpression(2),
                                )),
                                trueBranch = ConstantIntExpression(1),
                                falseBranch = SymbolExpression(arrayOf(
                                    BinaryOperatorExpression("+"),
                                    SymbolExpression(arrayOf(
                                        FunctionCallExpression(id = "fib", args = arrayListOf(SymbolExpression(arrayOf(
                                            BinaryOperatorExpression("-"),
                                            IdentifierExpression("n"),
                                            ConstantIntExpression(1)
                                        )))),
                                    )),
                                    SymbolExpression(arrayOf(
                                        FunctionCallExpression(id = "fib", args = arrayListOf(SymbolExpression(arrayOf(
                                            BinaryOperatorExpression("-"),
                                            IdentifierExpression("n"),
                                            ConstantIntExpression(2)
                                        )))),
                                    )),
                                ))
                            )
                        ))
                    )
                )),
                SymbolExpression(arrayOf(
                    FunctionCallExpression(
                        id = "fib",
                        args = arrayListOf(ConstantIntExpression(9))
                    )
                )),
            )), it)
        }.onFailure {
            assert(false) { it }
        }
    }

    @Test
    fun testFibonacciEvaluation() {
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
                        (< :n 2)
                        1
                        (+ (:fib (- :n 1)) (:fib (- :n 2)))
                    )
                )
                (:fib 9)
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

        assertEquals(55, result.getOrNull()!!)
    }
}