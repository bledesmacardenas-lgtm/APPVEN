package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.StoreSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreSettingsDao {
    @Query("SELECT * FROM store_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<StoreSettingsEntity?>

    @Query("SELECT * FROM store_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): StoreSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: StoreSettingsEntity)

    @Update
    suspend fun update(settings: StoreSettingsEntity)
}
