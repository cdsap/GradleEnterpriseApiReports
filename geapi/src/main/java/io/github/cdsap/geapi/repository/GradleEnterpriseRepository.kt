package io.github.cdsap.geapi.repository

import io.github.cdsap.geapi.domain.model.Build
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.domain.model.Scan
import io.github.cdsap.geapi.domain.model.ScanWithAttributes

interface GradleEnterpriseRepository {

    suspend fun getBuildScans(filter: Filter): Array<Scan>
    suspend fun getBuildScanAttribute(id: String): ScanWithAttributes
    suspend fun getBuildScanCachePerformance(id: String): Build
}
