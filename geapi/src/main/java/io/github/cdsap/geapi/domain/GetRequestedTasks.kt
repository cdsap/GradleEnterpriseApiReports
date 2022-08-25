package io.github.cdsap.geapi.domain

import io.github.cdsap.geapi.domain.model.RequestedTasks
import io.github.cdsap.geapi.domain.model.ScanWithAttributes

interface GetRequestedTasks {

    suspend fun get(builds: List<ScanWithAttributes>, tags: List<String>): RequestedTasks
}
