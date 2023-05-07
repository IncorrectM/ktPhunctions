package tech.zzhdev.phunctions.expression

import tech.zzhdev.phunctions.exception.EvaluationErrorException
import java.util.*

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

data class FunctionCallExpression(
    val id: String,
    val args: List<Expression> = ArrayList()
): Expression {
    override fun eval(): Result<EvaluationResult> {
        val env = GlobalEnvironment.getCurrentEnvironment() ?:
            return Result.failure(EvaluationErrorException("no valid environment found"))

        val function = env.getVarUpwards(id).getOrElse {
            return Result.failure(it)
        }

        if (function !is FunctionExpression) {
            return Result.failure(EvaluationErrorException("$id is not a function"))
        }

        for (arg in args) {
            function.environment.pushGeneralExpression(arg)
        }

        return function.eval()
    }
}

data class FunctionExpression(
    val environment: Environment,
    val args: ArgsDefinitionExpression,
    val expression: SymbolExpression
): Expression {
    override fun eval(): Result<EvaluationResult> {
        val argList = Stack<Expression>()
        for (arg in args.args) {
            val value = environment.popGeneralExpression()
                ?: return Result.failure(EvaluationErrorException("not enough arguments provided"))
            argList.push(value)
        }

        for (arg in args.args) {
            environment.putVar(arg.id, argList.pop())
        }

        val parentEnv = GlobalEnvironment.getCurrentEnvironment()!!
        parentEnv.pushSubEnvironment(environment)
        val result = expression.eval()
        parentEnv.popCurrentEnvironment()
        return result
    }

}

data class FunctionDefinitionExpression(
    val id: IdentifierExpression,
    val args: ArgsDefinitionExpression,
    val expression: SymbolExpression
): Expression {
    override fun eval(): Result<EvaluationResult> {
        val env = Environment()
        val parentEnv = GlobalEnvironment.getCurrentEnvironment() ?:
            return Result.failure(EvaluationErrorException("no valid environment found"))

        parentEnv.pushSubEnvironment(env)
        args.eval() // define args that is related to the function
        parentEnv.popCurrentEnvironment()

        parentEnv.putVar(id.id,
            FunctionExpression(
                env, args, expression
            ))

        return Result.success(0)
    }

}

data class VariableDefinitionExpression(
    val children: ArrayList<Expression> = ArrayList(),
    var evalNow: Boolean = false
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
        if (evalNow) {
            val idValue = when (valueExpression) {
                is SymbolExpression -> valueExpression.eval().getOrElse {
                    return Result.failure(it)
                }

                is ConstantIntExpression -> valueExpression.value

                else -> {
                    return Result.failure(EvaluationErrorException("expecting symbol expression or constant int"))
                }
            }
            GlobalEnvironment.putVar(name.id, ConstantIntExpression(idValue))
            return Result.success(idValue)
        }
        GlobalEnvironment.putVar(name.id, valueExpression)
        return Result.success(0)
    }
}

data class ArgsDefinitionExpression(
    val args: ArrayList<IdentifierExpression> = arrayListOf(),
): Expression {

    val argsCount
        get() = args.size

    fun addArg(id: IdentifierExpression) {
        args.add(id)
    }

    override fun eval(): Result<EvaluationResult> {
        val env = GlobalEnvironment.getCurrentEnvironment()
            ?: return Result.failure(EvaluationErrorException("no valid environment for registering"))

        args.forEach {
            // initialize variable with default value
            env.putVar(it.id, ConstantIntExpression(0))
        }

        return Result.success(0)
    }

}

data class IdentifierExpression(val id: String): Expression {
    override fun eval(): Result<EvaluationResult> {
        val env = GlobalEnvironment.getCurrentEnvironment() ?:
            return Result.failure(EvaluationErrorException("no valid environment found"))
        val value = env.getVarUpwards(id).getOrElse {
            return Result.failure(it)
        }.eval().getOrElse {
            return Result.failure(it)
        }
        return Result.success(value)
    }
}

data class IfExpression(
    val condition: Expression,
    val trueBranch: Expression,
    val falseBranch: Expression,
): Expression {
    override fun eval(): Result<EvaluationResult> {
        val conditionalResult = condition.eval().getOrElse {
            return Result.failure(it)
        }

        return if (conditionalResult == 0) {
            falseBranch.eval()
        } else {
            trueBranch.eval()
        }
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

        when (operator) {
            is VariableDefinitionExpression -> {
                return operator.eval()
            }

            is ArgsDefinitionExpression -> {
                return operator.eval()
            }

            is FunctionDefinitionExpression -> {
                return operator.eval()
            }

            is FunctionCallExpression -> {
                return operator.eval()
            }

            is IfExpression -> {
                return operator.eval()
            }
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