package tech.zzhdev.phunctions.exception

class EndOfSourceException(
    override val message: String?
): RuntimeException() {
    constructor() : this("") { }
}