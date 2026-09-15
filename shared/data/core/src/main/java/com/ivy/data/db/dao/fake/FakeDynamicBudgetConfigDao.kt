package com.ivy.data.db.dao.fake

import com.ivy.data.db.dao.read.DynamicBudgetConfigDao
import com.ivy.data.db.dao.write.WriteDynamicBudgetConfigDao
import com.ivy.data.db.entity.DynamicBudgetConfigEntity
import org.jetbrains.annotations.VisibleForTesting
import java.util.UUID

@VisibleForTesting
class FakeDynamicBudgetConfigDao : DynamicBudgetConfigDao, WriteDynamicBudgetConfigDao {
    private val items = mutableListOf<DynamicBudgetConfigEntity>()

    override suspend fun findFirstOrNull(): DynamicBudgetConfigEntity? {
        return items.find { !it.isDeleted }
    }

    override suspend fun findAll(): List<DynamicBudgetConfigEntity> {
        return items.filter { !it.isDeleted }
    }

    override suspend fun findById(id: UUID): DynamicBudgetConfigEntity? {
        return items.find { it.id == id }
    }

    override suspend fun save(value: DynamicBudgetConfigEntity) {
        items.removeIf { it.id == value.id }
        items.add(value)
    }

    override suspend fun saveMany(values: List<DynamicBudgetConfigEntity>) {
        values.forEach { save(it) }
    }

    override suspend fun deleteById(id: UUID) {
        items.removeIf { it.id == id }
    }

    override suspend fun deleteAll() {
        items.clear()
    }
}
