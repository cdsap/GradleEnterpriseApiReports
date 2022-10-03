package io.github.cdsap.geapi.domain.impl

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.geapi.domain.PrintExperimentResults
import io.github.cdsap.geapi.domain.model.*
import io.github.cdsap.geapi.repository.GradleEnterpriseRepository
import kotlin.system.exitProcess

class PrintExperimentResultsImpl(private val repository: GradleEnterpriseRepository) : PrintExperimentResults {

    override suspend fun print(builds: List<ScanWithAttributes>, filter: Filter) {
        val buildsa = mutableListOf<Build>()
        if (builds.isNotEmpty()) {
            println("Processing build scan cache performance")

            builds.map {
                if (it.tags.contains("experiment") && it.tags.contains("pr")) {
                    collectBuild(it, buildsa, Experiment.VARIANT_B)
                } else if (it.tags.contains("experiment") && it.tags.contains("main")) {
                    collectBuild(it, buildsa, Experiment.VARIANT_A)
                } else {
                    println("build not under experiments")
                }
            }

        }
        val generalMeasurements = mutableListOf<Measurement>()

        // GENERAL


        // General metrics
        buildsa.groupBy { it.OS }.forEach {
            val variantABuilds = it.value.filter { it.experiment == Experiment.VARIANT_A }
            val variantBBuilds = it.value.filter { it.experiment == Experiment.VARIANT_B }
            generalMeasurements.addAll(
                listOf(
                    Measurement(
                        name = "[General] Sample size",
                        variantA = variantABuilds.size,
                        variantB = variantBBuilds.size,
                        OS = it.key
                    ),

                    Measurement(
                        name = "[General] BuildTime Avg",
                        variantA = variantABuilds.sumOf { it.buildDuration } / variantABuilds.size,
                        variantB = variantBBuilds.sumOf { it.buildDuration } / variantBBuilds.size,
                        OS = it.key
                    ),

                    Measurement(
                        name = "[General] BuildTime Max",
                        variantA = variantABuilds.maxBy { it.buildDuration }.buildDuration,
                        variantB = variantBBuilds.maxBy { it.buildDuration }.buildDuration,
                        OS = it.key
                    ),
                    Measurement(
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
                        name = "[Tasks] Tasks",
                        variantA = variantABuilds.sumOf { it.taskExecution.count() },
                        variantB = variantBBuilds.sumOf { it.taskExecution.count() },
                        OS = it.key
                    ),
                    Measurement(
                        name = "[Tasks] Tasks UP-TO-DATE",
                        variantA = variantABuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "avoided_up_to_date" }.count()
                        },
                        variantB = variantBBuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "avoided_up_to_date" }.count()
                        },
                        OS = it.key
                    ),
                    Measurement(
                        name = "[Tasks] Tasks not cacheable Executed",
                        variantA = variantABuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "executed_not_cacheable" }.count()
                        },
                        variantB = variantBBuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "executed_not_cacheable" }.count()
                        },
                        OS = it.key
                    ),
                    Measurement(
                        name = "[Tasks] Tasks cacheable Executed",
                        variantA = variantABuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "executed_cacheable" }.count()
                        },
                        variantB = variantBBuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "executed_cacheable" }.count()
                        },
                        OS = it.key
                    ),

                    Measurement(
                        name = "[Tasks] Tasks from local cache",
                        variantA = variantABuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "avoided_from_local_cache" }.count()
                        },
                        variantB = variantBBuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "avoided_from_local_cache" }.count()
                        },
                        OS = it.key
                    ),
                    Measurement(
                        name = "[Tasks] Tasks from remote cache",
                        variantA = variantABuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "avoided_from_remote_cache" }.count()
                        },
                        variantB = variantBBuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "avoided_from_remote_cache" }.count()
                        },
                        OS = it.key
                    )
                )
            )



            generalMeasurements.addAll(
                listOf(
                    Measurement(
                        name = "[kotlin-compiler] Kotlin Compiler tasks",
                        variantA = variantABuilds.sumOf {
                            it.taskExecution.filter { it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile" }
                                .count()
                        },
                        variantB = variantBBuilds.sumOf {
                            it.taskExecution.filter { it.taskType == "org.jetbrains.kotlin.gradle.tasks.KotlinCompile" }
                                .count()
                        },
                        OS = it.key
                    ),
                    Measurement(
                        name = "[kotlin-compiler]  Kotlin Compiler tasks UP-TO-DATE",
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
                        name = "[kotlin-compiler]  Kotlin Compiler tasks Executed",
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
                        name = "[kotlin-compiler]  Kotlin Compiler tasks from local cache",
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
                        name = "[kotlin-compiler]  Kotlin Compiler tasks from remote cache",
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
                        name = "[kotlin-compiler]  Kotlin Compiler aggregated time",
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
                        name = " [kotlin-compiler]  Kotlin Compiler mean task execution",
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
                        name = "[Java compiler] Java tasks",
                        variantA = variantABuilds.sumOf {
                            it.taskExecution.filter { it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                                .count()
                        },
                        variantB = variantBBuilds.sumOf {
                            it.taskExecution.filter { it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                                .count()
                        },
                        OS = it.key
                    ),
                    Measurement(
                        name = "[Java compiler] Java Compiler tasks UP-TO-DATE",
                        variantA = variantABuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "avoided_up_to_date" && it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                                .count()
                        },
                        variantB = variantBBuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "avoided_up_to_date" && it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                                .count()
                        },
                        OS = it.key
                    ),
                    Measurement(
                        name = "[Java compiler] Java Compiler tasks not cacheable Executed",
                        variantA = variantABuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "executed_not_cacheable" && it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                                .count()
                        },
                        variantB = variantBBuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "executed_not_cacheable" && it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                                .count()
                        },
                        OS = it.key
                    ),
                    Measurement(
                        name = "[Java compiler] Java Compiler tasks cacheable Executed",
                        variantA = variantABuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "executed_cacheable" && it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                                .count()
                        },
                        variantB = variantBBuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "executed_cacheable" && it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                                .count()
                        },
                        OS = it.key
                    ),
                    Measurement(
                        name = "[Java compiler] Java Compiler tasks from local cache",
                        variantA = variantABuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "avoided_from_local_cache" && it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                                .count()
                        },
                        variantB = variantBBuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "avoided_from_local_cache" && it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                                .count()
                        },
                        OS = it.key
                    ),
                    Measurement(
                        name = "[Java compiler] Java Compiler tasks from remote cache",
                        variantA = variantABuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "avoided_from_remote_cache" && it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                                .count()
                        },
                        variantB = variantBBuilds.sumOf {
                            it.taskExecution.filter { it.avoidanceOutcome == "avoided_from_remote_cache" && it.taskType == "org.gradle.api.tasks.compile.JavaCompile" }
                                .count()
                        },
                        OS = it.key
                    )
                )
            )
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
                        cell("Executions by outcome") {
                            columnSpan = 3
                            alignment = TextAlignment.MiddleCenter
                        }
                    }

                    generalMeasurements.groupBy {
                        it.OS
                    }.forEach {
                        row {
                            cell(it.key.name) {
                                columnSpan = 3
                                alignment = TextAlignment.MiddleCenter
                            }
                        }
                        row {
                            cell("Metric") {

                            }
                            cell("VARIANT A") {

                            }
                            cell("VARIANT B") {

                            }
                        }
                        it.value.forEach {
                            row {
                                cell(it.name)
                                cell(it.variantA)
                                cell(it.variantB)
                            }
                        }
                    }
                }
//                taskGeneralMeasurements.groupBy {
//                    it.OS
//                }.forEach {
//                    it.value.forEach {
//                        row {
//
//
//                            cell(it.name)
//                            cell(it.variantA)
//                            cell(it.variantB)
//                        }
//                    }
//                }
//                kotlinCompilerMeasurements.groupBy {
//                    it.OS
//                }.forEach {
//                    it.value.forEach {
//                        row {
//
//                            cell(it.name)
//                            cell(it.variantA)
//                            cell(it.variantB)
//                        }
//                    }
//                }
//                javaCompilerMeasurements.groupBy {
//                    it.OS
//                }.forEach {
//                    it.value.forEach {
//                        row {
//
//                            cell(it.name)
//                            cell(it.variantA)
//                            cell(it.variantB)
//                        }
//                    }
//                }
//            }
            })

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

