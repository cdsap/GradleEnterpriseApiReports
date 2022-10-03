package io.github.cdsap.geapi.domain.model

import java.util.Calendar
import java.util.Date

data class Filter(
    val url: String,
    val maxBuilds: Int,
    var range: Long? = null,
    val rangeFilter: String,
    val sinceBuildId: String? = null,
    val project: String? = null,
    val includeFailedBuilds: Boolean = false,
    var requestedTask: String? = null,
    var tags: List<String> = emptyList(),
    var taskType: String? = null,
    val initFilter: Long,
    val since: Long? = null,
    val user: String? = null
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
        cal.time = Date()
        cal.add(Calendar.HOUR, -2)
//        if (rangeFilter == "month") {
//            cal.add(Calendar.MONTH, -1)
//        } else {
//            cal.add(Calendar.WEEK_OF_YEAR, -1)
//        }
        return cal.timeInMillis
    }
}
