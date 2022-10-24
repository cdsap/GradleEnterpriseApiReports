package io.github.cdsap.geapi.report

import io.github.cdsap.geapi.domain.impl.*
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.repository.impl.GradleRepositoryImpl
import io.github.cdsap.geapi.view.ExperimentResultView

class ExperimentReport(val filter: Filter, val repository: GradleRepositoryImpl) {

    suspend fun process() {

        val buildScansFiltered = GetBuildScansWithQueryImpl(repository).get(filter)
        val buildsExperiment = FilterExperimentsImpl(repository).filter(buildScansFiltered, filter)

        ExperimentResultView().print(GetMeasurementsImpl(repository).get(buildsExperiment))
    }
}

