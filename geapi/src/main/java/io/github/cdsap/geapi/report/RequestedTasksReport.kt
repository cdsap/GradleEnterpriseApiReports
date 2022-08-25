package io.github.cdsap.geapi.report

import io.github.cdsap.geapi.domain.impl.GetBuildScansWithQueryImpl
import io.github.cdsap.geapi.domain.impl.GetRequestedTasksImpl
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.repository.GradleEnterpriseRepository
import io.github.cdsap.geapi.view.RequestedTasksView

class RequestedTasksReport(
    val filter: Filter,
    val repository: GradleEnterpriseRepository
) {
    suspend fun process() {
        val getBuildScans = GetBuildScansWithQueryImpl(repository).get(filter)
        val requestedTasks = GetRequestedTasksImpl().get(getBuildScans, filter.tags)
        RequestedTasksView(requestedTasks).print(filter)
    }
}
