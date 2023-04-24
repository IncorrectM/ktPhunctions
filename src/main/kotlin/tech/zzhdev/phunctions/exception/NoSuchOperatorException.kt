package tech.zzhdev.phunctions.exception

data class NoSuchOperatorException(
    val pos: Int,
    val opr: String
): RuntimeException() {
}