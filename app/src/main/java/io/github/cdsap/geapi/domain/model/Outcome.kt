package io.github.cdsap.geapi.domain.model

data class Outcome(
    val occurrencesByOutcome: MutableMap<String, Long> = mutableMapOf(),
    val durationByOutcome: MutableMap<String, Long> = mutableMapOf(),
    val occurrencesByOutcomeAndTask: MutableMap<String, MutableMap<String, Long>> = mutableMapOf(),
    val durationByOutcomeAndTask: MutableMap<String, MutableMap<String, Long>> = mutableMapOf(),
    var totalBuildsProcessed: Int = 0,
    var totalBuildsFiltered: Int = 0,
    val taskType: String?
)
