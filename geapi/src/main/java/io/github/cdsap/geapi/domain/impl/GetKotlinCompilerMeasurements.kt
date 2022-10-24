package io.github.cdsap.geapi.domain.impl

import io.github.cdsap.geapi.domain.GetMeasurements
import io.github.cdsap.geapi.domain.model.Build
import io.github.cdsap.geapi.domain.model.Experiment
import io.github.cdsap.geapi.domain.model.Measurement
import io.github.cdsap.geapi.domain.model.OS

class GetKotlinCompilerMeasurements : GetMeasurements {
    override fun get(builds: List<Build>): List<Measurement> {
        return builds.groupBy { it.OS }.flatMap {
            kotlinMeasurements(
                it.value.filter { it.experiment == Experiment.VARIANT_A },
                it.value.filter { it.experiment == Experiment.VARIANT_B },
                it.key
            )
        }
    }

    private fun kotlinMeasurements(
        variantABuilds: List<Build>,
        variantBBuilds: List<Build>,
        os: OS
    ): List<Measurement> {
        return listOf(
            Measurement(
                category = "Kotlin Compiler",
                name = "Kotlin Compiler tasks",
                variantA = variantABuilds.sumOf {
                    tasksByType(
                        it,
                        "org.jetbrains.kotlin.gradle.tasks.KotlinCompile"
                    ).count()
                },
                variantB = variantBBuilds.sumOf {
                    tasksByType(
                        it,
                        "org.jetbrains.kotlin.gradle.tasks.KotlinCompile"
                    ).count()
                },
                OS = os
            ),
            Measurement(
                category = "Kotlin Compiler",
                name = "Kotlin Compiler tasks UP-TO-DATE",
                variantA = variantABuilds.sumOf {
                    it.taskExecution.filter { it.avoidanceOutcome == "avoided_up_to_date" && it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile" }
                        .count()
                },
                variantB = variantBBuilds.sumOf {
                    it.taskExecution.filter { it.avoidanceOutcome == "avoided_up_to_date" && it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile" }
                        .count()
                },
                OS = os
            ),
            Measurement(
                category = "Kotlin Compiler",
                name = "Kotlin Compiler tasks Executed",
                variantA = variantABuilds.sumOf {
                    it.taskExecution.filter { (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable") && it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile" }
                        .count()
                },
                variantB = variantBBuilds.sumOf {
                    it.taskExecution.filter { (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable") && it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile" }
                        .count()
                },
                OS = os
            ),
            Measurement(
                category = "Kotlin Compiler",
                name = "Kotlin Compiler tasks from local cache",
                variantA = variantABuilds.sumOf {
                    it.taskExecution.filter { it.avoidanceOutcome == "avoided_from_local_cache" && it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile" }
                        .count()
                },
                variantB = variantBBuilds.sumOf {
                    it.taskExecution.filter { it.avoidanceOutcome == "avoided_from_local_cache" && it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile" }
                        .count()
                },
                OS = os
            ),
            Measurement(
                category = "Kotlin Compiler",
                name = "Kotlin Compiler tasks from remote cache",
                variantA = variantABuilds.sumOf {
                    it.taskExecution.filter { it.avoidanceOutcome == "avoided_from_remote_cache" && it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile" }
                        .count()
                },
                variantB = variantBBuilds.sumOf {
                    it.taskExecution.filter { it.avoidanceOutcome == "avoided_from_remote_cache" && it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile" }
                        .count()
                },
                OS = os
            ),
            Measurement(
                category = "Kotlin Compiler",
                name = "Kotlin Compiler aggregated time",
                variantA = variantABuilds.sumOf {
                    it.taskExecution.filter {
                        it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile"
                    }
                        .sumOf { it.duration }
                },
                variantB = variantBBuilds.sumOf {
                    it.taskExecution.filter {
                        it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile"
                    }
                        .sumOf { it.duration }
                },
                OS = os
            ),
            Measurement(
                category = "Kotlin Compiler",
                name = "Kotlin Compiler mean task execution",
                variantA = variantABuilds.sumOf {
                    it.taskExecution.filter {
                        (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable")
                            && it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile"
                    }
                        .sumOf { it.duration }
                } / (
                    variantABuilds.sumOf {
                        it.taskExecution.filter { it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile" }
                            .count()
                    }),
                variantBBuilds.sumOf {
                    it.taskExecution.filter {
                        (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable")
                            && it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile"
                    }
                        .sumOf { it.duration }
                } / (
                    variantBBuilds.sumOf {
                        it.taskExecution.filter { it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile" }
                            .count()
                    }),
                OS = os
            )

        )
    }
}
