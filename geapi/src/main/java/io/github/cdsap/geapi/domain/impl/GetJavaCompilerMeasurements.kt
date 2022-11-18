package io.github.cdsap.geapi.domain.impl

import io.github.cdsap.geapi.domain.GetMeasurements
import io.github.cdsap.geapi.domain.model.Build
import io.github.cdsap.geapi.domain.model.Experiment
import io.github.cdsap.geapi.domain.model.Measurement
import io.github.cdsap.geapi.domain.model.OS

class GetJavaCompilerMeasurements : GetMeasurements {

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
        return listOf(
            Measurement(
                category = "Java Compiler",
                name = "Java tasks",
                variantA = variantABuilds.sumOf {
                    tasksByType(
                        it,
                        "org.gradle.api.tasks.compile.JavaCompile"
                    ).count()
                },
                variantB = variantBBuilds.sumOf {
                    tasksByType(
                        it,
                        "org.gradle.api.tasks.compile.JavaCompile"
                    ).count()
                },
                OS = os
            ),
            Measurement(
                category = "Java Compiler",
                name = "Java Compiler tasks UP-TO-DATE",
                variantA = sumByOutcomeAndType(
                    variantABuilds,
                    "avoided_up_to_date",
                    "org.gradle.api.tasks.compile.JavaCompile"
                ),
                variantB = sumByOutcomeAndType(
                    variantBBuilds,
                    "avoided_up_to_date",
                    "org.gradle.api.tasks.compile.JavaCompile"
                ),
                OS = os
            ),
            Measurement(
                category = "Java Compiler",
                name = "Java Compiler tasks Executed",
                variantA = variantABuilds.sumOf {
                    it.taskExecution.filter { (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable") && it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                        .count()
                },
                variantB = variantBBuilds.sumOf {
                    it.taskExecution.filter { (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable") && it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                        .count()
                },
                OS = os
            ),
            Measurement(
                category = "Java Compiler",
                name = "Java Compiler tasks from local cache",
                variantA = sumByOutcomeAndType(
                    variantABuilds,
                    "avoided_from_local_cache",
                    "org.gradle.api.tasks.compile.JavaCompile"
                ),
                variantB = sumByOutcomeAndType(
                    variantBBuilds,
                    "avoided_from_local_cache",
                    "org.gradle.api.tasks.compile.JavaCompile"
                ),
                OS = os
            ),
            Measurement(
                category = "Java Compiler",
                name = "Java Compiler tasks from remote cache",
                variantA = sumByOutcomeAndType(
                    variantABuilds,
                    "avoided_from_remote_cache",
                    "org.gradle.api.tasks.compile.JavaCompile"
                ),
                variantB = sumByOutcomeAndType(
                    variantBBuilds,
                    "avoided_from_remote_cache",
                    "org.gradle.api.tasks.compile.JavaCompile"
                ),
                OS = os
            ),
            Measurement(
                category = "Java Compiler",
                name = "Java Compiler aggregated time",
                variantA = variantABuilds.sumOf {
                    it.taskExecution.filter {
                        it.taskType == "org.gradle.api.tasks.compile.JavaCompile"
                    }
                        .sumOf { it.duration }
                },
                variantB = variantBBuilds.sumOf {
                    it.taskExecution.filter {
                        it.taskType == "org.gradle.api.tasks.compile.JavaCompile"
                    }
                        .sumOf { it.duration }
                },
                OS = os
            ),
            Measurement(
                category = "Java Compiler",
                name = "Java Compiler mean task execution",
                variantA = variantABuilds.sumOf {
                    it.taskExecution.filter {
                        (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable")
                            && it.taskType == "org.gradle.api.tasks.compile.JavaCompile"
                    }
                        .sumOf { it.duration }
                } / (
                    variantABuilds.sumOf {
                        it.taskExecution.filter { it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                            .count()
                    }),
                variantBBuilds.sumOf {
                    it.taskExecution.filter {
                        (it.avoidanceOutcome == "executed_cacheable" || it.avoidanceOutcome == "executed_not_cacheable")
                            && it.taskType == "org.gradle.api.tasks.compile.JavaCompile"
                    }
                        .sumOf { it.duration }
                } / (
                    variantBBuilds.sumOf {
                        it.taskExecution.filter { it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                            .count()
                    }),
                OS = os
            )
        )
    }

    fun sumByOutcomeAndType(builds: List<Build>, outcome: String, type: String): Int {
        return builds.sumOf {
            it.taskExecution.filter { it.avoidanceOutcome == outcome && it.taskType == type }.count()
        }
    }

}
