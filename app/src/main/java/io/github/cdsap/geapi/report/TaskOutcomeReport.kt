package io.github.cdsap.geapi.report

import io.github.cdsap.geapi.domain.impl.GetBuildScansWithQueryImpl
import io.github.cdsap.geapi.domain.impl.GetOutcomeReportImpl
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.repository.GradleEnterpriseRepository
import io.github.cdsap.geapi.view.OutcomeView

class TaskOutcomeReport(
    val filter: Filter,
    val repository: GradleEnterpriseRepository
) {

    suspend fun process() {
        val getBuildScans = GetBuildScansWithQueryImpl(repository)
        val getOutcome = GetOutcomeReportImpl(repository)
        val buildScansFiltered = getBuildScans.get(filter)
        val outcome = getOutcome.get(buildScansFiltered, filter)
        outcome.totalBuildsProcessed = filter.maxBuilds
        outcome.totalBuildsFiltered = buildScansFiltered.size
        OutcomeView(outcome).print(filter)
    }
}
