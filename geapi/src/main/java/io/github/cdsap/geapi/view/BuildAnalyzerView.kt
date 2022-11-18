package io.github.cdsap.geapi.view

import com.jakewharton.picnic.*
import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.domain.model.ScanWithAttributes
import org.nield.kotlinstatistics.percentile
import org.nield.kotlinstatistics.standardDeviation
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class BuildAnalyzerView(private val builds: Array<ScanWithAttributes>) : View<Filter> {

    override fun print(filter: Filter) {
        val buildsGrouped = groupBuildsByProjectName()

        println(
            table {
                cellStyle {
                    border = true
                    alignment = TextAlignment.MiddleLeft
                    paddingLeft = 2
                    paddingRight = 2
                }
                body {
                    printBuilds(buildsGrouped, this, this@table, filter)
                    printProjects(buildsGrouped, this, this@table)
                }
            })
    }

    private fun groupBuildsByProjectName() = builds
        .groupBy {
            it.rootProjectName
        }.map { it.key to it.value }

    private fun printProjects(
        sorted: List<Pair<String, List<ScanWithAttributes>>>,
        tableSectionDsl: TableSectionDsl,
        tableDsl: TableDsl
    ) {
        sorted.toList().sortedBy { (_, value) -> value.size }.toMap()
            .forEach {
                tableSectionDsl.header("Project ${it.key}")
                tableSectionDsl.headerTable("Requested task")
                it.value.groupBy { it.requestedTasks.joinToString(" ") }.forEach {
                    tableDsl.entry(it)
                }
            }
    }

    private fun printBuilds(
        sorted: List<Pair<String, List<ScanWithAttributes>>>,
        tableSectionDsl: TableSectionDsl,
        tableDsl: TableDsl,
        filter: Filter
    ) {
        tableSectionDsl.header("Builds by project in ${filter.url}")
        tableSectionDsl.headerTable("Project")
        sorted.toList().sortedBy { (_, value) -> value.size }.toMap()
            .forEach {
                tableDsl.entry(it)
            }
    }

    private fun TableSectionDsl.header(title: String) {
        row {
            cell(title) {
                columnSpan = 11
                alignment = TextAlignment.MiddleCenter

            }
        }
    }

    private fun TableDsl.entry(
        a: Map.Entry<String, List<ScanWithAttributes>>
    ) {
        if (a.key != null) {
            row {
                val mean = a.value.sumOf { it.buildDuration } / a.value.size
                cell(a.key.take(60))
                cell(a.value.size) {
                    alignment = TextAlignment.MiddleRight
                }
                cell(a.value.filter { it.tags.map { it.uppercase() }.contains("CI") }.count()) {
                    alignment = TextAlignment.MiddleRight
                }
                cell(a.value.filter { it.tags.map { it.uppercase() }.contains("LOCAL") }.count()) {
                    alignment = TextAlignment.MiddleRight
                }
                cell(mean.toDuration(DurationUnit.MILLISECONDS)) {
                    alignment = TextAlignment.MiddleRight
                }
                percentiles(a)
            }
        }
    }

    private fun RowDsl.percentiles(it: Map.Entry<String, List<ScanWithAttributes>>) {
        cell(it.value.map { it.buildDuration }.standardDeviation().toDuration(DurationUnit.MILLISECONDS).toRound(2)) {
            alignment = TextAlignment.MiddleRight
        }
        cell(it.value.map { it.buildDuration }.percentile(25.0).toDuration(DurationUnit.MILLISECONDS)) {
            alignment = TextAlignment.MiddleRight
        }
        cell(it.value.map { it.buildDuration }.percentile(50.0).toDuration(DurationUnit.MILLISECONDS)) {
            alignment = TextAlignment.MiddleRight
        }
        cell(it.value.map { it.buildDuration }.percentile(75.0).toDuration(DurationUnit.MILLISECONDS)) {
            alignment = TextAlignment.MiddleRight
        }
        cell(it.value.map { it.buildDuration }.percentile(90.0).toDuration(DurationUnit.MILLISECONDS)) {
            alignment = TextAlignment.MiddleRight
        }
        cell(it.value.map { it.buildDuration }.percentile(99.0).toDuration(DurationUnit.MILLISECONDS)) {
            alignment = TextAlignment.MiddleRight
        }
    }


    private fun TableSectionDsl.headerTable(title: String) = row {
        cell(title)
        cell("Builds") {
            alignment = TextAlignment.MiddleCenter
        }
        cell("CI") {
            alignment = TextAlignment.MiddleCenter
        }
        cell("Local") {
            alignment = TextAlignment.MiddleCenter
        }
        cell("Mean") {
            alignment = TextAlignment.MiddleCenter
        }
        cell("Standard deviation") {
            alignment = TextAlignment.MiddleCenter
        }
        cell("P25") {
            alignment = TextAlignment.MiddleCenter
        }
        cell("P50") {
            alignment = TextAlignment.MiddleCenter
        }
        cell("P75") {
            alignment = TextAlignment.MiddleCenter
        }
        cell("P90") {
            alignment = TextAlignment.MiddleCenter
        }
        cell("P99") {
            alignment = TextAlignment.MiddleCenter
        }
    }
}

fun Double.roundTo(numFractionDigits: Int): Double {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this * factor).roundToInt() / factor
}

