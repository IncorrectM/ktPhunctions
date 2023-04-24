package tech.zzhdev.phunctions.parser

import tech.zzhdev.phunctions.exception.EndOfSourceException
import tech.zzhdev.phunctions.exception.NoSuchOperatorException

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

    fun march() {
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
}