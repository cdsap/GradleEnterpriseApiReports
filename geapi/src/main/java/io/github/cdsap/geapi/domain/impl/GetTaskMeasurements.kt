package io.github.cdsap.geapi.domain.impl

import io.github.cdsap.geapi.domain.GetMeasurements
import io.github.cdsap.geapi.domain.model.Build
import io.github.cdsap.geapi.domain.model.Experiment
import io.github.cdsap.geapi.domain.model.Measurement
import io.github.cdsap.geapi.domain.model.OS

class GetTaskMeasurements : GetMeasurements {
    override fun get(builds: List<Build>): List<Measurement> {
        return builds.groupBy { it.OS }.flatMap {
            tasksMeasurements(
                it.value.filter { it.experiment == Experiment.VARIANT_A },
                it.value.filter { it.experiment == Experiment.VARIANT_B },
                it.key
            )
        }
    }

    private fun tasksMeasurements(
        variantABuilds: List<Build>,
        variantBBuilds: List<Build>,
        os: OS
    ): List<Measurement> {
        return listOf(
            Measurement(
                category = "Tasks",
                name = "Tasks",
                variantA = variantABuilds.sumOf { it.taskExecution.count() },
                variantB = variantBBuilds.sumOf { it.taskExecution.count() },
                OS = os
            ),
            Measurement(
                category = "Tasks",
                name = "Tasks UP-TO-DATE",
                variantA = variantABuilds.sumOf { tasksByOutcome(it, "avoided_up_to_date").count() },
                variantB = variantBBuilds.sumOf { tasksByOutcome(it, "avoided_up_to_date").count() },
                OS = os
            ),
            Measurement(
                category = "Tasks",
                name = "Tasks Executed",
                variantA = variantABuilds.sumOf {
                    tasksByOutcomes(
                        it,
                        "executed_not_cacheable",
                        "executed_cacheable"
                    ).count()
                },
                variantB = variantBBuilds.sumOf {
                    tasksByOutcomes(
                        it,
                        "executed_not_cacheable",
                        "executed_cacheable"
                    ).count()
                },
                OS = os
            ),
            Measurement(
                category = "Tasks",
                name = "Tasks from local cache",
                variantA = variantABuilds.sumOf { tasksByOutcome(it, "avoided_from_local_cache").count() },
                variantB = variantBBuilds.sumOf { tasksByOutcome(it, "avoided_from_local_cache").count() },
                OS = os
            ),
            Measurement(
                category = "Tasks",
                name = "Tasks from remote cache",
                variantA = variantABuilds.sumOf { tasksByOutcome(it, "avoided_from_remote_cache").count() },
                variantB = variantBBuilds.sumOf { tasksByOutcome(it, "avoided_from_remote_cache").count() },
                OS = os
            )
        )
    }

    private fun tasksByOutcome(it: Build, outcome: String) =
        it.taskExecution.filter { it.avoidanceOutcome == outcome }

    private fun tasksByOutcomes(it: Build, outcome1: String, outcome2: String) =
        it.taskExecution.filter { it.avoidanceOutcome == outcome1 || it.avoidanceOutcome == outcome2 }
}
