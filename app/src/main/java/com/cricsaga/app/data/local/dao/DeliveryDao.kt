package com.cricsaga.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cricsaga.app.data.local.entity.DeliveryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: DeliveryEntity)

    @Query("DELETE FROM deliveries WHERE deliveryId = :deliveryId")
    suspend fun deleteDelivery(deliveryId: String)

    @Query("SELECT * FROM deliveries WHERE inningsId = :inningsId ORDER BY deliveryIndex ASC")
    fun getDeliveriesForInnings(inningsId: String): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE inningsId = :inningsId ORDER BY deliveryIndex ASC")
    suspend fun getDeliveriesListForInnings(inningsId: String): List<DeliveryEntity>

    @Query("SELECT * FROM deliveries WHERE inningsId = :inningsId ORDER BY deliveryIndex DESC LIMIT 1")
    suspend fun getLastDelivery(inningsId: String): DeliveryEntity?

    @Query("DELETE FROM deliveries WHERE inningsId = :inningsId AND deliveryIndex = (SELECT MAX(deliveryIndex) FROM deliveries WHERE inningsId = :inningsId)")
    suspend fun deleteLastDelivery(inningsId: String): Int
}
