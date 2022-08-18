package io.github.cdsap.geapi.view

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.geapi.domain.model.Detector
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.domain.model.Outcome

class OutcomeView(private val outcome: Outcome) {

    fun print(filter: Filter) {
        printlnReport(filter)
        printReport()
        printReportByTask()
    }

    private fun printReportByTask() {
        val detector = mutableListOf<Detector>()
        outcome.occurrencesByOutcomeAndTask.toSortedMap().forEach { task ->

            // The main goal is to compare Remote outcome. If the task doesn't have outcomes
            // registered as remote we skip the task
            if (task.value.contains("avoided_from_remote_cache")) {
                val tempOutcome = mutableMapOf<String, Long>()
                task.value.forEach {
                    tempOutcome[it.key] = outcome.durationByOutcomeAndTask[task.key]!![it.key]!! / it.value
                }
                if (tempOutcome.contains("avoided_from_remote_cache") && tempOutcome.contains("executed_cacheable") &&
                    tempOutcome["avoided_from_remote_cache"]!! > tempOutcome["executed_cacheable"]!!
                ) {
                    detector.add(
                        Detector(
                            task.key,
                            tempOutcome["avoided_from_remote_cache"]!!,
                            tempOutcome["executed_cacheable"]!!,
                            outcome.occurrencesByOutcomeAndTask[task.key]!!["avoided_from_remote_cache"]!!,
                            outcome.occurrencesByOutcomeAndTask[task.key]!!["executed_cacheable"]!!
                        )
                    )
                }
            }
        }

        if (detector.isNotEmpty()) {
            println(
                table {
                    cellStyle {
                        border = true
                        alignment = TextAlignment.MiddleLeft
                        paddingLeft = 2
                        paddingRight = 2
                    }
                    header {
                        row {
                            cell("Task paths with Remote outcome duration greater than execution") {
                                columnSpan = 5
                            }
                        }
                    }
                    body {
                        row {
                            cell("Task path")
                            cell("Remote outcome occurrences")
                            cell("Execution outcome occurrences")
                            cell("Remote outcome mean")
                            cell("Execution outcome mean")
                        }
                        detector.forEach {
                            row {
                                cell(it.task)
                                cell(it.remoteTimes)
                                cell(it.executedTimes)
                                cell(it.remoteTime)
                                cell(it.executeTime)
                            }
                        }
                    }
                }
            )
        }
    }

    private fun printReport() {
        println(
            table {
                cellStyle {
                    border = true
                    alignment = TextAlignment.MiddleLeft
                    paddingLeft = 2
                    paddingRight = 2
                }
                body {

                    row {
                        cell("Executions by outcome") {
                            columnSpan = 2
                        }
                    }
                    outcome.occurrencesByOutcome.forEach { (t, u) ->
                        row {
                            cell(t)
                            cell(u)
                        }
                    }
                    row {
                        cell("Duration by outcome(ms)") {
                            columnSpan = 2
                        }
                    }
                    outcome.durationByOutcome.forEach { (t, u) ->
                        row {
                            cell(t)
                            cell(readableDuration(u))
                        }
                    }
                    row {
                        cell("Mean time by outcome(ms)") {
                            columnSpan = 2
                        }
                    }
                    outcome.durationByOutcome.forEach { (t, u) ->
                        row {
                            cell(t)
                            cell(readableDuration(u / outcome.occurrencesByOutcome[t]!!))
                        }
                    }
                }
            }
        )
    }

    private fun printlnReport(
        filter: Filter
    ) {
        val end = System.currentTimeMillis()
        val duration = end - filter.initFilter
        println(
            table {
                cellStyle {
                    border = true
                    alignment = TextAlignment.TopCenter
                }
                header {
                    row {
                        cell("Gradle Enterprise API") {
                            columnSpan = 2
                        }
                    }
                }
                body {
                    row {
                        cell("Server")
                        cell(filter.url)
                    }
                    row {
                        cell("Report type")
                        cell("xxxx")
                    }
                    row {
                        cell("MaxBuilds")
                        cell(filter.maxBuilds)
                    }
                    if (filter.project != null) {
                        row {
                            cell("Project")
                            cell(filter.project)
                        }
                    }
                    row {
                        cell("Including Failed builds")
                        cell(filter.includeFailedBuilds)
                    }
                    if (filter.requestedTask != null) {
                        row {
                            cell("Requested Task")
                            cell(filter.requestedTask)
                        }
                    }
                    if (filter.tags.isNotEmpty()) {
                        row {
                            cell("Tags")
                            cell(filter.tags.joinToString())
                        }
                    }
                    if (filter.taskType != null) {
                        row {
                            cell("Task type")
                            cell(filter.taskType)
                        }
                    }
                    row {
                        cell("Total builds processed")
                        cell(outcome.totalBuildsProcessed)
                    }
                    row {
                        cell("Total build filtered")
                        cell(outcome.totalBuildsFiltered)
                    }

                    row {
                        cell("Duration")
                        cell(readableDuration(duration))
                    }
                }
            }
        )
    }

    private fun readableDuration(duration: Long): String {
        val minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60
        return if (minutes == 0L) {
            if (seconds == 0L) {
                "$duration milliseconds"
            } else {
                "$seconds seconds"
            }
        } else {
            "$minutes minutes and $seconds seconds"
        }
    }
}
