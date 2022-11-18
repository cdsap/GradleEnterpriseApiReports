package io.github.cdsap.geapi.domain

import io.github.cdsap.geapi.domain.model.Build
import io.github.cdsap.geapi.domain.model.Measurement

interface GetMeasurements {
    fun get(builds: List<Build>): List<Measurement>

    fun tasksByType(it: Build, type: String) = it.taskExecution.filter { it.taskType == type }

}
