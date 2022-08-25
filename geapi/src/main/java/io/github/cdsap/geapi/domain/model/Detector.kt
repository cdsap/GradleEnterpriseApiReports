package io.github.cdsap.geapi.domain.model

data class Detector(
    val task: String,
    val remoteTime: Long,
    val executeTime: Long,
    val remoteTimes: Long,
    val executedTimes: Long
)
