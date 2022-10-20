package io.github.cdsap.geapi.domain.impl

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.geapi.domain.PrintExperimentResults
import io.github.cdsap.geapi.domain.model.*
import io.github.cdsap.geapi.repository.GradleEnterpriseRepository
import kotlin.math.roundToInt

class PrintExperimentResultsImpl(private val repository: GradleEnterpriseRepository) : PrintExperimentResults {

    override suspend fun print(builds: List<ScanWithAttributes>, filter: Filter): List<Measurement> {
        val buildsa = mutableListOf<Build>()

        if (builds.isNotEmpty()) {
            println("Processing build scan cache performance")

            builds.map {
                if (filter.experimentId != null) {
                    if (it.tags.contains(filter.experimentId) && it.tags.contains("pr")) {
                        collectBuild(it, buildsa, Experiment.VARIANT_B)
                    } else if (it.tags.contains(filter.experimentId) && it.tags.contains("main")) {
                        collectBuild(it, buildsa, Experiment.VARIANT_A)
                    } else {

                    }
                } else {
                    if (it.tags.contains("experiment") && it.tags.contains("pr")) {
                        collectBuild(it, buildsa, Experiment.VARIANT_B)
                    } else if (it.tags.contains("experiment") && it.tags.contains("main")) {
                        collectBuild(it, buildsa, Experiment.VARIANT_A)
                    } else {

                    }
                }
            }

        }
        val generalMeasurements = mutableListOf<Measurement>()

        // General metrics
        buildsa.groupBy { it.OS }.forEach {
            val variantABuilds = it.value.filter { it.experiment == Experiment.VARIANT_A }
            val variantBBuilds = it.value.filter { it.experiment == Experiment.VARIANT_B }

            generalMeasurements.addAll(
                listOf(
                    Measurement(
                        category = "General",
                        name = "Sample size",
                        variantA = variantABuilds.size,
                        variantB = variantBBuilds.size,
                        OS = it.key
                    ),

                    Measurement(
                        category = "General",
                        name = "BuildTime Avg",
                        variantA = variantABuilds.sumOf { it.buildDuration } / variantABuilds.size,
                        variantB = variantBBuilds.sumOf { it.buildDuration } / variantBBuilds.size,
                        OS = it.key
                    ),

                    Measurement(
                        category = "General",
                        name = "BuildTime Max",
                        variantA = variantABuilds.maxBy { it.buildDuration }.buildDuration,
                        variantB = variantBBuilds.maxBy { it.buildDuration }.buildDuration,
                        OS = it.key
                    ),
                    Measurement(
                        category = "General",
                        name = "BuildTime Min",
                        variantA = variantABuilds.minBy { it.buildDuration }.buildDuration,
                        variantB = variantBBuilds.minBy { it.buildDuration }.buildDuration,
                        OS = it.key
                    )
                )
            )
            generalMeasurements.addAll(
                listOf(
                    Measurement(
                        category = "Tasks",
                        name = "Tasks",
                        variantA = variantABuilds.sumOf { it.taskExecution.count() },
                        variantB = variantBBuilds.sumOf { it.taskExecution.count() },
                        OS = it.key
                    ),
                    Measurement(
                        category = "Tasks",
                        name = "Tasks UP-TO-DATE",
                        variantA = variantABuilds.sumOf { tasksByOutcome(it, "avoided_up_to_date").count() },
                        variantB = variantBBuilds.sumOf { tasksByOutcome(it, "avoided_up_to_date").count() },
                        OS = it.key
                    ),
                    Measurement(
                        category = "Tasks",
                        name = "Tasks Executed",
                        variantA = variantABuilds.sumOf {
                            it.taskExecution.filter {
                                it.avoidanceOutcome == "executed_not_cacheable"
                                    || it.avoidanceOutcome == "executed_cacheable"
                            }.count()
                        },
                        variantB = variantBBuilds.sumOf {
                            it.taskExecution.filter {
                                it.avoidanceOutcome == "executed_not_cacheable"
                                    || it.avoidanceOutcome == "executed_cacheable"
                            }.count()
                        },
                        OS = it.key
                    ),
                    Measurement(
                        category = "Tasks",
                        name = "Tasks from local cache",
                        variantA = variantABuilds.sumOf { tasksByOutcome(it, "avoided_from_local_cache").count() },
                        variantB = variantBBuilds.sumOf { tasksByOutcome(it, "avoided_from_local_cache").count() },
                        OS = it.key
                    ),
                    Measurement(
                        category = "Tasks",
                        name = "Tasks from remote cache",
                        variantA = variantABuilds.sumOf { tasksByOutcome(it, "avoided_from_remote_cache").count() },
                        variantB = variantBBuilds.sumOf { tasksByOutcome(it, "avoided_from_remote_cache").count() },
                        OS = it.key
                    )
                )
            )

            generalMeasurements.addAll(
                listOf(
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
                        OS = it.key
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
                        OS = it.key
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
                        OS = it.key
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
                        OS = it.key
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
                        OS = it.key
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
                        OS = it.key
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
                        OS = it.key
                    )


                )
            )



            generalMeasurements.addAll(
                listOf(
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
                        OS = it.key
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
                        OS = it.key
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
                        OS = it.key
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
                        OS = it.key
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
                        OS = it.key
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
                        OS = it.key
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
                        OS = it.key
                    )
                )
            )


//            generalMeasurements.add(
//                Measurement(
//                    category = "Avoidance Savings",
//                    name = "Total",
//                    variantA = variantABuilds.sumOf {
//                        it.avoidanceSavingsSummary?.total?.toInt() ?: 0
//                    } / variantABuilds.size,
//                    variantB = variantBBuilds.sumOf {
//                        it.avoidanceSavingsSummary?.total?.toInt() ?: 0
//                    } / variantABuilds.size,
//                    OS = it.key
//                )
//            )
//            generalMeasurements.add(
//                Measurement(
//                    category = "Avoidance Savings",
//                    name = "Remote Cache",
//                    variantA = variantABuilds.sumOf {
//                        it.avoidanceSavingsSummary?.remoteCache?.toInt() ?: 0
//                    } / variantABuilds.size,
//                    variantB = variantBBuilds.sumOf {
//                        it.avoidanceSavingsSummary?.remoteCache?.toInt() ?: 0
//                    } / variantABuilds.size,
//                    OS = it.key
//                )
//            )
//            generalMeasurements.add(
//                Measurement(
//                    category = "Avoidance Savings",
//                    name = "Ratio",
//                    variantA = variantABuilds.sumOf {
//                        it.avoidanceSavingsSummary?.ratio?.toInt() ?: 0
//                    } / variantABuilds.size,
//                    variantB = variantBBuilds.sumOf {
//                        it.avoidanceSavingsSummary?.ratio?.toInt() ?: 0
//                    } / variantABuilds.size,
//                    OS = it.key
//                )
//            )
//
//        }


        }

        println(
            table
            {
                cellStyle {
                    border = true
                    alignment = TextAlignment.MiddleLeft
                    paddingLeft = 2
                    paddingRight = 2
                }
                body {
                    row {
                        cell("Experiment") {
                            columnSpan = 4
                            alignment = TextAlignment.MiddleCenter
                        }
                    }

                    generalMeasurements.groupBy {
                        it.OS
                    }.forEach {
                        row {
                            cell(it.key.name) {
                                columnSpan = 5
                                alignment = TextAlignment.MiddleCenter
                            }
                        }
                        row {
                            cell("Category")
                            cell("Metric")
                            cell("VARIANT A")
                            cell("VARIANT B")
                            cell("Delta")
                        }
                        it.value.forEach {
                            row {
                                cell(it.category)
                                cell(it.name)
                                cell(it.variantA)
                                cell(it.variantB)
                                if (it.variantA is Int) {
                                    if ((it.variantA as Int) - (it.variantB as Int) != 0) {
                                        val x = (it.variantB * 100) / it.variantA
                                        cell(x)
                                    } else {
                                        cell("")
                                    }

                                } else if (it.variantA is Long) {
                                    if ((it.variantA as Long) - (it.variantB as Long) != 0L) {
                                        val x = (it.variantB * 100L) / it.variantA
                                        cell(x)
                                    } else {
                                        cell("")
                                    }

                                } else if (it.variantA is Double) {
                                    if ((it.variantA as Double) - (it.variantB as Double) != 0.0) {
                                        val x = (it.variantB * 100.0) / it.variantA
                                        val result = 100 - x
                                        cell("$result%"  )
                                    } else {
                                        cell("")
                                    }
                                } else {

                                }


                            }
                        }
                    }
                }

            })
        return generalMeasurements

    }

    fun sumByOutcomeAndType(builds: List<Build>, outcome: String, type: String): Int {
        return builds.sumOf {
            it.taskExecution.filter { it.avoidanceOutcome == outcome && it.taskType == type }.count()
        }
    }

    private fun tasksByOutcome(it: Build, outcome: String) =
        it.taskExecution.filter { it.avoidanceOutcome == outcome }

    private fun tasksByType(it: Build, type: String) =
        it.taskExecution.filter { it.taskType == type }

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

