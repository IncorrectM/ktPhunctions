package tech.zzhdev.phunctions.parser

import tech.zzhdev.phunctions.exception.EndOfSourceException
import tech.zzhdev.phunctions.exception.NoSuchOperatorException
import tech.zzhdev.phunctions.exception.SyntaxErrorException
import tech.zzhdev.phunctions.expression.ConstantIntExpression
import tech.zzhdev.phunctions.expression.Expression
import tech.zzhdev.phunctions.expression.OperatorExpression
import tech.zzhdev.phunctions.expression.SymbolExpression

class Parser(private val source: String) {
    private var pos: Int = 0
    private val nextChar
        get() = if (pos < source.length) source[pos] else null

    private fun skipWhitespaces() {
        while (pos < source.length && source[pos].isWhitespace()) {
            pos++
        }
    }

    fun hasNext(): Boolean = pos < source.length

    private fun march() {
        pos++
    }

    fun nextToken(): Result<Token> {
        skipWhitespaces()

        if (pos >= source.length) {
            return Result.failure(EndOfSourceException())
        }

        val firstChar = source[pos]
        if (firstChar.isDigit()) {
            var num = 0
            // while nextChar is not null and nextChar is digit
            while (nextChar?.isDigit() == true) {
                num = num * 10 + source[pos].digitToInt()
                march()
            }
            return Result.success(IntToken(num))
        }

        // Single Char Operators have higher priority
        // Single Char Operators will not contain letters
        if (OperatorTokens.isSingleCharOperator(firstChar)) {
            val opr = OperatorTokens.getOperatorToken("" + firstChar)
            if (opr != null) {
                march()
                return Result.success(opr)
            }
        }

        val builder = StringBuilder()
        // while nextChar is not null nor whitespace
        while (nextChar?.isWhitespace() == false) {
            builder.append(nextChar)
            march()
        }

        val opr = OperatorTokens.getOperatorToken(builder.toString())
        return if (opr == null) {
            // currently we do not support identifier
            Result.failure(NoSuchOperatorException(pos, builder.toString()))
        } else {
            Result.success(opr)
        }
    }

    fun getTokens(): Result<Array<Token>> {
        val list = ArrayList<Token>()
        while (hasNext()) {
            val tokenResult = nextToken()
            if (tokenResult.isFailure) {
                return Result.failure(tokenResult.exceptionOrNull()!!)
            }
            val token = tokenResult.getOrNull()!!
            list.add(token)
        }
        return Result.success(list.toTypedArray())
    }

     fun parse(): Result<Expression> {
         val tokenResult = nextToken()
         if (tokenResult.isFailure) {
             return Result.failure(tokenResult.exceptionOrNull()!!)
         }

         // symbol expression starts with a '('
         val firstToken = tokenResult.getOrNull()
         if (firstToken == null || firstToken != OperatorTokens.LEFT_PARENT) {
             return Result.failure(SyntaxErrorException("symbol expressions should start with '('"))
         }

         val symbolExpressionResult = parseSymbolExpression()
         return if (symbolExpressionResult.isFailure) {
             Result.failure(symbolExpressionResult.exceptionOrNull()!!)
         } else {
             Result.success(symbolExpressionResult.getOrNull()!!)
         }
     }

    fun parseSymbolExpression(): Result<SymbolExpression> {
        val symbolExpression = SymbolExpression()

        var tokenResult = nextToken()
        if (tokenResult.isFailure) {
            return Result.failure(tokenResult.exceptionOrNull()!!)
        }

        // second token should be an operator
        val secondToken = tokenResult.getOrNull()
        if ((secondToken == null) || (secondToken !is BasicToken) || !OperatorTokens.isOperator(secondToken.symbol)) {
            return Result.failure(SyntaxErrorException("expect an operator"))
        }
        symbolExpression.appendChild(OperatorExpression(secondToken.symbol))

        // parse remaining children
        tokenResult = nextToken()
        while (tokenResult.isSuccess && tokenResult.getOrNull()!! != OperatorTokens.RIGHT_PARENT && hasNext()) {
            val token = tokenResult.getOrNull()!!
            when (token) {
                is IntToken -> {
                    symbolExpression.appendChild(ConstantIntExpression(token.value))
                }

                OperatorTokens.LEFT_PARENT -> {
                    val result = parseSymbolExpression()
                    if (result.isFailure) {
                        return  Result.failure(result.exceptionOrNull()!!)
                    }
                    symbolExpression.appendChild(result.getOrNull()!!)
                }

                else -> {
                    return Result.failure(SyntaxErrorException("expecting an expression"))
                }
            }
            tokenResult = nextToken()
        }

        return Result.success(symbolExpression)
    }
}