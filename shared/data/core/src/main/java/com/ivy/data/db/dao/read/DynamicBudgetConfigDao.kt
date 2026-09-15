package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.DynamicBudgetConfigEntity
import java.util.UUID

@Dao
interface DynamicBudgetConfigDao {
    @Query("SELECT * FROM dynamic_budget_config WHERE isDeleted = 0 LIMIT 1")
    suspend fun findFirstOrNull(): DynamicBudgetConfigEntity?

    @Query("SELECT * FROM dynamic_budget_config WHERE isDeleted = 0")
    suspend fun findAll(): List<DynamicBudgetConfigEntity>

    @Query("SELECT * FROM dynamic_budget_config WHERE id = :id")
    suspend fun findById(id: UUID): DynamicBudgetConfigEntity?
}