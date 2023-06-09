import org.junit.jupiter.api.Test
import tech.zzhdev.phunctions.exception.EvaluationErrorException
import tech.zzhdev.phunctions.expression.*
import tech.zzhdev.phunctions.parser.*
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
    fun testParsingWithIdentifier() {
        val source = """
            (*
                (+ 1 1)
                4
                :kto
                :banana
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
            IntToken(4),
            IdentifierToken("kto"),
            IdentifierToken("banana"),
            OperatorTokens.RIGHT_PARENT,
        ), tokens.toArray())
    }

    @Test
    fun testParsingExclamation() {
        val source = """
            (def
                :a
                1
                !
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
            OperatorTokens.DEF,
            IdentifierToken("a"),
            IntToken(1),
            OperatorTokens.EXCLAMATION,
            OperatorTokens.RIGHT_PARENT,
        ), tokens.toArray())
    }

    @Test
    fun testParsingDoAndDef() {
        val source = """
            ( do
                ( def
                    :kto
                    100
                )
                ( *
                    ( + 1 1)
                    4
                    :kto
                )
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
                    OperatorTokens.DEF,
                    IdentifierToken("kto"),
                    IntToken(100),
                OperatorTokens.RIGHT_PARENT,
                OperatorTokens.LEFT_PARENT,
                    OperatorTokens.MULTIPLY,
                    OperatorTokens.LEFT_PARENT,
                        OperatorTokens.PLUS,
                        IntToken(1),
                        IntToken(1),
                    OperatorTokens.RIGHT_PARENT,
                    IntToken(4),
                    IdentifierToken("kto"),
                OperatorTokens.RIGHT_PARENT,
            OperatorTokens.RIGHT_PARENT,
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
            BinaryOperatorExpression("*"),
            SymbolExpression(arrayOf(
                BinaryOperatorExpression("+"),
                ConstantIntExpression(1),
                ConstantIntExpression(1),
            )),
            SymbolExpression(arrayOf(
                BinaryOperatorExpression("*"),
                ConstantIntExpression(2),
                ConstantIntExpression(2),
            )),
            ConstantIntExpression(4),
        ))
        assertEquals(expected, expression)
    }

    @Test
    fun testDoAndDefExpression() {
        val source = """
            ( do
                ( def
                    :kto
                    100
                )
                ( *
                    ( + 1 1)
                    4
                    :kto
                )
             )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val expressionResult = parser.parse()
        assert(expressionResult.isSuccess)

        val expression = expressionResult.getOrNull()!!
        val expected = SymbolExpression(arrayOf(
            BinaryOperatorExpression("do"),
            SymbolExpression(arrayOf(
                VariableDefinitionExpression(arrayListOf(
                    IdentifierExpression("kto"),
                    ConstantIntExpression(100),
                )),
            )),
            SymbolExpression(arrayOf(
                BinaryOperatorExpression("*"),
                SymbolExpression(arrayOf(
                    BinaryOperatorExpression("+"),
                    ConstantIntExpression(1),
                    ConstantIntExpression(1),
                )),
                ConstantIntExpression(4),
                IdentifierExpression("kto"),
            ))
        ))
        assertEquals(expected, expression)
    }

    @Test
    fun testExpressionEvaluation() {
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
        assert(expression.eval().isSuccess)
        assertEquals(32, expression.eval().getOrNull()!!)
    }

    @Test
    fun testDoAnDefEvaluation() {
        val source = """
            ( do
                ( def
                    :kto
                    200
                )
                ( *
                    ( + 1 1)
                    4
                    :kto
                )
             )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val expressionResult = parser.parse()
        assert(expressionResult.isSuccess)

        val expression = expressionResult.getOrNull()!!
        assert(expression.eval().isSuccess)
        assertEquals(1600, expression.eval().getOrNull()!!)
    }

    @Test
    fun testLazyEvaluation() {
        val source = """
            (do
                (def 
                    :a 
                    (* 2 2 :b)
                )
                (def 
                    :b 
                    10
                )
                (* :a :b)
            )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val expressionResult = parser.parse()
        assert(expressionResult.isSuccess)

        val expression = expressionResult.getOrNull()!!
        assert(expression.eval().isSuccess)
        assertEquals(400, expression.eval().getOrNull()!!)
    }

    @Test
    fun testInstantEvaluation() {
        val source = """
            (def 
                :a 
                (* 2 2)
                !
            )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val expressionResult = parser.parse()
        assert(expressionResult.isSuccess)

        val expression = expressionResult.getOrNull()!!
        assert(expression.eval().isSuccess)
        assertEquals(4, expression.eval().getOrNull()!!)
    }

    @Test
    fun testInstantEvaluationFailed() {
        val source = """
            (def 
                :a 
                (* 2 :b)
                !
            )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val expressionResult = parser.parse()
        assert(expressionResult.isSuccess)

        val expression = expressionResult.getOrNull()!!
        assert(expression.eval().isFailure)
        assertEquals(EvaluationErrorException("b is not defined"), expression.eval().exceptionOrNull()!!)
    }

    @Test
    fun testDisplayEvaluation() {
        val source = """
            (do
                (display 1 1 1 1 1) 
                (def :b 2)
                (display :b :b :b :b :b (* 2 :b)) 
            )
        """.trimIndent()

        val parser = Parser(source)
        assert(parser.hasNext())

        val expressionResult = parser.parse()
        assert(expressionResult.isSuccess)

        val expression = expressionResult.getOrNull()!!
        val evaluationResult = expression.eval()
        assert(evaluationResult.isSuccess)
        assertEquals(4, evaluationResult.getOrNull()!!)
    }
}