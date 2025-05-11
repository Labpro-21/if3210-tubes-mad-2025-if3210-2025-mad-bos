package com.example.tubesmobdev.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tubesmobdev.data.model.ListeningRecord
import com.example.tubesmobdev.data.model.StreakEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ListeningRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ListeningRecord)

    @Query("SELECT * FROM listening_records WHERE creatorId = :userId ORDER BY date DESC")
    fun getAllRecords(userId: Long): Flow<List<ListeningRecord>>

    @Query("SELECT SUM(durationListened) FROM listening_records WHERE creatorId = :userId")
    fun getTotalListeningTime(userId: Long): Flow<Long?>

    @Query("""
        SELECT artist 
        FROM listening_records 
        WHERE creatorId = :userId 
        GROUP BY artist 
        ORDER BY SUM(durationListened) DESC 
        LIMIT 1
    """)
    fun getTopArtist(userId: Long): Flow<String>

    @Query("""
        SELECT title 
        FROM listening_records 
        WHERE creatorId = :userId 
        GROUP BY title 
        ORDER BY SUM(durationListened) DESC 
        LIMIT 1
    """)
    fun getTopSong(userId: Long): Flow<String>

    @Query("""
        SELECT songId, title, date 
        FROM listening_records 
        WHERE creatorId = :userId 
        ORDER BY songId, date
    """)
    suspend fun getRecordsForStreakAnalysis(userId: Long): List<StreakEntry>
}