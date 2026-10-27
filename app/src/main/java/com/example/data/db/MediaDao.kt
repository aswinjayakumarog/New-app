package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

  @Query("SELECT * FROM tracked_media ORDER BY addedAt DESC")
  fun getAllTrackedMedia(): Flow<List<MediaItemEntity>>

  @Query("SELECT * FROM tracked_media WHERE mediaType = :mediaType ORDER BY addedAt DESC")
  fun getTrackedMediaByType(mediaType: String): Flow<List<MediaItemEntity>>

  @Query("SELECT * FROM tracked_media WHERE status = :status ORDER BY addedAt DESC")
  fun getTrackedMediaByStatus(status: String): Flow<List<MediaItemEntity>>

  @Query("SELECT * FROM tracked_media WHERE id = :id AND mediaType = :mediaType LIMIT 1")
  fun getMediaById(id: Int, mediaType: String): Flow<MediaItemEntity?>

  @Query("SELECT * FROM tracked_media WHERE id = :id AND mediaType = :mediaType LIMIT 1")
  suspend fun getMediaByIdDirect(id: Int, mediaType: String): MediaItemEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertMedia(media: MediaItemEntity)

  @Query("DELETE FROM tracked_media WHERE id = :id AND mediaType = :mediaType")
  suspend fun deleteMedia(id: Int, mediaType: String)

  @Query("UPDATE tracked_media SET status = :status, lastWatchedAt = :lastWatchedAt WHERE id = :id AND mediaType = :mediaType")
  suspend fun updateStatus(id: Int, mediaType: String, status: String, lastWatchedAt: Long?)

  @Query("UPDATE tracked_media SET userRating = :rating WHERE id = :id AND mediaType = :mediaType")
  suspend fun updateRating(id: Int, mediaType: String, rating: Int)

  @Query("UPDATE tracked_media SET isFavorite = :isFavorite WHERE id = :id AND mediaType = :mediaType")
  suspend fun updateFavorite(id: Int, mediaType: String, isFavorite: Boolean)

  @Query("UPDATE tracked_media SET userNotes = :notes WHERE id = :id AND mediaType = :mediaType")
  suspend fun updateNotes(id: Int, mediaType: String, notes: String?)

  // Episode tracking queries
  @Query("SELECT * FROM watched_episodes WHERE showId = :showId ORDER BY seasonNumber ASC, episodeNumber ASC")
  fun getWatchedEpisodesForShow(showId: Int): Flow<List<WatchedEpisodeEntity>>

  @Query("SELECT * FROM watched_episodes WHERE showId = :showId ORDER BY seasonNumber ASC, episodeNumber ASC")
  suspend fun getWatchedEpisodesForShowDirect(showId: Int): List<WatchedEpisodeEntity>

  @Query("SELECT * FROM watched_episodes ORDER BY watchedAt DESC")
  fun getAllWatchedEpisodes(): Flow<List<WatchedEpisodeEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWatchedEpisode(episode: WatchedEpisodeEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWatchedEpisodes(episodes: List<WatchedEpisodeEntity>)

  @Query("DELETE FROM watched_episodes WHERE showId = :showId AND seasonNumber = :seasonNumber AND episodeNumber = :episodeNumber")
  suspend fun deleteWatchedEpisode(showId: Int, seasonNumber: Int, episodeNumber: Int)

  @Query("DELETE FROM watched_episodes WHERE showId = :showId AND seasonNumber = :seasonNumber")
  suspend fun deleteWatchedEpisodesForSeason(showId: Int, seasonNumber: Int)

  @Query("DELETE FROM watched_episodes WHERE showId = :showId")
  suspend fun deleteWatchedEpisodesForShow(showId: Int)

  // Custom lists
  @Query("SELECT * FROM custom_lists ORDER BY createdAt DESC")
  fun getAllCustomLists(): Flow<List<CustomListEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomList(list: CustomListEntity): Long

  @Query("DELETE FROM custom_lists WHERE id = :id")
  suspend fun deleteCustomList(id: Long)

  @Query("DELETE FROM tracked_media")
  suspend fun clearAllMedia()

  @Query("DELETE FROM watched_episodes")
  suspend fun clearAllEpisodes()
}
