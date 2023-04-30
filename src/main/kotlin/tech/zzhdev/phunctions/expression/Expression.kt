package tech.zzhdev.phunctions.expression

import tech.zzhdev.phunctions.exception.EvaluationErrorException

typealias EvaluationResult = Int
typealias OperationEvaluator = (num1: Int, num2: Int) -> Result<EvaluationResult>

interface Expression {
    fun eval(): Result<EvaluationResult>
}

data class OperatorExpression(
    val symbol: String
    ): Expression {

    val evaluator: OperationEvaluator? = operationEvaluators[symbol]
    override fun eval(): Result<EvaluationResult> {
        return Result.failure(EvaluationErrorException("operators can not be evaluated"))
    }

    companion object {
        private val operationEvaluators = HashMap<String, OperationEvaluator>()
        init {
            operationEvaluators["+"] = { num1: Int, num2: Int ->
                Result.success(num1 + num2)
            }
            operationEvaluators["-"] = { num1: Int, num2: Int ->
                Result.success(num1 - num2)
            }
            operationEvaluators["*"] = { num1: Int, num2: Int ->
                Result.success(num1 * num2)
            }
            operationEvaluators["/"] = { num1: Int, num2: Int ->
                if (num2 != 0) {
                    Result.success(num1 / num2)
                } else {
                    Result.failure(EvaluationErrorException("divided by zero"))
                }
            }
            operationEvaluators["do"] = { num1: Int, num2: Int ->
                Result.success(num2)
            }
        }
    }
}
data class ConstantIntExpression(val value: Int): Expression {
    override fun eval(): Result<EvaluationResult> {
        return Result.success(value)
    }

}

data class VariableDefineExpression(
    val children: ArrayList<Expression> = ArrayList()
): Expression {
    override fun eval(): Result<EvaluationResult> {
        // define variable and put it into Environment
        if (children.size != 2) {
            return Result.failure(EvaluationErrorException("define expression should have two children"))
        }

        val name = children[0]
        if (name !is IdentifierExpression) {
            return Result.failure(EvaluationErrorException("expecting identifier, got $name"))
        }

        val valueExpression = children[1]
//        val idValue = when (valueExpression) {
//            is SymbolExpression -> valueExpression.eval().getOrElse {
//                return Result.failure(it)
//            }
//
//            is ConstantIntExpression -> valueExpression.value
//
//            else -> {
//                return Result.failure(EvaluationErrorException("expecting symbol expression or constant int"))
//            }
//        }
        Environment.putVar(name.id, valueExpression)
        return Result.success(0)
    }
}

data class IdentifierExpression(val id: String): Expression {
    override fun eval(): Result<EvaluationResult> {
        val value = Environment.getVar(id).getOrElse {
            return Result.failure(it)
        }
        return Result.success(value)
    }
}

data class SymbolExpression(
    val children: ArrayList<Expression> = ArrayList()
): Expression {
    constructor(
        children: Array<Expression>
    ): this() {
        this.children.addAll(children)
    }

    override fun eval(): Result<EvaluationResult> {
        if (children.size == 0) {
            return Result.failure(EvaluationErrorException("empty expression"))
        }

        val operator = children.first()

        if (operator is VariableDefineExpression) {
            return operator.eval()
        }

        if (operator !is OperatorExpression) {
            return Result.failure(EvaluationErrorException("first child is not operator"))
        }
        if (operator.evaluator == null) {
            return Result.failure(EvaluationErrorException("'${operator.symbol}' can not be evaluated"))
        }

        if (children.size == 1) {
            return Result.failure(EvaluationErrorException("at least one operand is required"))
        }

        var x = children[1].eval().getOrElse {
            return Result.failure(it)
        }
        var index = 2
        while (index < children.size) {
            val y = children[index++].eval().getOrElse {
                return Result.failure(it)
            }
            x = operator.evaluator!!(x, y).getOrElse {
                return Result.failure(it)
            }
        }

        return Result.success(x)
    }

    private fun toStringWithIndent(indent: Int = 1, indentChar: Char = ' '): String {
        val builder = StringBuilder()
        builder.append("SymbolExpression: \n")

        for (child in children) {
            repeat(indent + 2) {
                builder.append(indentChar)
            }
            if (child is SymbolExpression) {
                builder.append(child.toStringWithIndent(indent + 2, indentChar))
            } else {
                builder.append(child)
                builder.append("\n")
            }
        }

        return builder.toString()
    }

    override fun toString(): String {
        return toStringWithIndent()
    }

    fun appendChild(expr: Expression) {
        children.add(expr)
    }
}