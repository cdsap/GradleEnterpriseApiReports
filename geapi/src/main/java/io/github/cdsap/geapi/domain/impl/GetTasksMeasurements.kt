package io.github.cdsap.geapi.domain.impl

import io.github.cdsap.geapi.domain.GetMeasurements
import io.github.cdsap.geapi.domain.model.*

class GetTasksMeasurements : GetMeasurements {

    override fun get(builds: List<Build>): List<Measurement> {
        return builds.groupBy { it.OS }.flatMap {
            javaMeasurements(
                it.value.filter { it.experiment == Experiment.VARIANT_A }.drop(2),
                it.value.filter { it.experiment == Experiment.VARIANT_B }.drop(2),
                it.key
            )
        }
    }

    private fun javaMeasurements(
        variantABuilds: List<Build>,
        variantBBuilds: List<Build>,
        os: OS
    ): List<Measurement> {
        val taskTypes = variantABuilds[0].taskExecution.filter {
            (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable")
        }.distinctBy { it.taskType }
        val measurements = mutableListOf<Measurement>()
        taskTypes.forEach { task ->

            val sumVariantA = variantABuilds.sumOf {
                filterByExecutionAndType(it, task)
                    .sumOf { it.duration }
            } / variantABuilds.sumOf {
                filterByExecutionAndType(it, task)
                    .count()
            }
            var process = true

            if (sumVariantA < 100L) {
                process = false
            }

            if (process) {
                measurements.add(
                    Measurement(
                        category = "Tasks Compiler",
                        name = task.taskType,
                        variantA = variantABuilds.sumOf {
                            filterByExecutionAndType(it, task)
                                .sumOf { it.duration }
                        } /
                            variantABuilds.sumOf {
                                filterByExecutionAndType(it, task)
                                    .count()
                            },
                        variantB = variantBBuilds.sumOf {
                            filterByExecutionAndType(it, task)
                                .sumOf { it.duration }
                        } /
                            variantBBuilds.sumOf {
                                filterByExecutionAndType(it, task)
                                    .count()
                            },
                        OS = os
                    )
                )
            }
        }
        measurements.sortBy { it.name }
        return measurements

    }

    private fun filterByExecutionAndType(
        it: Build,
        task: Task
    ) = it.taskExecution.filter {
        (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable")
            && it.taskType == task.taskType
    }
}
