package io.github.cdsap.geapi.domain

import io.github.cdsap.geapi.domain.model.Build
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.domain.model.ScanWithAttributes

interface FilterExperiments {
    suspend fun filter(builds: List<ScanWithAttributes>, filter: Filter): List<Build>
}
