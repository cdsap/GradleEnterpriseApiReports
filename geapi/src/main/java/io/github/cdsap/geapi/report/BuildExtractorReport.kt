package io.github.cdsap.geapi.report

import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.repository.impl.GradleRepositoryImpl

class BuildExtractorReport(
    val filter: Filter,
    val repository: GradleRepositoryImpl
) : Report {

    override suspend fun process() {
    }
}
