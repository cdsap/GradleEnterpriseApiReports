package io.github.cdsap.geapi

import io.github.cdsap.geapi.domain.impl.*
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.repository.impl.GradleRepositoryImpl

class ExperimentReport(val filter: Filter, val repository: GradleRepositoryImpl) {

    suspend fun process() {

        val buildScansFiltered = GetBuildScansWithQueryImpl(repository).get(filter)
        val buildsExperiment = FilterExperimentsImpl(repository).filter(buildScansFiltered, filter)

        PrintExperimentResultsImpl().print(GetMeasurementsImpl(repository).get(buildsExperiment))
    }
}

