package com.example.tubesmobdev.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tubesmobdev.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs WHERE creatorId = :userId ORDER BY createdAt")
    fun getAllSongs(userId: Long): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isLiked = 1 AND creatorId = :userId ORDER BY createdAt")
    fun getLikedSongs(userId: Long): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isDownloaded = 1 AND creatorId = :userId ORDER BY createdAt")
    fun getDownloadedSongs(userId: Long): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isDownloaded = 0 AND isOnline = 0 AND creatorId = :userId ORDER BY createdAt")
    fun getLocalSongs(userId: Long): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE creatorId = :userId AND isOnline = 0 ORDER BY createdAt DESC LIMIT 10")
    fun getNewestSongs(userId: Long): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE creatorId = :userId AND lastPlayed IS NOT NULL ORDER BY lastPlayed DESC LIMIT 10")
    fun getRecentlyPlayedSongs(userId: Long): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("UPDATE songs SET isLiked = :isLiked WHERE id = :songId")
    suspend fun updateLikedStatus(songId: Int, isLiked: Boolean)

    @Query("UPDATE songs SET lastPlayed = :timestamp WHERE id = :songId")
    suspend fun updateLastPlayed(songId: Int, timestamp: Long)

    @Update
    suspend fun updateSong(song: Song)

    @Query("SELECT * FROM songs WHERE LOWER(title) LIKE LOWER('%' || :query || '%') OR LOWER(artist) LIKE LOWER('%' || :query || '%')")
    fun searchSongs(query: String): Flow<List<Song>>

    @Query("SELECT COUNT(*) FROM songs WHERE creatorId = :userId AND lastPlayed IS NOT NULL")
    fun countPlayedSongs(userId: Long): Flow<Int>

    @Query("SELECT * FROM songs WHERE title = :title AND artist = :artist LIMIT 1")
    suspend fun findSongByTitleAndArtist(title: String, artist: String): Song?

    @Query("SELECT * FROM songs WHERE serverId = :serverId AND creatorId = :userId LIMIT 1")
    suspend fun findSongByServerId(serverId: Int, userId:Long): Song?

    @Query("SELECT * FROM songs WHERE id = :songId AND creatorId = :userId LIMIT 1")
    suspend fun getSongById(songId: Int, userId: Long): Song?

    @Query("""
    WITH favorite_artists AS (
        SELECT artist
        FROM listening_records
        WHERE creatorId = :userId
        GROUP BY artist
        ORDER BY SUM(durationListened) DESC
        LIMIT 5
    ),
    average_duration AS (
        SELECT AVG(durationListened) AS avg_duration
        FROM listening_records
        WHERE creatorId = :userId
    )
    SELECT * FROM songs
    WHERE artist IN (SELECT artist FROM favorite_artists)
    ORDER BY abs(hex(printf('%s%s', title, :hourSeed))) % 100
    LIMIT 20
""")
    suspend fun getRecommendedSongs(userId: Long, hourSeed: String): List<Song>

    @Query("""
    SELECT * FROM songs
    WHERE id NOT IN (
        SELECT songId FROM listening_records WHERE creatorId = :userId
    )
    ORDER BY abs(hex(printf('%s%s', title, :hourSeed))) % 100
    LIMIT 20
""")
    suspend fun getRandomRecommendedSongs(userId: Long, hourSeed: String): List<Song>


}
