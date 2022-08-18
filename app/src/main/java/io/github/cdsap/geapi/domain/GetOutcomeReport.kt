package io.github.cdsap.geapi.domain

import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.domain.model.Outcome
import io.github.cdsap.geapi.domain.model.ScanWithAttributes

interface GetOutcomeReport {
    suspend fun get(builds: List<ScanWithAttributes>, filter: Filter): Outcome
}
