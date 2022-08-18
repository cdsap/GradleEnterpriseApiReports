package io.github.cdsap.geapi.domain.model

data class Task(
    val taskType: String,
    val taskPath: String,
    val avoidanceOutcome: String,
    val duration: Long
)
