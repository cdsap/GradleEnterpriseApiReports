package io.github.cdsap.geapi.domain.impl

import io.github.cdsap.geapi.domain.GetBuildScansWithQuery
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.domain.model.ScanWithAttributes
import io.github.cdsap.geapi.progressbar.ProgressBar
import io.github.cdsap.geapi.repository.GradleEnterpriseRepository

class GetBuildScansWithQueryImpl(private val repository: GradleEnterpriseRepository) : GetBuildScansWithQuery {

    override suspend fun get(filter: Filter): List<ScanWithAttributes> {
        println("Getting Build Scans")
        val buildScans = repository.getBuildScans(filter).filter { it.buildToolType == "gradle" }

        println("Processing build scan attributes")
        if (buildScans.isNotEmpty()) {
            val progressBar = ProgressBar()
            progressBar.update(0, buildScans.size)
            var i = 0
            val buildScansWithAttributes = buildScans
                .map {
                    progressBar.update(i++, buildScans.size)
                    repository.getBuildScanAttribute(it.id)
                }
            return buildScansWithAttributes
                .filter {
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
