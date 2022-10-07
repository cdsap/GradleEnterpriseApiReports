package io.github.cdsap.geapi.repository.impl

import io.github.cdsap.geapi.domain.model.Build
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.domain.model.Scan
import io.github.cdsap.geapi.domain.model.ScanWithAttributes
import io.github.cdsap.geapi.network.GEClient
import io.github.cdsap.geapi.repository.GradleEnterpriseRepository

class GradleRepositoryImpl(private val client: GEClient) : GradleEnterpriseRepository {

    override suspend fun getBuildScans(filter: Filter): Array<Scan> {
        val filtering = if (filter.sinceBuildId != null) {
            "sinceBuild=${filter.sinceBuildId}"
        } else {
            "since=${filter.range}"
        }
        println("${client.url}?$filtering&maxBuilds=${filter.maxBuilds}")
        return client.get("${client.url}?$filtering&maxBuilds=${filter.maxBuilds}")
    }

    override suspend fun getBuildScanAttribute(id: String): ScanWithAttributes {
        return client.get("${client.url}/$id/gradle-attributes")
    }

    override suspend fun getBuildScanCachePerformance(id: String): Build {
        return client.get("${client.url}/$id/gradle-build-cache-performance")
    }
}
