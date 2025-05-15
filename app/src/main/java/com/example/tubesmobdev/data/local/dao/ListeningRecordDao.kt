package com.example.tubesmobdev.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tubesmobdev.data.model.ListeningRecord
import com.example.tubesmobdev.data.model.StreakEntry
import com.example.tubesmobdev.data.model.TopArtist
import com.example.tubesmobdev.data.model.TopSong
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
      WITH top_artist AS (
        SELECT artist
        FROM listening_records
        WHERE creatorId = :userId
        GROUP BY artist
        ORDER BY SUM(durationListened) DESC
        LIMIT 1
      )
      SELECT
        ta.artist    AS artist,
        s.coverUrl   AS coverUrl
      FROM top_artist ta
      JOIN songs s
        ON s.artist = ta.artist
      LIMIT 1
    """)
    fun getTopArtist(userId: Long): Flow<TopArtist>

    @Query("""
      WITH top_song AS (
        SELECT songId
        FROM listening_records
        WHERE creatorId = :userId
        GROUP BY songId
        ORDER BY SUM(durationListened) DESC
        LIMIT 1
      )
      SELECT
        s.title      AS title,
        s.coverUrl   AS coverUrl
      FROM top_song ts
      JOIN songs s
        ON s.id = ts.songId
      LIMIT 1
    """)
    fun getTopSong(userId: Long): Flow<TopSong>

    @Query("""
      SELECT songId, title, date 
      FROM listening_records 
      WHERE creatorId = :userId 
      ORDER BY songId, date
    """)
    suspend fun getRecordsForStreakAnalysis(userId: Long): List<StreakEntry>
}