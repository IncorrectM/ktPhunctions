package tech.zzhdev.phunctions.expression

import tech.zzhdev.phunctions.exception.EvaluationErrorException
import java.util.*

open class Environment {
    var parentEnvironment: Environment? = null
    private val subEnvironmentsStack = Stack<Environment>()

    private val generals = Stack<Expression>()  // a place for exchanging data
    private val variables = HashMap<String, Expression>()

    fun getCurrentEnvironment(): Environment? {
        if (subEnvironmentsStack.isEmpty()) {
            return GlobalEnvironment
        }
        return subEnvironmentsStack.peek()
    }

    fun popCurrentEnvironment(): Environment? {
        if (subEnvironmentsStack.isEmpty()) {
            return GlobalEnvironment
        }
        val popped = subEnvironmentsStack.pop()
        popped.parentEnvironment = null
        return popped
    }

    fun pushSubEnvironment(env: Environment) {
        env.parentEnvironment = this
        subEnvironmentsStack.push(env)
    }

    // variables are evaluated during access
    fun getVar(id: String): Result<EvaluationResult> = variables[id]?.eval() ?:
        Result.failure(EvaluationErrorException("$id is not defined"))

    fun getVarUpwards(id: String): Result<Expression> {
        val variable = variables[id]
        if (variable != null) {
            return Result.success(variable)
        }

        if (parentEnvironment == null) {
            // top level environment
            return Result.failure(EvaluationErrorException("$id is not defined"))
        }

        return parentEnvironment!!.getVarUpwards(id)
    }

    fun putVar(id: String, value: Expression) {
        variables[id] = value
    }

    fun popGeneralExpression(): Expression? = if (generals.isEmpty()) null else generals.pop()

    fun pushGeneralExpression(exp: Expression) = generals.push(exp)
}

object GlobalEnvironment: Environment() {
    init {
        parentEnvironment = null
    }
}