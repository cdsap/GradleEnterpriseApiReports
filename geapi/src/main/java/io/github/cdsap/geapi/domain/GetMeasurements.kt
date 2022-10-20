package io.github.cdsap.geapi.domain

import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.domain.model.Measurement
import io.github.cdsap.geapi.domain.model.ScanWithAttributes

interface GetMeasurements {
    suspend fun get(builds: List<ScanWithAttributes>, filter: Filter): List<Measurement>
}
