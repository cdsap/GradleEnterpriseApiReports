package io.github.cdsap.geapi.domain.model

data class Build(
    val taskExecution: Array<Task>,
    var tags: Array<String> = emptyArray(),
    var requestedTask: Array<String> = emptyArray(),
    var id: String = "",
    var buildDuration: Long = 0L,
    var experiment: Experiment = Experiment.VARIANT_A,
    var OS: OS = io.github.cdsap.geapi.domain.model.OS.MAC,
    val metrics: MutableMap<String, Any>
)

enum class Experiment {
    VARIANT_A,
    VARIANT_B
}

enum class OS {
    MAC,
    Linux
}

data class Measurement(val name: String, val variantA : Any, val variantB: Any, val OS: OS)
