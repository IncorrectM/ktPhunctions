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
            return if (identifier.startsWith(":") || GlobalEnvironment.isBuiltinVar(identifier)) {
                val id = IdentifierToken(identifier.removePrefix(":"))
                lastToken = id
                Result.success(id)
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
        // token can be an identifier if user try to call function
        if (oprToken is IdentifierToken) {
            symbolExpression.appendChild(parseFunctionCallExpression().getOrElse {
                return Result.failure(it)
            })
            return Result.success(symbolExpression)
        }

        if ((oprToken == null) || (oprToken !is BasicToken) || !OperatorTokens.isOperator(oprToken.symbol)) {
            return Result.failure(SyntaxErrorException("expect an operator"))
        }

        when (oprToken) {
            OperatorTokens.DEF -> {
                symbolExpression.appendChild(parseDefinitionExpression().getOrElse {
                    return Result.failure(it)
                })
                // closing is checked inside parseVariableDefineExpression
                return Result.success(symbolExpression)
            }

            OperatorTokens.ARGS -> {
                symbolExpression.appendChild(parseArgsDefinitionExpression().getOrElse {
                    return Result.failure(it)
                })
                return Result.success(symbolExpression)
            }

            OperatorTokens.IF -> {
                symbolExpression.appendChild(parseIfExpression().getOrElse {
                    return Result.failure(it)
                })
                return Result.success(symbolExpression)
            }
        }

        symbolExpression.appendChild(BinaryOperatorExpression(oprToken.symbol))

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

    private fun parseDefinitionExpression(): Result<Expression> {
        val idToken = nextToken().getOrElse {
            return Result.failure(it)
        }

        if (idToken !is IdentifierToken) {
            return Result.failure(SyntaxErrorException("expecting an identifier name"))
        }

        val expression = VariableDefinitionExpression()
        expression.children.add(IdentifierExpression(idToken.identifier))

        val valueToken = nextToken().getOrElse {
            return Result.failure(it)
        }

        when (valueToken) {
            OperatorTokens.LEFT_PARENT -> {
                val innerExpression = parseSymbolExpression().getOrElse {
                    return Result.failure(it)
                }
                when (innerExpression.children[0]) {
                    is ArgsDefinitionExpression -> {
                        // declaring function
                        val args = innerExpression.children[0] as ArgsDefinitionExpression
                        // the definition of the function
                        val leftParent = nextToken().getOrElse {
                            return Result.failure(it)
                        }
                        if (leftParent != OperatorTokens.LEFT_PARENT) {
                            return Result.failure(SyntaxErrorException("expecting symbol expression"))
                        }
                        val symbolExpression = parseSymbolExpression().getOrElse {
                            return  Result.failure(it)
                        }

                        val endingToken = nextToken().getOrElse {
                            return Result.failure(it)
                        }
                        if (endingToken != OperatorTokens.RIGHT_PARENT) {
                            return Result.failure(SyntaxErrorException("expression is not closed"))
                        }

                        return Result.success(FunctionDefinitionExpression(
                            IdentifierExpression(idToken.identifier),
                            args,
                            symbolExpression
                        ))

                    }

                    else -> {
                        // declaring variable
                        expression.children.add(innerExpression)
                        // go on
                    }
                }
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

    private fun parseArgsDefinitionExpression(): Result<ArgsDefinitionExpression> {
        val args = ArgsDefinitionExpression()

        var curToken = nextToken().getOrElse {
            return Result.failure(it)
        }
        while (curToken != OperatorTokens.RIGHT_PARENT) {
            if (curToken !is IdentifierToken) {
                return Result.failure(SyntaxErrorException("expecting identifier"))
            }

            args.addArg(
                IdentifierExpression(curToken.identifier)
            )

            curToken = nextToken().getOrElse {
                return Result.failure(it)
            }
        }

        return Result.success(args)
    }

    private fun parseFunctionCallExpression(): Result<FunctionCallExpression> {
        val id = lastToken
        if (id == null || id !is IdentifierToken) {
            return Result.failure(SyntaxErrorException("expected an identifier to call function"))
        }

        val args = arrayListOf<Expression>()
        var currentToken = nextToken().getOrElse {
            return Result.failure(it)
        }
        while (hasNext() && currentToken != OperatorTokens.RIGHT_PARENT) {
            when(currentToken) {
                OperatorTokens.LEFT_PARENT -> {
                    args.add(parseSymbolExpression().getOrElse {
                        return Result.failure(it)
                    })
                }

                is IntToken -> {
                    args.add(ConstantIntExpression(currentToken.value))
                }

                else -> {
                    return Result.failure(SyntaxErrorException("expecting symbol expression or constant integer"))
                }
            }

            currentToken = nextToken().getOrElse {
                return Result.failure(it)
            }
        }

        return Result.success(FunctionCallExpression(
            id.identifier,
            args = args,
        ))
    }

    private fun parseIfExpression(): Result<IfExpression> {
        val conditionalExpression = parseValueExpression().getOrElse {
            return Result.failure(it)
        }
        val trueBranch = parseValueExpression().getOrElse {
            return Result.failure(it)
        }

        val falseBranch = parseValueExpression().getOrElse {
            return Result.failure(it)
        }
        if (falseBranch !is SymbolExpression) { // remove remaining ')'
            nextToken()
        }

        return Result.success(IfExpression(conditionalExpression, trueBranch, falseBranch))
    }

    private fun parseValueExpression(): Result<Expression> {
        val token = nextToken().getOrElse {
            return Result.failure(it)
        }
        val expression: Expression = when(token) {
            OperatorTokens.LEFT_PARENT -> parseSymbolExpression().getOrElse {
                return Result.failure(it)
            }
            is IntToken -> ConstantIntExpression(token.value)
            is IdentifierToken -> IdentifierExpression(token.identifier)
            else -> {
                println(token)
                return Result.failure(SyntaxErrorException("expecting symbol expression or constant integer"))
            }
        }
        return Result.success(expression)
    }
}