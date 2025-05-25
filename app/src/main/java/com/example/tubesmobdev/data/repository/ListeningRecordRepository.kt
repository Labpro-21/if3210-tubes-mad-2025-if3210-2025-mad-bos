package com.example.tubesmobdev.data.repository

import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.tubesmobdev.data.local.dao.ListeningRecordDao
import com.example.tubesmobdev.data.local.preferences.IAuthPreferences
import com.example.tubesmobdev.data.model.ListeningRecord
import com.example.tubesmobdev.data.model.StreakEntry
import com.example.tubesmobdev.data.model.TopArtist
import com.example.tubesmobdev.data.model.TopSong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ListeningRecordRepository @Inject constructor(
    private val dao: ListeningRecordDao,
    private val authPreferences: IAuthPreferences
) {
    suspend fun insertRecord(record: ListeningRecord): Result<Unit> {
        return try {
            Log.d("debug", "insertRecord: "+ record.title)
            dao.insertRecord(record)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllRecords(): Flow<List<ListeningRecord>> {
        val userId = authPreferences.getUserId()
        return if (userId != null) {
            dao.getAllRecords(userId)
        } else {
            flowOf(emptyList())
        }
    }

    suspend fun getTotalListeningTime(): Flow<Long> {
        val userId = authPreferences.getUserId()
        return if (userId != null) {
            dao.getTotalListeningTime(userId)
                .map { it ?: 0L }
        } else {
            flowOf(0L)
        }
    }
    suspend fun getTopArtistLastYear(): Flow<List<TopArtist>> {
        val userId = authPreferences.getUserId()
        return if (userId != null) {
            dao.getTopArtistLastYear(userId)
        } else {
            flowOf(emptyList())
        }
    }

    suspend fun getTopSongLastYear(): Flow<List<TopSong>> {
        val userId = authPreferences.getUserId()
        return if (userId != null) {
            dao.getTopSongLastYear(userId)
        } else {
            flowOf(emptyList())
        }
    }

    suspend fun getMonthlyTopArtistsFor(monthYear: String): Flow<List<TopArtist>> {
        Log.d("debug", "getMonthlyTopArtistsFor: "+monthYear)
        val userId = authPreferences.getUserId() ?: return flowOf(emptyList())
        return dao.getMonthlyTopArtist(userId, monthYear)
    }

    suspend fun getMonthlyTopSongsFor(monthYear: String): Flow<List<TopSong>> {
        val userId = authPreferences.getUserId() ?: return flowOf(emptyList())
        return dao.getMonthlyTopSong(userId, monthYear)
    }

    suspend fun getDailyListeningMinutes(month: String): List<Pair<Int, Int>> {
        val userId = authPreferences.getUserId()
        if (userId == null) return emptyList() // ✅ handle null userId

        val entries = dao.getDailyListeningMinutes(userId, month)
        return entries.map { it.day.toInt() to it.minutes }
    }

    suspend fun getMonthlyTopStreak(): Flow<List<StreakEntry>> {
        val sql = """
            WITH
              monthly_records AS (
                SELECT
                  songId,
                  title,
                  date(date)             AS date,
                  strftime('%Y-%m',date) AS monthYear
                FROM listening_records
                WHERE
                  creatorId = ? 
                  AND date(date) >= date('now','-1 year')
              ),
              numbered AS (
                SELECT
                  songId,
                  title,
                  date,
                  monthYear,
                  ROW_NUMBER() OVER (PARTITION BY songId, monthYear ORDER BY date) AS rn
                FROM monthly_records
              ),
              grouped AS (
                SELECT
                  songId,
                  title,
                  date,
                  monthYear,
                  CAST(julianday(date) AS INTEGER) - rn AS grp
                FROM numbered
              ),
              streaks AS (
                SELECT
                  songId,
                  title,
                  monthYear,
                  MIN(date) AS startDate,
                  MAX(date) AS endDate,
                  COUNT(*)  AS days
                FROM grouped
                GROUP BY songId, monthYear, grp
                HAVING days >= 3
              ),
              top_per_month AS (
                SELECT
                  monthYear,
                  songId,
                  title,
                  startDate,
                  endDate,
                  days,
                  ROW_NUMBER() OVER (PARTITION BY monthYear ORDER BY days DESC) AS rn
                FROM streaks
              )
            SELECT
              monthYear,
              songId,
              title,
              startDate,
              endDate,
              days
            FROM top_per_month
            WHERE rn = 1
            ORDER BY monthYear DESC
          """.trimIndent()
        val userId = authPreferences.getUserId() ?: return flowOf(emptyList())
        val query = SimpleSQLiteQuery(sql, arrayOf(userId))
        return try {
            Log.d("debug", "getMonthlyTopStreak: ")
            dao.getMonthlyTopStreakRaw(query)
        } catch (e: SQLiteException) {
            Log.e("Repo", "RawQuery gagal – kemungkinan window function nggak didukung", e)
            flowOf(emptyList())
        }
    }
}