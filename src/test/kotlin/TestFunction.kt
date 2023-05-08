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
                    BinaryOperatorExpression("+"),
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

    @Test
    fun testFunctionCallingManually() {
        val env = GlobalEnvironment

        // (+ (+ :a :a :a) :a)
        // -> (* 4 :a)
        val innerFunction = FunctionExpression(
            Environment(),
            args = ArgsDefinitionExpression(
                args = arrayListOf(IdentifierExpression("a"))
            ),
            expression = SymbolExpression(arrayOf(
                BinaryOperatorExpression("+"),
                SymbolExpression(arrayOf(
                    BinaryOperatorExpression("+"),
                    IdentifierExpression("a"),
                    IdentifierExpression("a"),
                    IdentifierExpression("a"),
                )),
                IdentifierExpression("a"),
            )),
        )
        env.putVar("aa", innerFunction)

        // (+ (:aa 10) :a)
        // -> (+ (* 4 10) :a)
        // -> (+ 40 :a)
        val anotherInnerFunction = FunctionExpression(
            Environment(),
            args = ArgsDefinitionExpression(
                args = arrayListOf(IdentifierExpression("a"))
            ),
            expression = SymbolExpression(arrayOf(
                BinaryOperatorExpression("+"),
                FunctionCallExpression("aa", arrayListOf(ConstantIntExpression(10))),
                IdentifierExpression("a"),
            )),
        )
        env.putVar("b", anotherInnerFunction)

        // (+ (:aa 100) (:b 555) (:aa 25) :a)
        // -> (+ 400 (+ 40 555) 100 :a)
        // -> (+ 400 595 100 :a)
        // -> (+ 1095 :a)
        val function = FunctionExpression(
            Environment(),
            args = ArgsDefinitionExpression(
                args = arrayListOf(IdentifierExpression("a"))
            ),
            expression = SymbolExpression(arrayOf(
                BinaryOperatorExpression("+"),
                FunctionCallExpression("aa", arrayListOf(ConstantIntExpression(100))),
                FunctionCallExpression("b", arrayListOf(ConstantIntExpression(555))),
                FunctionCallExpression("aa", arrayListOf(ConstantIntExpression(25))),
                IdentifierExpression("a"),
            )),
        )
        function.environment.pushGeneralExpression(ConstantIntExpression(10000))
        // -> (+ 1095 :a)
        // -> (+ 1095 10000)
        // -> 11095
        assertEquals(11095, function.eval().getOrNull())
    }

    @Test
    fun testBasicFunctionCall() {
        val source = """
            (do
                (def
                    :addTwo
                    (args :a :b)
                    (+ :a :b)
                )
                (+ (:addTwo 1 (:addTwo 1 (:addTwo 10 (:addTwo 100 (:addTwo 999 1))))) (:addTwo 1 1))
            )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val expression = parser.parse()
        assert(expression.isSuccess)

        val exprResult = expression.getOrNull()!!
        assertEquals(1114, exprResult.eval().getOrElse {
            assert(false)
        })
    }
}