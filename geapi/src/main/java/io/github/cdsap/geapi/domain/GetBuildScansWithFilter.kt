package io.github.cdsap.geapi.domain

import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.domain.model.ScanWithAttributes

interface GetBuildScansWithFilter {
    suspend fun get(query: Filter): List<ScanWithAttributes>
}
