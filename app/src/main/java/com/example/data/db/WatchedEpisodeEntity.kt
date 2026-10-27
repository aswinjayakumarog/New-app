package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "watched_episodes",
  indices = [
    Index(value = ["showId", "seasonNumber", "episodeNumber"], unique = true),
    Index(value = ["showId"])
  ]
)
data class WatchedEpisodeEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val showId: Int,
  val seasonNumber: Int,
  val episodeNumber: Int,
  val episodeName: String? = null,
  val stillPath: String? = null,
  val airDate: String? = null,
  val runtime: Int? = null, // in minutes
  val watchedAt: Long = System.currentTimeMillis(),
  val userRating: Int = 0
)
