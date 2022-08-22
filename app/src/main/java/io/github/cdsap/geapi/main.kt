package io.github.cdsap.geapi

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.OptionGroup
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
import io.github.cdsap.geapi.report.TaskOutcomeReport
import io.github.cdsap.geapi.repository.impl.GradleRepositoryImpl
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    GEApi().main(args)
}

class GEApi : CliktCommand() {
    private val apiKey by option().file(mustExist = true).required()
    private val url by option().required()
    private val maxBuilds by option().int().default(10)
    private val sinceBuildId: String? by option()
    private val project: String? by option()
    private val includeFailedBuilds: Boolean by option().flag(default = false)
    private val range by option().choice("week", "month").default("week")
    private val tags: List<String> by option().multiple(default = listOf("ci", "local"))
    private val type by option().help(" Task type used by the TaskOutcome report, example: org.jetbrains.kotlin.gradle.tasks.KotlinCompile")

    private val task: String? by option().help("[Optional] Task used for filter build scans. In the TaskOutcome report we may want to search only in specific builds, example: assembleDebug")

    override fun run() {

        val repository = GradleRepositoryImpl(GEClient(apiKey.readText(), url))

        runBlocking {
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
                initFilter = System.currentTimeMillis()
            )

            TaskOutcomeReport(
                filter = filter,
                repository = repository
            ).process()
        }
    }
}

sealed class ReportConfig(name: String) : OptionGroup(name)
class TaskOutcome : ReportConfig("Task type duration and occurrences by Outcome. Label: taskOutcome")
class TaskOccurrences : ReportConfig("Tasks Occurrences: Label: taskOccurrences")
