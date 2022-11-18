package io.github.cdsap.geapi.domain.impl

import io.github.cdsap.geapi.domain.GetBuildScansWithFilter
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.domain.model.ScanWithAttributes
import io.github.cdsap.geapi.progressbar.ProgressBar
import io.github.cdsap.geapi.repository.GradleEnterpriseRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Semaphore
import kotlin.system.exitProcess

class GetBuildScansWithQueryImpl(private val repository: GradleEnterpriseRepository) : GetBuildScansWithFilter {

    override suspend fun get(filter: Filter): List<ScanWithAttributes> {
        println("Getting Build Scans")
        val semaphore = Semaphore(permits = filter.concurrentCalls)
        val buildScans = repository.getBuildScans(filter).filter { it.buildToolType == filter.buildSystem }
        val scans = mutableListOf<ScanWithAttributes>()
        if (buildScans.isNotEmpty()) {
            val progressBar = ProgressBar()
            progressBar.update(0, buildScans.size)
            var i = 0

            coroutineScope {
                val runningTasks = buildScans.map { sc ->
                    async {
                        semaphore.acquire()
                        val scan = repository.getBuildScanAttribute(sc.id)
                        progressBar.update(i++, buildScans.size)
                        semaphore.release()
                        scan
                    }
                }
                scans.addAll(runningTasks.awaitAll())
            }
            return scans.filter {
                filterBuildScans(it, filter)
            }
        } else {
            return emptyList()
        }
    }


    private fun filterBuildScans(scanWithAttributes: ScanWithAttributes, filter: Filter): Boolean {
        val filterProcessBuildScansFailed = if (filter.includeFailedBuilds) true else !scanWithAttributes.hasFailed
        val filterProject =
            if (filter.project == null) true else filter.project == scanWithAttributes.rootProjectName
        val filterTags = tagIsIncluded(filter.tags, scanWithAttributes.tags.toList())
        val filterTasks = if (filter.requestedTask == null) true else requestedTasksIncludeTask(
            scanWithAttributes.requestedTasks,
            filter.requestedTask!!
        )
        val filterUser = if (filter.user == null) true else scanWithAttributes.environment.username == filter.user
        return filterProcessBuildScansFailed && filterProject && filterTags && filterTasks && filterUser
    }

    private fun tagIsIncluded(filterTags: List<String>, buildTags: List<String>): Boolean {
        if (filterTags.isEmpty()) {
            return true
        }
        buildTags.forEach {
            if (filterTags.map { it.uppercase() }.contains(it.uppercase())) {
                return true
            }
        }
        return false
    }

    private fun requestedTasksIncludeTask(requestedTasks: Array<String>, task: String): Boolean {
        requestedTasks.forEach {
            if (it.contains(task)) return true
        }
        return false
    }
}
