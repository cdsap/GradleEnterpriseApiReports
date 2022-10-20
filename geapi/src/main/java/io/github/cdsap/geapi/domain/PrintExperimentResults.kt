package io.github.cdsap.geapi.domain

import io.github.cdsap.geapi.domain.model.Filter
import io.github.cdsap.geapi.domain.model.Measurement
import io.github.cdsap.geapi.domain.model.ScanWithAttributes

interface PrintExperimentResults {

    fun print(measurements: List<Measurement>)
}
