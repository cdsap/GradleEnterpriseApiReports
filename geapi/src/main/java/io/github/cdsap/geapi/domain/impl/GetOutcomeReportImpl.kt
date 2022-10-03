package io.github.cdsap.geapi.domain.impl

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.geapi.domain.GetOutcomeReport
import io.github.cdsap.geapi.domain.model.*
import io.github.cdsap.geapi.progressbar.ProgressBar
import io.github.cdsap.geapi.repository.GradleEnterpriseRepository

class GetOutcomeReportImpl(private val repository: GradleEnterpriseRepository) : GetOutcomeReport {

    override suspend fun get(builds: List<ScanWithAttributes>, filter: Filter): Outcome {
        val outcome = Outcome(
            totalBuildsFiltered = builds.size,
            taskType = filter.taskType
        )
        if (builds.isNotEmpty()) {
            println("Processing build scan cache performance")
            val progressBar = ProgressBar()
            progressBar.update(0, builds.size)
            var i = 0

            builds.map {
                val cachePerformance = repository.getBuildScanCachePerformance(it.id)
                val taskInfoList =
                    if (filter.taskType == null) {
                        cachePerformance.taskExecution.toList()
                    } else {
                        cachePerformance.taskExecution.filter { it.taskType == filter.taskType }
                    }

                processByOutcome(taskInfoList, outcome.occurrencesByOutcome, outcome.durationByOutcome)
                processByOutcomeAndTask(
                    taskInfoList,
                    outcome.occurrencesByOutcomeAndTask,
                    outcome.durationByOutcomeAndTask
                )
                progressBar.update(i++, builds.size)
            }
        }
        return outcome
    }

    private fun processByOutcomeAndTask(
        taskInfoList: List<Task>,
        occurrencesByOutcomeAndTask: MutableMap<String, MutableMap<String, Long>>,
        durationByOutcomeAndTask: MutableMap<String, MutableMap<String, Long>>
    ) {
        taskInfoList.forEach { task ->
            if (occurrencesByOutcomeAndTask.contains(task.taskPath)) {
                if (occurrencesByOutcomeAndTask[task.taskPath]!!.contains(task.avoidanceOutcome)) {
                    val temp = occurrencesByOutcomeAndTask[task.taskPath]!!
                    temp[task.avoidanceOutcome] = 1 + temp[task.avoidanceOutcome]!!
                    occurrencesByOutcomeAndTask[task.taskPath] = temp
                } else {
                    val temp = occurrencesByOutcomeAndTask[task.taskPath]!!
                    temp[task.avoidanceOutcome] = 1
                    occurrencesByOutcomeAndTask[task.taskPath] = temp
                }
            } else {
                occurrencesByOutcomeAndTask[task.taskPath] = mutableMapOf(task.avoidanceOutcome to 1)
            }

            if (durationByOutcomeAndTask.contains(task.taskPath)) {
                if (durationByOutcomeAndTask[task.taskPath]!!.contains(task.avoidanceOutcome)) {
                    val temp = durationByOutcomeAndTask[task.taskPath]!!
                    temp[task.avoidanceOutcome] = task.duration + temp[task.avoidanceOutcome]!!
                    durationByOutcomeAndTask[task.taskPath] = temp
                } else {
                    val temp = durationByOutcomeAndTask[task.taskPath]!!
                    temp[task.avoidanceOutcome] = task.duration
                    durationByOutcomeAndTask[task.taskPath] = temp
                }
            } else {
                durationByOutcomeAndTask[task.taskPath] = mutableMapOf(task.avoidanceOutcome to task.duration)
            }
        }
    }

    private fun processByOutcome(
        taskInfoList: List<Task>,
        occurrencesByOutcome: MutableMap<String, Long>,
        durationByOutcome: MutableMap<String, Long>
    ) {
        taskInfoList
            .groupBy {
                it.avoidanceOutcome
            }
            .forEach { task ->
                if (occurrencesByOutcome.contains(task.key)) {
                    occurrencesByOutcome[task.key] = task.value.size.toLong() + occurrencesByOutcome[task.key]!!
                } else {
                    occurrencesByOutcome[task.key] = task.value.size.toLong()
                }
                if (durationByOutcome.contains(task.key)) {
                    durationByOutcome[task.key] = task.value.sumOf { it.duration } + durationByOutcome[task.key]!!
                } else {
                    durationByOutcome[task.key] = task.value.sumOf { it.duration }
                }
            }
    }
}
