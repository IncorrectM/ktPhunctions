package tech.zzhdev.phunctions.expression

object Environment {
    private val variables = HashMap<String, Int>()

    fun getVar(id: String): Int? = variables[id]

    fun putVar(id: String, value: Int) {
        variables[id] = value
    }
}