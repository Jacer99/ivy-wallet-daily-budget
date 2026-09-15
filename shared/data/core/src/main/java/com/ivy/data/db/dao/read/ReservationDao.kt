package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.ReservationEntity
import java.util.UUID

@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations WHERE isDeleted = 0 AND linkedTransactionId IS NULL")
    suspend fun findActive(): List<ReservationEntity>

    @Query("SELECT * FROM reservations WHERE isDeleted = 0 AND linkedTransactionId IS NULL AND currencyCode = :currencyCode")
    suspend fun findActiveByCurrency(currencyCode: String): List<ReservationEntity>

    @Query("SELECT * FROM reservations WHERE id = :id")
    suspend fun findById(id: UUID): ReservationEntity?

    @Query("SELECT * FROM reservations WHERE isDeleted = 0")
    suspend fun findAll(): List<ReservationEntity>
}
