package io.github.cdsap.geapi.domain.impl

import io.github.cdsap.geapi.domain.FilterExperiments
import io.github.cdsap.geapi.domain.model.*
import io.github.cdsap.geapi.repository.GradleEnterpriseRepository

class FilterExperimentsImpl(val repository: GradleEnterpriseRepository) : FilterExperiments {
    override suspend fun filter(builds: List<ScanWithAttributes>, filter: Filter): List<Build> {
        val buildsFiltered = mutableListOf<Build>()

        if (builds.isNotEmpty()) {
            println("Processing build scan cache performance")

            builds.map {
                if (filter.experimentId != null) {
                    if (it.tags.contains(filter.experimentId) && it.tags.contains("pr")) {
                        collectBuild(it, buildsFiltered, Experiment.VARIANT_B)
                    } else if (it.tags.contains(filter.experimentId) && it.tags.contains("main")) {
                        collectBuild(it, buildsFiltered, Experiment.VARIANT_A)
                    } else {

                    }
                } else {
                    if (it.tags.contains("experiment") && it.tags.contains("pr")) {
                        collectBuild(it, buildsFiltered, Experiment.VARIANT_B)
                    } else if (it.tags.contains("experiment") && it.tags.contains("main")) {
                        collectBuild(it, buildsFiltered, Experiment.VARIANT_A)
                    } else {

                    }
                }
            }

        }
        return buildsFiltered
    }

    private suspend fun collectBuild(
        it: ScanWithAttributes,
        buildsa: MutableList<Build>,
        experiment: Experiment
    ) {
        var os = if (it.tags.contains("Mac OS X")) {
            OS.MAC
        } else if (it.tags.contains("Linux")) {
            OS.Linux
        } else {
            null
        }
        if (os != null) {
            val cachePerformance = repository.getBuildScanCachePerformance(it.id)
            cachePerformance.experiment = experiment
            cachePerformance.id = it.id
            cachePerformance.requestedTask = it.requestedTasks
            cachePerformance.tags = it.tags
            cachePerformance.buildDuration = it.buildDuration
            cachePerformance.OS = os
            buildsa.add(cachePerformance)
        }
    }
}
