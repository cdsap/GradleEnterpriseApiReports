package io.github.cdsap.geapi.report

import com.google.gson.Gson
import com.jakewharton.picnic.*
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.domain.model.ScanWithAttributes
import io.github.cdsap.geapi.view.BuildAnalyzerView
import org.nield.kotlinstatistics.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class BuildAnalyzerReport(
    private val file: File,
    val filter: Filter
) : Report {

    override suspend fun process() {
        val builds = getBuildsFromFile()
        BuildAnalyzerView(builds).print(filter)
    }

    private fun getBuildsFromFile(): Array<ScanWithAttributes> =
        Gson().fromJson(Files.newBufferedReader(Paths.get(file.path)), Array<ScanWithAttributes>::class.java)
}
