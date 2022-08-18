package io.github.cdsap.geapi.view

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.domain.model.RequestedTasks

class RequestedTasksView(private val requestedTasks: RequestedTasks) {

    fun print(filter: Filter) {
        println(filter)
        printReport()
        printReportByTag()
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
                header {
                    row {
                        cell("Summary") {
                            columnSpan = 2
                        }
                    }
                }
                body {
                    row {
                        cell("Requested Tasks occurrences") {
                            columnSpan = 2
                        }
                    }

                    val mapSorted = mutableMapOf<String, Long>()
                    requestedTasks.occurrencesTasks
                        .forEach { (t, u) ->
                            var sum = 0L
                            u.forEach { (_, xx) ->
                                sum += xx
                            }
                            mapSorted[t] = sum
                        }

                    mapSorted.entries.associate { (k, v) -> v to k }.toSortedMap(Comparator.reverseOrder())
                        .forEach { (t, u) ->
                            row {
                                cell(t)
                                cell(u.take(60))
                            }
                        }
                }
            }
        )
    }

    private fun printReportByTag() {
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
                        cell("Tasks By Tag") {
                            columnSpan = 2
                        }
                    }
                }
                body {

                    requestedTasks.occurrencesTasksByTask.forEach { (t, u) ->
                        row {
                            cell(t) {
                                columnSpan = 2
                            }
                        }
                        u.entries.associate { (k, v) -> v to k }.toSortedMap(Comparator.reverseOrder())
                            .forEach { (x, m) ->
                                row {
                                    cell(x)
                                    cell(m.take(60))
                                }
                            }
                    }
                }
            }
        )
    }

    private fun printlnReport(
        filter: Filter
    ) {
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
                }
            }
        )

        println("Loading data...")
    }
}
