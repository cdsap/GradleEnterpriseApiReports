package io.github.cdsap.geapi.domain.model

import java.util.Calendar
import java.util.Date

data class Filter(
    val url: String,
    var maxBuilds: Int = 50,
    var range: Long? = null,
    val rangeFilter: String,
    var sinceBuildId: String? = null,
    val project: String? = null,
    val includeFailedBuilds: Boolean = false,
    var requestedTask: String? = null,
    var tags: List<String> = emptyList(),
    var taskType: String? = null,
    val initFilter: Long,
    val since: Long? = null,
    val user: String? = null,
    val experimentId: String? = null,
    val buildSystem: String = "gradle",
    val concurrentCalls: Int
) {
    init {
        range = if (sinceBuildId == null) {
            rangeFilter(rangeFilter)
        } else {
            null
        }
    }

    private fun rangeFilter(rangeFilter: String): Long {
        val cal: Calendar = Calendar.getInstance()
        if (experimentId != null) {
            cal.time = Date()
            cal.add(Calendar.HOUR, -1)
            return cal.timeInMillis
        } else {

            if (rangeFilter == "month") {
                cal.add(Calendar.MONTH, -1)
            } else {
                cal.add(Calendar.WEEK_OF_YEAR, -1)
            }
            return cal.timeInMillis
        }
    }
}
