package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivy.base.kotlinxserilzation.KSerializerInstant
import com.ivy.base.kotlinxserilzation.KSerializerUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Keep
@Serializable
@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID,

    @SerialName("name")
    val name: String,

    @SerialName("amountMinorUnits")
    val amountMinorUnits: Long,

    @SerialName("currencyCode")
    val currencyCode: String,

    @SerialName("dueEpochDay")
    val dueEpochDay: Long?,

    @SerialName("categoryId")
    @Serializable(with = KSerializerUUID::class)
    val categoryId: UUID?,

    @SerialName("accountId")
    @Serializable(with = KSerializerUUID::class)
    val accountId: UUID?,

    @SerialName("linkedTransactionId")
    @Serializable(with = KSerializerUUID::class)
    val linkedTransactionId: UUID?,

    @SerialName("dateTime")
    @Serializable(with = KSerializerInstant::class)
    val dateTime: Instant,

    @Deprecated("Obsolete field used for cloud sync. Can't be deleted because of backwards compatibility")
    @SerialName("isSynced")
    val isSynced: Boolean = false,

    @Deprecated("Obsolete field used for cloud sync. Can't be deleted because of backwards compatibility")
    @SerialName("isDeleted")
    val isDeleted: Boolean = false
)
