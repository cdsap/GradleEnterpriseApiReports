package io.github.cdsap.geapi.domain.model

data class RequestedTasks(
    val occurrencesTasks: MutableMap<String, MutableMap<String, Long>> = mutableMapOf(),
    val occurrencesTasksByTask: MutableMap<String, MutableMap<String, Long>> = mutableMapOf()
)
