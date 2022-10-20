package io.github.cdsap.geapi.domain.model

data class Build(
    val taskExecution: Array<Task>,
    var tags: Array<String> = emptyArray(),
    var requestedTask: Array<String> = emptyArray(),
    var id: String = "",
    var buildDuration: Long = 0L,
    var experiment: Experiment = Experiment.VARIANT_A,
    var OS: OS = io.github.cdsap.geapi.domain.model.OS.MAC,
    val metrics: MutableMap<String, Any>,
    val avoidanceSavingsSummary: AvoidanceSavingsSummary
)

enum class Experiment {
    VARIANT_A,
    VARIANT_B
}

enum class OS {
    MAC,
    Linux
}

class Measurement(
    val category: String, val name: String, val variantA: Any, val variantB: Any, val OS: OS
) {
    fun diff(): Any {
        if (variantA is Int) {
            if ((variantA as Int) - (variantB as Int) != 0) {
                val x = (variantB * 100) / variantA
                return x
            } else {
                return ""
            }

        } else if (variantA is Long) {
            if ((variantA as Long) - (variantB as Long) != 0L) {
                val x = (variantB * 100L) / variantA
                return x
            } else {
                return ""
            }

        } else if (variantA is Double) {
            if ((variantA as Double) - (variantB as Double) != 0.0) {
                val x = (variantB * 100.0) / variantA
                val result = 100 - x
                return "$result%"
            } else {
                return ""
            }
        } else {

        }
        return ""
    }

}
