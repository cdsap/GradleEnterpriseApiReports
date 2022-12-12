package io.github.cdsap.geapi.view

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.geapi.domain.model.*

class ExperimentResultView
    : View<List<Measurement>> {

    private val LIMIT_DIFFERENCE_LONG = 1000L
    private val LIMIT_DIFFERENCE_INT = 1000

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
                        columnSpan = 5
                        alignment = TextAlignment.MiddleCenter
                    }
                }

                measurement.groupBy {
                    it.OS
                }.forEach {
                    row {
                        cell(it.key.name) {
                            columnSpan = 4
                            alignment = TextAlignment.MiddleCenter
                        }
                    }
                    row {
                        cell("Metric")
                        cell("VARIANT A")
                        cell("VARIANT B")
                        cell("Improvement")
                    }
                    it.value.forEach {
                        if (it.variantA is Long) {
                            if (it.variantA > LIMIT_DIFFERENCE_LONG || (it.variantB as Long) > LIMIT_DIFFERENCE_LONG) {
                                row {
                                    cell(it.name)
                                    cell(it.variantA)
                                    cell(it.variantB)
                                    cell(it.diff()) {
                                        alignment = TextAlignment.MiddleRight
                                    }
                                }
                            }
                        }
                        if (it.variantA is Int) {
                            if (it.variantA > LIMIT_DIFFERENCE_INT || (it.variantB as Int) > LIMIT_DIFFERENCE_INT) {
                                row {

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
                }
            }
        })
    }
}

