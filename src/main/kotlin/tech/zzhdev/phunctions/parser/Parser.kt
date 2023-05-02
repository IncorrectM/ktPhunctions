package tech.zzhdev.phunctions.parser

import tech.zzhdev.phunctions.exception.EndOfSourceException
import tech.zzhdev.phunctions.exception.NoSuchOperatorException
import tech.zzhdev.phunctions.exception.SyntaxErrorException
import tech.zzhdev.phunctions.expression.*

class Parser(private val source: String) {
    private var pos: Int = 0
    private val nextChar
        get() = if (pos < source.length) source[pos] else null

    private var lastToken: Token? = null

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
            lastToken = IntToken(num)
            return Result.success(IntToken(num))
        }

        // Single Char Operators have higher priority
        // Single Char Operators will not contain letters
        if (OperatorTokens.isSingleCharOperator(firstChar)) {
            val opr = OperatorTokens.getOperatorToken("" + firstChar)
            if (opr != null) {
                march()
                lastToken = opr
                return Result.success(opr)
            }
        }

        val builder = StringBuilder()
        // while nextChar is not null nor whitespace
        while (nextChar?.isWhitespace() == false && nextChar != ')') {
            builder.append(nextChar)
            march()
        }

        val opr = OperatorTokens.getOperatorToken(builder.toString())
        return if (opr == null) {
            val identifier = builder.toString()
            // identifiers starts with :
            return if (identifier.startsWith(":")) {
                Result.success(IdentifierToken(identifier.removePrefix(":")))
            } else {
                Result.failure(NoSuchOperatorException(pos, builder.toString()))
            }
        } else {
            lastToken = opr
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
         } else if (lastToken != OperatorTokens.RIGHT_PARENT) {
             Result.failure(SyntaxErrorException("expression is not closed"))
         }else {
             Result.success(symbolExpressionResult.getOrNull()!!)
         }
     }

    private fun parseSymbolExpression(): Result<SymbolExpression> {
        val symbolExpression = SymbolExpression()

        var tokenResult = nextToken()
        if (tokenResult.isFailure) {
            return Result.failure(tokenResult.exceptionOrNull()!!)
        }

        // second token should be an operator
        val oprToken = tokenResult.getOrNull()
        if ((oprToken == null) || (oprToken !is BasicToken) || !OperatorTokens.isOperator(oprToken.symbol)) {
            return Result.failure(SyntaxErrorException("expect an operator"))
        }

        if (oprToken == OperatorTokens.DEF) {
            symbolExpression.appendChild(parseVariableDefineExpression().getOrElse {
                return Result.failure(it)
            })
            // closing is checked inside parseVariableDefineExpression
            return Result.success(symbolExpression)
        }

        symbolExpression.appendChild(OperatorExpression(oprToken.symbol))

        // parse remaining children
        tokenResult = nextToken()
        while (tokenResult.isSuccess && tokenResult.getOrNull()!! != OperatorTokens.RIGHT_PARENT && hasNext()) {
            when (val token = tokenResult.getOrNull()!!) {
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

                is IdentifierToken -> {
                    symbolExpression.appendChild(IdentifierExpression(token.identifier))
                }

                else -> {
                    return Result.failure(SyntaxErrorException("expecting an expression, got $token"))
                }
            }
            tokenResult = nextToken()
        }

        if (lastToken != OperatorTokens.RIGHT_PARENT) {
            return Result.failure(SyntaxErrorException("expression is not closed"))
        }
        return Result.success(symbolExpression)
    }

    private fun parseVariableDefineExpression(): Result<VariableDefineExpression> {
        val idToken = nextToken().getOrElse {
            return Result.failure(it)
        }

        if (idToken !is IdentifierToken) {
            return Result.failure(SyntaxErrorException("expecting an identifier name"))
        }

        val expression = VariableDefineExpression()
        expression.children.add(IdentifierExpression(idToken.identifier))

        val valueToken = nextToken().getOrElse {
            return Result.failure(it)
        }

        when (valueToken) {
            OperatorTokens.LEFT_PARENT -> {
                expression.children.add(parseSymbolExpression().getOrElse {
                    return Result.failure(it)
                })
            }
            is IntToken -> {
                expression.children.add(ConstantIntExpression(valueToken.value))
            }
            else -> {
                return Result.failure(SyntaxErrorException("expecting symbol expression or constant int"))
            }
        }

        var endingToken: Token = nextToken().getOrElse {
            return Result.failure(it)
        }
        when (endingToken) {
            OperatorTokens.RIGHT_PARENT -> {
                return Result.success(expression)
            }

            OperatorTokens.EXCLAMATION -> {
                expression.evalNow = true
            }
        }

        endingToken = nextToken().getOrElse {
            return Result.failure(it)
        }
        if (endingToken != OperatorTokens.RIGHT_PARENT) {
            return Result.failure(SyntaxErrorException("expression is not closed"))
        }

        return Result.success(expression)
    }
}