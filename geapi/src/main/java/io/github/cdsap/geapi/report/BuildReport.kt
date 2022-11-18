package io.github.cdsap.geapi.report

import io.github.cdsap.geapi.domain.impl.GetBuildScansWithQueryImpl
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.repository.impl.GradleRepositoryImpl
import io.github.cdsap.geapi.view.BuildAnalyzerView

class BuildReport(
    val filter: Filter,
    val repository: GradleRepositoryImpl
) : Report {

    override suspend fun process() {
        val getBuildScans = GetBuildScansWithQueryImpl(repository).get(filter)
        BuildAnalyzerView(getBuildScans.toTypedArray()).print(filter)
    }
}
