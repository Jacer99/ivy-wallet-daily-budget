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
@Entity(tableName = "dynamic_budget_config")
data class DynamicBudgetConfigEntity(
    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID,

    @SerialName("periodTypeTag")
    val periodTypeTag: String,

    @SerialName("salaryPayday")
    val salaryPayday: Int?,

    @SerialName("customStartEpochDay")
    val customStartEpochDay: Long?,

    @SerialName("customEndEpochDay")
    val customEndEpochDay: Long?,

    @SerialName("budgetLimitMinorUnits")
    val budgetLimitMinorUnits: Long,

    @SerialName("currencyCode")
    val currencyCode: String,

    @SerialName("includedAccountIdsSerialized")
    val includedAccountIdsSerialized: String?,

    @SerialName("includedCategoryIdsSerialized")
    val includedCategoryIdsSerialized: String?,

    @SerialName("includeIncomeInBudget")
    val includeIncomeInBudget: Boolean,

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