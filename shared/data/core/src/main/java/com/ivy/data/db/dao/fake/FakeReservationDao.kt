package com.ivy.data.db.dao.fake

import com.ivy.data.db.dao.read.ReservationDao
import com.ivy.data.db.dao.write.WriteReservationDao
import com.ivy.data.db.entity.ReservationEntity
import org.jetbrains.annotations.VisibleForTesting
import java.util.UUID

@VisibleForTesting
class FakeReservationDao : ReservationDao, WriteReservationDao {
    private val items = mutableListOf<ReservationEntity>()

    override suspend fun findActive(): List<ReservationEntity> {
        return items.filter { !it.isDeleted && it.linkedTransactionId == null }
    }

    override suspend fun findActiveByCurrency(currencyCode: String): List<ReservationEntity> {
        return items.filter {
            !it.isDeleted && it.linkedTransactionId == null &&
                it.currencyCode == currencyCode
        }
    }

    override suspend fun findById(id: UUID): ReservationEntity? {
        return items.find { it.id == id }
    }

    override suspend fun findAll(): List<ReservationEntity> {
        return items.filter { !it.isDeleted }
    }

    override suspend fun save(value: ReservationEntity) {
        items.removeIf { it.id == value.id }
        items.add(value)
    }

    override suspend fun saveMany(values: List<ReservationEntity>) {
        values.forEach { save(it) }
    }

    override suspend fun deleteById(id: UUID) {
        items.removeIf { it.id == id }
    }

    override suspend fun deleteAll() {
        items.clear()
    }
}
