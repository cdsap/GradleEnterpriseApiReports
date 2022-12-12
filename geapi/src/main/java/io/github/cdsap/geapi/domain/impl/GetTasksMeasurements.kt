package io.github.cdsap.geapi.domain.impl

import io.github.cdsap.geapi.domain.GetMeasurements
import io.github.cdsap.geapi.domain.model.Build
import io.github.cdsap.geapi.domain.model.Experiment
import io.github.cdsap.geapi.domain.model.Measurement
import io.github.cdsap.geapi.domain.model.OS

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
        val taskTypes = variantABuilds[0].taskExecution.distinctBy { it.taskType }
        val measurements = mutableListOf<Measurement>()
        taskTypes.forEach { task ->

            val sumVarianA = variantABuilds.sumOf {
                it.taskExecution.filter {
                    (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable")
                        && it.taskType == task.taskType
                }
                    .sumOf { it.duration }
            }
            var process = true
            if (sumVarianA is Long) {
                if (sumVarianA == 0L) {
                    process = false
                }
            }

            if (process) {
                measurements.add(
                    Measurement(
                        category = "Tasks Compiler",
                        name = "${task.taskType}  mean task execution",
                        variantA = variantABuilds.sumOf {
                            it.taskExecution.filter {
                                (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable")
                                    && it.taskType == task.taskType
                            }
                                .sumOf { it.duration }
                        } / (
                            variantABuilds.sumOf {
                                it.taskExecution.filter {
                                    (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable")
                                        && it.taskType == task.taskType
                                }
                                    .count()
                            }),
                        variantB = variantBBuilds.sumOf {
                            it.taskExecution.filter {
                                (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable")
                                    && it.taskType == task.taskType
                            }
                                .sumOf { it.duration }
                        } / (
                            variantBBuilds.sumOf {
                                it.taskExecution.filter {
                                    (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable")
                                        && it.taskType == task.taskType
                                }
                                    .count()
                            }),
                        OS = os
                    )
                )
            }
        }
        return measurements

    }

}
