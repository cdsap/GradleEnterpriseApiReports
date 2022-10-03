package io.github.cdsap.geapi.domain

import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.domain.model.ScanWithAttributes

interface PrintExperimentResults {

    suspend fun print(builds: List<ScanWithAttributes>, filter: Filter)
}
