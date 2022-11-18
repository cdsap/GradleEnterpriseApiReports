package io.github.cdsap.geapi.report

import com.google.gson.Gson
import io.github.cdsap.geapi.domain.impl.GetBuildScansWithQueryImpl
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.repository.impl.GradleRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class BuildExtractorReport(
    val filter: Filter,
    val repository: GradleRepositoryImpl
) : Report {

    override suspend fun process() {
        val getBuildScans = GetBuildScansWithQueryImpl(repository).get(filter)

        val fileName = "${System.currentTimeMillis()}_builds.bundle"
        withContext(Dispatchers.IO) {
            val fw = FileWriter(File(fileName))
            val bw = BufferedWriter(fw)
            Gson().toJson(getBuildScans, bw)
            bw.close()
            if (File(fileName).exists()) {
                println("File $fileName created")
                val duration = System.currentTimeMillis() - filter.initFilter
                println("Duration: ${duration.toDuration(DurationUnit.MILLISECONDS)}")
            }
        }

    }
}
