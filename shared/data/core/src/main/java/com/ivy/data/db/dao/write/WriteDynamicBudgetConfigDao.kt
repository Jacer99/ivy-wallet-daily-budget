package com.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ivy.data.db.entity.DynamicBudgetConfigEntity
import java.util.UUID

@Dao
interface WriteDynamicBudgetConfigDao {
    @Upsert
    suspend fun save(value: DynamicBudgetConfigEntity)

    @Upsert
    suspend fun saveMany(value: List<DynamicBudgetConfigEntity>)

    @Query("DELETE FROM dynamic_budget_config WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("DELETE FROM dynamic_budget_config")
    suspend fun deleteAll()
}