package com.example.tubesmobdev.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.tubesmobdev.data.model.DailyListeningEntry
import com.example.tubesmobdev.data.model.ListeningRecord
import com.example.tubesmobdev.data.model.TopArtist
import com.example.tubesmobdev.data.model.TopSong
import com.example.tubesmobdev.data.model.StreakEntry
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ListeningRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRecord(record: ListeningRecord)

    @Query("SELECT * FROM listening_records WHERE creatorId = :userId ORDER BY date DESC")
    abstract fun getAllRecords(userId: Long): Flow<List<ListeningRecord>>

    @Query("SELECT SUM(durationListened) FROM listening_records WHERE creatorId = :userId")
    abstract fun getTotalListeningTime(userId: Long): Flow<Long?>

    @Query(
        """
    SELECT
      strftime('%Y-%m', date) AS monthYear,
      (
        SELECT artist
        FROM listening_records lr2
        WHERE
          lr2.creatorId = :userId
          AND strftime('%Y-%m', lr2.date) = strftime('%Y-%m', date)
        GROUP BY lr2.artist
        ORDER BY SUM(lr2.durationListened) DESC
        LIMIT 1
      ) AS artist,
      (
        SELECT s.coverUrl
        FROM listening_records lr3
        JOIN songs s ON s.artist = lr3.artist
        WHERE
          lr3.creatorId = :userId
          AND strftime('%Y-%m', lr3.date) = strftime('%Y-%m', date)
        GROUP BY lr3.artist
        ORDER BY SUM(lr3.durationListened) DESC
        LIMIT 1
      ) AS coverUrl,
      (
        SELECT SUM(lr4.durationListened)
        FROM listening_records lr4
        WHERE
          lr4.creatorId = :userId
          AND lr4.artist = (
            SELECT artist
            FROM listening_records lr5
            WHERE
              lr5.creatorId = :userId
              AND strftime('%Y-%m', lr5.date) = strftime('%Y-%m', date)
            GROUP BY lr5.artist
            ORDER BY SUM(lr5.durationListened) DESC
            LIMIT 1
          )
          AND strftime('%Y-%m', lr4.date) = strftime('%Y-%m', date)
      ) AS playCount
    FROM listening_records
    WHERE
      creatorId = :userId
      AND date(date) >= date('now', '-1 year')
    GROUP BY strftime('%Y-%m', date)
    ORDER BY monthYear DESC
    """
    )
    abstract fun getTopArtistLastYear(userId: Long): Flow<List<TopArtist>>

    @Query("""
    SELECT 
        strftime('%d', date) AS day,
        SUM(durationListened) / 60000 AS minutes
    FROM listening_records
    WHERE creatorId = :userId AND strftime('%Y-%m', date) = :monthYear
    GROUP BY day
    ORDER BY day
""")
    abstract suspend fun getDailyListeningMinutes(
        userId: Long,
        monthYear: String
    ): List<DailyListeningEntry>

    @Query(
        """
    SELECT
      strftime('%Y-%m', lr.date) AS monthYear,
      lr.title AS title,
      lr.artist AS artist,
      lr.coverUrl AS coverUrl,
      COUNT(*) AS playCount
    FROM listening_records lr
    WHERE
      lr.creatorId = :userId
      AND date(lr.date) >= date('now', '-1 year')
    GROUP BY strftime('%Y-%m', lr.date), lr.title, lr.artist
    ORDER BY monthYear DESC
    """
    )
    abstract fun getTopSongLastYear(userId: Long): Flow<List<TopSong>>

    @Query("""
    SELECT
      strftime('%Y-%m', lr.date)           AS monthYear,
      lr.artist                             AS artist,
      (SELECT s.coverUrl
         FROM songs s
         WHERE s.artist = lr.artist
         LIMIT 1)                           AS coverUrl,
      COUNT(*)                             AS playCount
    FROM listening_records lr
    WHERE
      lr.creatorId = :userId
      AND strftime('%Y-%m', lr.date) = :monthYear
    GROUP BY lr.artist
    ORDER BY playCount DESC
  """)
    abstract fun getMonthlyTopArtist(
        userId: Long,
        monthYear: String
    ): Flow<List<TopArtist>>

    @Query(
        """
    SELECT
      strftime('%Y-%m', lr.date) AS monthYear,
      lr.title AS title,
      lr.artist AS artist,
      lr.coverUrl AS coverUrl,
      COUNT(*) AS playCount
    FROM listening_records lr
    WHERE
      lr.creatorId = :userId
      AND strftime('%Y-%m', lr.date) = :monthYear
    GROUP BY lr.title, lr.artist
    ORDER BY playCount DESC
    """
    )
    abstract fun getMonthlyTopSong(userId: Long, monthYear: String): Flow<List<TopSong>>
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(record: ListeningRecord): Long

    @Update
    abstract suspend fun update(record: ListeningRecord)

    @Query("SELECT * FROM listening_records WHERE sessionId = :sessionId LIMIT 1")
    abstract suspend fun getBySessionId(sessionId: String): ListeningRecord?

    @Query("UPDATE listening_records SET durationListened = durationListened + :increment WHERE sessionId = :sessionId")
    abstract suspend fun incrementDurationBySessionId(sessionId: String, increment: Long)


    @RawQuery(observedEntities = [ListeningRecord::class])
    abstract fun getMonthlyTopStreakRaw(query: SupportSQLiteQuery): Flow<List<StreakEntry>>
}
