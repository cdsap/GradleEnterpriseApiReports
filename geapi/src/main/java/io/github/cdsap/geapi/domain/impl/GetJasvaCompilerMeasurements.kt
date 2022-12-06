package io.github.cdsap.geapi.domain.impl

import io.github.cdsap.geapi.domain.GetMeasurements
import io.github.cdsap.geapi.domain.model.Build
import io.github.cdsap.geapi.domain.model.Experiment
import io.github.cdsap.geapi.domain.model.Measurement
import io.github.cdsap.geapi.domain.model.OS

class GetJasvaCompilerMeasurements : GetMeasurements {

    override fun get(builds: List<Build>): List<Measurement> {
        return builds.groupBy { it.OS }.flatMap {
            javaMeasurements(
                it.value.filter { it.experiment == Experiment.VARIANT_A },
                it.value.filter { it.experiment == Experiment.VARIANT_B },
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
//            measurements.add(
//                Measurement(
//                    category = "Tasks Compiler",
//                    name = task.taskType,
//                    variantA = variantABuilds.sumOf { build ->
//                        tasksByType(
//                            build,
//                            task.taskType
//                        ).count()
//                    },
//                    variantB = variantBBuilds.sumOf { build ->
//                        tasksByType(
//                            build,
//                            task.taskType
//                        ).count()
//                    },
//                    OS = os
//                )
//            )
//            measurements.add(
//                Measurement(
//                    category = "Tasks Compiler",
//                    name = "${task.taskType}  UP-TO-DATE",
//                    variantA = sumByOutcomeAndType(
//                        variantABuilds,
//                        "avoided_up_to_date",
//                        task.taskType
//                    ),
//                    variantB = sumByOutcomeAndType(
//                        variantBBuilds,
//                        "avoided_up_to_date",
//                        task.taskType
//                    ),
//                    OS = os
//                )
//            )
//            measurements.add(
//                Measurement(
//                    category = "Tasks Compiler",
//                    name = "${task.taskType}  tasks Executed",
//                    variantA = variantABuilds.sumOf { build ->
//                        build.taskExecution.filter { (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable") && it.taskType == task.taskType }
//                            .count()
//                    },
//                    variantB = variantBBuilds.sumOf { build ->
//                        build.taskExecution.filter { (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable") && it.taskType == task.taskType }
//                            .count()
//                    },
//                    OS = os
//                )
//            )
//            measurements.add(
//                Measurement(
//                    category = "Tasks Compiler",
//                    name = "${task.taskType}  aggregated time",
//                    variantA =  variantABuilds.sumOf {
//                        it.taskExecution.filter {
//                            it.taskType == task.taskType
//                        }
//                            .sumOf { it.duration }
//                    },
//                    variantB =  variantBBuilds.sumOf {
//                        it.taskExecution.filter {
//                            it.taskType == task.taskType
//                        }
//                            .sumOf { it.duration }
//                    },
//                    OS = os
//                )
//            )
            measurements.add(
                Measurement(
                    category = "Tasks Compiler",
                    name = "${task.taskType}  mean task execution",
                    variantA =  variantABuilds.sumOf {
                        it.taskExecution.filter {
                            (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable")
                                && it.taskType ==task.taskType
                        }
                            .sumOf { it.duration }
                    } / (
                        variantABuilds.sumOf {
                            it.taskExecution.filter { it.taskType == task.taskType }
                                .count()
                        }),
                    variantB =  variantBBuilds.sumOf {
                        it.taskExecution.filter {
                            (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable")
                                && it.taskType == task.taskType
                        }
                            .sumOf { it.duration }
                    } / (
                        variantBBuilds.sumOf {
                            it.taskExecution.filter { it.taskType == task.taskType }
                                .count()
                        }),
                    OS = os
                )
            )
        }
        return measurements

    }

    fun sumByOutcomeAndType(builds: List<Build>, outcome: String, type: String): Int {
        return builds.sumOf {
            it.taskExecution.filter { it.avoidanceOutcome == outcome && it.taskType == type }.count()
        }
    }

}
