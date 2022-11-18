package io.github.cdsap.geapi.domain.impl

import io.github.cdsap.geapi.domain.GetMeasurements
import io.github.cdsap.geapi.domain.model.Build
import io.github.cdsap.geapi.domain.model.Experiment
import io.github.cdsap.geapi.domain.model.Measurement
import io.github.cdsap.geapi.domain.model.OS

class GetGeneralMeasurements : GetMeasurements {

    override fun get(builds: List<Build>): List<Measurement> {

        return builds.groupBy { it.OS }.flatMap {
            generalMeasurements(
                it.value.filter { it.experiment == Experiment.VARIANT_A },
                it.value.filter { it.experiment == Experiment.VARIANT_B },
                it.key
            )
        }
    }

    private fun generalMeasurements(
        variantABuilds: List<Build>,
        variantBBuilds: List<Build>,
        os: OS
    ): List<Measurement> {
        return listOf(
            Measurement(
                category = "General",
                name = "Sample size",
                variantA = variantABuilds.size,
                variantB = variantBBuilds.size,
                OS = os
            ),
            Measurement(
                category = "General",
                name = "BuildTime Avg",
                variantA = variantABuilds.sumOf { it.buildDuration } / variantABuilds.size,
                variantB = variantBBuilds.sumOf { it.buildDuration } / variantBBuilds.size,
                OS = os
            ),

            Measurement(
                category = "General",
                name = "BuildTime Max",
                variantA = variantABuilds.maxBy { it.buildDuration }.buildDuration,
                variantB = variantBBuilds.maxBy { it.buildDuration }.buildDuration,
                OS = os
            ),
            Measurement(
                category = "General",
                name = "BuildTime Min",
                variantA = variantABuilds.minBy { it.buildDuration }.buildDuration,
                variantB = variantBBuilds.minBy { it.buildDuration }.buildDuration,
                OS = os
            )
        )

    }
}
