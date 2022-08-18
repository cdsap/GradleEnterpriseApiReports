package io.github.cdsap.geapi.domain.impl

import io.github.cdsap.geapi.domain.GetRequestedTasks
import io.github.cdsap.geapi.domain.model.RequestedTasks
import io.github.cdsap.geapi.domain.model.ScanWithAttributes
import io.github.cdsap.geapi.progressbar.ProgressBar

class GetRequestedTasksImpl : GetRequestedTasks {

    override suspend fun get(builds: List<ScanWithAttributes>, tags: List<String>): RequestedTasks {
        val requestedTasks = RequestedTasks()
        println("Processing build scan requested tasks")
        if (builds.isNotEmpty()) {
            val progressBar = ProgressBar()
            progressBar.update(0, builds.size)
            var i = 0
            builds.forEach { build ->
                processTasks(build, requestedTasks, tags)
                processTasksByTag(build, requestedTasks, tags)
                progressBar.update(i++, builds.size)
            }
        }
        return requestedTasks
    }

    private fun processTasksByTag(
        build: ScanWithAttributes,
        requestedTasks: RequestedTasks,
        tags: List<String>
    ) {
        build.tags.filter { tag ->
            isTagIncluded(tags, tag)
        }.forEach { tag ->
            if (requestedTasks.occurrencesTasksByTask.contains(tag)) {
                val task = build.requestedTasks.joinToString(separator = " ")
                if (requestedTasks.occurrencesTasksByTask[tag]!!.contains(task)) {
                    val temp = requestedTasks.occurrencesTasksByTask[tag]!![task]!! + 1
                    requestedTasks.occurrencesTasksByTask[tag]!![task] = temp
                } else {
                    requestedTasks.occurrencesTasksByTask[tag]!![task] = 1
                }
            } else {
                requestedTasks.occurrencesTasksByTask[tag] = mutableMapOf()
                val task = build.requestedTasks.joinToString(separator = " ")
                requestedTasks.occurrencesTasksByTask[tag]!![task] = 1
            }
        }
    }

    private fun processTasks(
        build: ScanWithAttributes,
        requestedTasks: RequestedTasks,
        tags: List<String>
    ) {
        val task = build.requestedTasks.joinToString(separator = " ")

        if (requestedTasks.occurrencesTasks.contains(task)) {
            build.tags.filter { tag ->
                isTagIncluded(tags, tag)
            }.forEach { tag ->
                if (requestedTasks.occurrencesTasks[task]!!.contains(tag)) {
                    val temp = requestedTasks.occurrencesTasks[task]!![tag]!! + 1
                    requestedTasks.occurrencesTasks[task]!![tag] = temp
                } else {
                    requestedTasks.occurrencesTasks[task]!![tag] = 1
                }
            }
        } else {
            requestedTasks.occurrencesTasks[task] = mutableMapOf()
            build.tags.filter { tag ->
                isTagIncluded(tags, tag)
            }.forEach { tag ->
                requestedTasks.occurrencesTasks[task]!![tag] = 1
            }
        }
    }

    private fun isTagIncluded(tags: List<String>, tag: String) =
        tags.map { it.uppercase() }.contains(tag.uppercase())
}
