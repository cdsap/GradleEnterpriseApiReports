package io.github.cdsap.geapi.domain.impl

import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.table
import io.github.cdsap.geapi.domain.PrintExperimentResults
import io.github.cdsap.geapi.domain.model.*
import io.github.cdsap.geapi.repository.GradleEnterpriseRepository

class PrintExperimentResultsImpl(private val repository: GradleEnterpriseRepository) : PrintExperimentResults {

    override suspend fun print(measurement: List<Measurement> {

        println(
            table
            {
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
                                if (it.variantA is Int) {
                                    if ((it.variantA as Int) - (it.variantB as Int) != 0) {
                                        val x = (it.variantB * 100) / it.variantA
                                        cell(x)
                                    } else {
                                        cell("")
                                    }

                                } else if (it.variantA is Long) {
                                    if ((it.variantA as Long) - (it.variantB as Long) != 0L) {
                                        val x = (it.variantB * 100L) / it.variantA
                                        cell(x)
                                    } else {
                                        cell("")
                                    }

                                } else if (it.variantA is Double) {
                                    if ((it.variantA as Double) - (it.variantB as Double) != 0.0) {
                                        val x = (it.variantB * 100.0) / it.variantA
                                        val result = 100 - x
                                        cell("$result%")
                                    } else {
                                        cell("")
                                    }
                                } else {

                                }


                            }
                        }
                    }
                }

            })
    }
}

