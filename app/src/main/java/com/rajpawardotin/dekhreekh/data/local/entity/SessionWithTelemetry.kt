package com.rajpawardotin.dekhreekh.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SessionWithTelemetry(
    @Embedded val session: SessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val telemetry: List<TelemetryEntity>
)
