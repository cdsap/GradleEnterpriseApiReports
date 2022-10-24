package io.github.cdsap.geapi.view

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.geapi.domain.model.*

class ExperimentResultView
    : View<List<Measurement>> {

    override fun print(measurement: List<Measurement>) {

        println(table {
            cellStyle {
                border = true
                alignment = TextAlignment.MiddleLeft
                paddingLeft = 1
                paddingRight = 1
            }
            body {
                row {
                    cell("Experiment") {
                        columnSpan = 3
                        alignment = TextAlignment.MiddleCenter
                    }
                }

                measurement.groupBy {
                    it.OS
                }.forEach {
                    row {
                        cell(it.key.name) {
                            columnSpan = 3
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
                    it.value.filter {
                        it.diff() != ""
                    }.forEach {
                        row {
                            cell(it.category)
                            cell(it.name)
                            cell(it.variantA)
                            cell(it.variantB)
                            cell(it.diff()) {
                                alignment = TextAlignment.MiddleRight
                            }
                        }
                    }
                }
            }

        })
    }
}

