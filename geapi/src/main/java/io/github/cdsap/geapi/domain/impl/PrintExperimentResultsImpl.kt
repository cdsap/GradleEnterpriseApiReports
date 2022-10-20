package io.github.cdsap.geapi.domain.impl

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.geapi.domain.PrintExperimentResults
import io.github.cdsap.geapi.domain.model.*

class PrintExperimentResultsImpl
    : PrintExperimentResults {

    override fun print(measurement: List<Measurement>) {

        println(table {
            cellStyle {
                border = true
                alignment = TextAlignment.MiddleLeft
                paddingLeft = 2
                paddingRight = 2
            }
            body {
                row {
                    cell("Experiment") {
                        columnSpan = 4
                        alignment = TextAlignment.MiddleCenter
                    }
                }

                measurement.groupBy {
                    it.OS
                }.forEach {
                    row {
                        cell(it.key.name) {
                            columnSpan = 5
                            alignment = TextAlignment.MiddleCenter
                        }
                    }
                    row {
                        cell("Category")
                        cell("Metric")
                        cell("VARIANT A")
                        cell("VARIANT B")
                        cell("Delta")
                    }
                    it.value.forEach {
                        row {
                            cell(it.category)
                            cell(it.name)
                            cell(it.variantA)
                            cell(it.variantB)
                            cell(it.diff())
                        }
                    }
                }
            }

        })
    }
}

