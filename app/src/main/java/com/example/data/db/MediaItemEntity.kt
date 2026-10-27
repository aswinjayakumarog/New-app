package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
  tableName = "tracked_media",
  primaryKeys = ["id", "mediaType"]
)
data class MediaItemEntity(
  val id: Int,
  val mediaType: String, // "movie" or "tv"
  val title: String,
  val posterPath: String? = null,
  val backdropPath: String? = null,
  val overview: String? = null,
  val releaseDate: String? = null,
  val genres: String? = null,
  val runtime: Int? = null, // in minutes
  val tmdbVoteAverage: Double = 0.0,
  val status: String = "plan_to_watch", // "watching", "plan_to_watch", "completed", "dropped"
  val isFavorite: Boolean = false,
  val userRating: Int = 0, // 1 to 10, 0 = unrated
  val userNotes: String? = null,
  val totalSeasons: Int? = null,
  val totalEpisodes: Int? = null,
  val addedAt: Long = System.currentTimeMillis(),
  val lastWatchedAt: Long? = null
)
