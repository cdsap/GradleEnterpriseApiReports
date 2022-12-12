package io.github.cdsap.geapi.domain.impl

import io.github.cdsap.geapi.domain.GetMeasurements
import io.github.cdsap.geapi.domain.model.*
import io.github.cdsap.geapi.repository.GradleEnterpriseRepository

class GetMeasurementsImpl(val repository: GradleEnterpriseRepository) : GetMeasurements {
    override fun get(builds: List<Build>): List<Measurement> {

        val measurements = mutableListOf<Measurement>()
        measurements.addAll(GetGeneralMeasurements().get(builds))
        measurements.addAll(GetTaskMeasurements().get(builds))
        return measurements
    }
}
