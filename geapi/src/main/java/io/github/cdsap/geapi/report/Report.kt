package io.github.cdsap.geapi.report

import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.network.GEClient
import io.github.cdsap.geapi.repository.impl.GradleRepositoryImpl

interface Report {
    suspend fun process()
}
