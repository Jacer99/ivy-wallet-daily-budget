package com.ivy.domain.usecase.budget

import com.ivy.data.db.dao.read.DynamicBudgetConfigDao
import com.ivy.data.db.dao.write.WriteDynamicBudgetConfigDao
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomDynamicBudgetConfigStore @Inject constructor(
    private val readDao: DynamicBudgetConfigDao,
    private val writeDao: WriteDynamicBudgetConfigDao,
) : DynamicBudgetConfigStore {

    override suspend fun load(): DynamicBudgetConfig {
        return readDao.findFirstOrNull()?.let {
            DynamicBudgetConfigEntityMapper.toDomain(it)
        } ?: DynamicBudgetConfig.default()
    }

    override suspend fun save(config: DynamicBudgetConfig) {
        val existing = readDao.findFirstOrNull()
        val dateTime = existing?.dateTime ?: Instant.now()
        
        writeDao.deleteAll()
        writeDao.save(DynamicBudgetConfigEntityMapper.toEntity(config, dateTime))
    }
}
