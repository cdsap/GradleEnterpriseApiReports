package io.github.cdsap.geapi.domain.model

data class ScanWithAttributes(
    val id: String,
    val rootProjectName: String,
    val requestedTasks: Array<String>,
    val tags: Array<String>,
    val hasFailed: Boolean,
    val environment: Environment,
    val buildDuration: Long,
    val avoidanceSavingsSummary: AvoidanceSavingsSummary
)
