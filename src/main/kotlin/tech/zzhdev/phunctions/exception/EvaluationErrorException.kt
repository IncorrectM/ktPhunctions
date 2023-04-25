package tech.zzhdev.phunctions.exception

data class EvaluationErrorException(
    override val message: String?
): RuntimeException() {

}
