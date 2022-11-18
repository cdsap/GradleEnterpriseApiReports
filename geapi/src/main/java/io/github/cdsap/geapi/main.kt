package io.github.cdsap.geapi

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.network.GEClient
import io.github.cdsap.geapi.report.*
import io.github.cdsap.geapi.repository.impl.GradleRepositoryImpl
import kotlinx.coroutines.runBlocking
import java.io.FileNotFoundException

fun main(args: Array<String>) {
    GEApi().main(args)
}

class GEApi : CliktCommand() {
    private val report: ReportConfig? by option().groupChoice(
        "taskOutcome" to TaskOutcome(),
        "experiment" to Experiment(),
        "buildExtractor" to BuildExtractor(),
        "buildReport" to BuildReport(),
        "buildAnalyzer" to BuildAnalyzer()
    )
    private val apiKey: String by option().required()
    private val url by option().required()
    private val maxBuilds by option().int().default(10)
    private val sinceBuildId: String? by option()
    private val experimentId: String? by option()
    private val project: String? by option()
    private val includeFailedBuilds: Boolean by option().flag(default = true)
    private val range by option().choice("week", "month").default("week")
    private val tags: List<String> by option().multiple(default = emptyList())
    private val type by option().help(" Task type used by the TaskOutcome report, example: org.jetbrains.kotlin.gradle.tasks.KotlinCompile")
    private val buildSupportFile by option().file()
    private val concurrentCalls by option().int().default(10)

    private val task: String? by option().help("[Optional] Task used for filter build scans. In the TaskOutcome report we may want to search only in specific builds, example: assembleDebug")
    private val user: String? by option()

    override fun run() {
        val filter = Filter(
            url = url,
            maxBuilds = maxBuilds,
            sinceBuildId = sinceBuildId,
            project = project,
            includeFailedBuilds = includeFailedBuilds,
            tags = tags,
            rangeFilter = range,
            taskType = type,
            requestedTask = task,
            initFilter = System.currentTimeMillis(),
            user = user,
            experimentId = experimentId,
            concurrentCalls = concurrentCalls
        )
        val repository = GradleRepositoryImpl(GEClient(apiKey, url))

        runBlocking {
            when (val it = report) {
                is TaskOutcome -> {
                    TaskOutcomeReport(
                        filter = filter, repository = repository
                    ).process()
                }
                is Experiment -> {
                    ExperimentReport(
                        filter = filter, repository = repository
                    ).process()
                }
                is BuildExtractor -> {
                    BuildExtractorReport(
                        filter = filter, repository = repository
                    ).process()
                }
                is BuildAnalyzer -> {
                    if (buildSupportFile == null) {
                        throw FileNotFoundException("File is required")
                    } else {
                        BuildAnalyzerReport(buildSupportFile!!, filter).process()
                    }
                }
                is BuildReport -> {
                    BuildReport(
                        filter = filter, repository = repository
                    ).process()
                }
                else -> {
                    println("Option not supported")
                }
            }
        }
    }
}

sealed class ReportConfig(name: String) : OptionGroup(name)
class TaskOutcome : ReportConfig("Task type duration and occurrences by Outcome. Label: taskOutcome")
class Experiment : ReportConfig("Generates Experiment Report")
class BuildExtractor : ReportConfig("Extracts builds")
class BuildReport : ReportConfig("Generates build report")
class BuildAnalyzer : ReportConfig("Analyze builds")
