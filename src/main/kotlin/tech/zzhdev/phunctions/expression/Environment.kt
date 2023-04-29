package tech.zzhdev.phunctions.expression

import tech.zzhdev.phunctions.exception.EvaluationErrorException

object Environment {
    private val variables = HashMap<String, Expression>()

    // variables are evaluated during access
    fun getVar(id: String): Result<EvaluationResult> = variables[id]?.eval() ?:
        Result.failure(EvaluationErrorException("$id is no defined"))

    fun putVar(id: String, value: Expression) {
        variables[id] = value
    }
}