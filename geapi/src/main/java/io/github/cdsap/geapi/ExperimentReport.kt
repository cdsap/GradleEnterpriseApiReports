package io.github.cdsap.geapi

import io.github.cdsap.geapi.domain.impl.GetBuildScansWithQueryImpl
import io.github.cdsap.geapi.domain.impl.GetOutcomeReportImpl
import io.github.cdsap.geapi.domain.impl.PrintExperimentResultsImpl
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.repository.impl.GradleRepositoryImpl
import io.github.cdsap.geapi.view.OutcomeView

class ExperimentReport(val filter: Filter, val repository: GradleRepositoryImpl) {

    suspend fun process() {

        val getBuildScans = GetBuildScansWithQueryImpl(repository)
        val getReport = PrintExperimentResultsImpl(repository)
        val buildScansFiltered = getBuildScans.get(filter)
        getReport.print(buildScansFiltered, filter)

    }
}

