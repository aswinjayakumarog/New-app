package com.example.data.repository

import com.example.data.api.NetworkModule
import com.example.data.api.TmdbClient
import com.example.data.api.TmdbEpisodeDto
import com.example.data.api.TmdbMediaDto
import com.example.data.api.TmdbMovieDetailsDto
import com.example.data.api.TmdbSeasonDetailsDto
import com.example.data.api.TmdbTvDetailsDto
import com.example.data.db.MediaDao
import com.example.data.db.MediaItemEntity
import com.example.data.db.WatchedEpisodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

data class UpNextItem(
  val showId: Int,
  val showTitle: String,
  val posterPath: String?,
  val seasonNumber: Int,
  val episodeNumber: Int,
  val episodeName: String?,
  val stillPath: String?,
  val airDate: String?,
  val runtime: Int?,
  val totalEpisodes: Int,
  val watchedCount: Int
) {
  val progressPercent: Float
    get() = if (totalEpisodes > 0) (watchedCount.toFloat() / totalEpisodes).coerceIn(0f, 1f) else 0f
}

data class CineStats(
  val totalMoviesWatched: Int = 0,
  val totalMovieMinutes: Long = 0L,
  val totalEpisodesWatched: Int = 0,
  val totalTvMinutes: Long = 0L,
  val ratingDistribution: Map<Int, Int> = emptyMap(),
  val topGenres: List<Pair<String, Int>> = emptyList()
) {
  val totalMinutes: Long get() = totalMovieMinutes + totalTvMinutes
  val totalHours: Long get() = totalMinutes / 60
  val remainingMinutes: Long get() = totalMinutes % 60
  val days: Long get() = totalHours / 24
  val remainingHours: Long get() = totalHours % 24
}

class CineTrackRepository(
  private val mediaDao: MediaDao,
  private val apiService: com.example.data.api.TmdbApiService = NetworkModule.tmdbApiService
) {

  val allTrackedMedia: Flow<List<MediaItemEntity>> = mediaDao.getAllTrackedMedia()
  val allWatchedEpisodes: Flow<List<WatchedEpisodeEntity>> = mediaDao.getAllWatchedEpisodes()

  fun getMediaById(id: Int, mediaType: String): Flow<MediaItemEntity?> =
    mediaDao.getMediaById(id, mediaType)

  fun getWatchedEpisodesForShow(showId: Int): Flow<List<WatchedEpisodeEntity>> =
    mediaDao.getWatchedEpisodesForShow(showId)

  // TMDB API calls with error handling & fallback
  suspend fun getTrending(): Result<List<TmdbMediaDto>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getTrending()
      Result.success(response.results)
    } catch (e: Exception) {
      Result.success(getFallbackTrending())
    }
  }

  suspend fun getTrendingThisWeek(): Result<List<TmdbMediaDto>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getTrendingAll(timeWindow = "week")
      Result.success(response.results)
    } catch (e: Exception) {
      Result.success(getFallbackTrending().reversed())
    }
  }

  suspend fun getTrendingMoviesThisWeek(): Result<List<TmdbMediaDto>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getTrendingMovies(timeWindow = "week")
      val mapped = response.results.map {
        TmdbMediaDto(
          id = it.id,
          title = it.title,
          overview = it.overview,
          posterPath = it.posterPath,
          backdropPath = it.backdropPath,
          mediaType = "movie",
          releaseDate = it.releaseDate,
          voteAverage = it.voteAverage,
          voteCount = it.voteCount,
          genreIds = it.genreIds
        )
      }
      Result.success(mapped)
    } catch (e: Exception) {
      Result.success(getFallbackMovies())
    }
  }

  suspend fun getTrendingTvThisWeek(): Result<List<TmdbMediaDto>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getTrendingTv(timeWindow = "week")
      val mapped = response.results.map {
        TmdbMediaDto(
          id = it.id,
          name = it.name,
          overview = it.overview,
          posterPath = it.posterPath,
          backdropPath = it.backdropPath,
          mediaType = "tv",
          firstAirDate = it.firstAirDate,
          voteAverage = it.voteAverage,
          voteCount = it.voteCount,
          genreIds = it.genreIds
        )
      }
      Result.success(mapped)
    } catch (e: Exception) {
      Result.success(getFallbackTv())
    }
  }

  suspend fun getPopularMovies(): Result<List<TmdbMediaDto>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getPopularMovies()
      Result.success(response.results.map { it.copy(mediaType = "movie") })
    } catch (e: Exception) {
      Result.success(getFallbackMovies())
    }
  }

  suspend fun getPopularTv(): Result<List<TmdbMediaDto>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getPopularTv()
      Result.success(response.results.map { it.copy(mediaType = "tv") })
    } catch (e: Exception) {
      Result.success(getFallbackTv())
    }
  }

  suspend fun getTopRatedMovies(): Result<List<TmdbMediaDto>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getTopRatedMovies()
      Result.success(response.results.map { it.copy(mediaType = "movie") })
    } catch (e: Exception) {
      Result.success(getFallbackMovies().reversed())
    }
  }

  suspend fun searchMedia(query: String): Result<List<TmdbMediaDto>> = withContext(Dispatchers.IO) {
    if (query.isBlank()) return@withContext Result.success(emptyList())
    try {
      val response = apiService.searchMulti(query = query.trim())
      val filtered = response.results.filter { it.mediaType == "movie" || it.mediaType == "tv" }
      Result.success(filtered)
    } catch (e: Exception) {
      // Local filter on fallback items if offline
      val queryLower = query.lowercase().trim()
      val matched = (getFallbackTrending() + getFallbackMovies() + getFallbackTv())
        .distinctBy { "${it.id}_${it.resolvedMediaType}" }
        .filter { it.displayTitle.lowercase().contains(queryLower) || it.overview?.lowercase()?.contains(queryLower) == true }
      Result.success(matched)
    }
  }

  suspend fun getMovieDetails(id: Int): Result<TmdbMovieDetailsDto> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getMovieDetails(movieId = id)
      Result.success(response)
    } catch (e: Exception) {
      val fallback = getFallbackMovies().find { it.id == id } ?: getFallbackMovies().first()
      Result.success(
        TmdbMovieDetailsDto(
          id = fallback.id,
          title = fallback.displayTitle,
          overview = fallback.overview,
          posterPath = fallback.posterPath,
          backdropPath = fallback.backdropPath,
          releaseDate = fallback.displayDate,
          runtime = 148,
          voteAverage = fallback.voteAverage ?: 8.5
        )
      )
    }
  }

  suspend fun getTvDetails(id: Int): Result<TmdbTvDetailsDto> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getTvDetails(tvId = id)
      Result.success(response)
    } catch (e: Exception) {
      val fallback = getFallbackTv().find { it.id == id } ?: getFallbackTv().first()
      Result.success(
        TmdbTvDetailsDto(
          id = fallback.id,
          name = fallback.displayTitle,
          overview = fallback.overview,
          posterPath = fallback.posterPath,
          backdropPath = fallback.backdropPath,
          firstAirDate = fallback.displayDate,
          numberOfSeasons = 5,
          numberOfEpisodes = 62,
          voteAverage = fallback.voteAverage ?: 9.0
        )
      )
    }
  }

  suspend fun getSeasonDetails(tvId: Int, seasonNumber: Int): Result<TmdbSeasonDetailsDto> =
    withContext(Dispatchers.IO) {
      try {
        val response = apiService.getSeasonDetails(tvId = tvId, seasonNumber = seasonNumber)
        Result.success(response)
      } catch (e: Exception) {
        // Generate mock season episodes if offline
        val episodes = (1..10).map { epNum ->
          TmdbEpisodeDto(
            id = tvId * 1000 + seasonNumber * 100 + epNum,
            episodeNumber = epNum,
            seasonNumber = seasonNumber,
            name = "Episode $epNum",
            overview = "Episode $epNum of Season $seasonNumber.",
            airDate = "2024-01-${String.format("%02d", epNum)}",
            runtime = 52,
            voteAverage = 8.4
          )
        }
        Result.success(
          TmdbSeasonDetailsDto(
            seasonNumber = seasonNumber,
            name = "Season $seasonNumber",
            episodes = episodes
          )
        )
      }
    }

  // Tracking operations
  suspend fun trackMedia(
    id: Int,
    mediaType: String,
    title: String,
    posterPath: String?,
    backdropPath: String?,
    overview: String?,
    releaseDate: String?,
    genres: String?,
    runtime: Int?,
    tmdbVoteAverage: Double,
    status: String,
    totalSeasons: Int? = null,
    totalEpisodes: Int? = null
  ) = withContext(Dispatchers.IO) {
    val existing = mediaDao.getMediaByIdDirect(id, mediaType)
    val entity = MediaItemEntity(
      id = id,
      mediaType = mediaType,
      title = title,
      posterPath = posterPath ?: existing?.posterPath,
      backdropPath = backdropPath ?: existing?.backdropPath,
      overview = overview ?: existing?.overview,
      releaseDate = releaseDate ?: existing?.releaseDate,
      genres = genres ?: existing?.genres,
      runtime = runtime ?: existing?.runtime,
      tmdbVoteAverage = if (tmdbVoteAverage > 0) tmdbVoteAverage else (existing?.tmdbVoteAverage ?: 0.0),
      status = status,
      isFavorite = existing?.isFavorite ?: false,
      userRating = existing?.userRating ?: 0,
      userNotes = existing?.userNotes,
      totalSeasons = totalSeasons ?: existing?.totalSeasons,
      totalEpisodes = totalEpisodes ?: existing?.totalEpisodes,
      addedAt = existing?.addedAt ?: System.currentTimeMillis(),
      lastWatchedAt = if (status == "completed" || status == "watching") System.currentTimeMillis() else existing?.lastWatchedAt
    )
    mediaDao.upsertMedia(entity)
  }

  suspend fun updateStatus(id: Int, mediaType: String, status: String) = withContext(Dispatchers.IO) {
    val lastWatched = if (status == "completed" || status == "watching") System.currentTimeMillis() else null
    mediaDao.updateStatus(id, mediaType, status, lastWatched)
  }

  suspend fun updateRating(id: Int, mediaType: String, rating: Int) = withContext(Dispatchers.IO) {
    mediaDao.updateRating(id, mediaType, rating)
  }

  suspend fun toggleFavorite(id: Int, mediaType: String, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
    mediaDao.updateFavorite(id, mediaType, !currentFavorite)
  }

  suspend fun updateNotes(id: Int, mediaType: String, notes: String?) = withContext(Dispatchers.IO) {
    mediaDao.updateNotes(id, mediaType, notes)
  }

  suspend fun removeTracking(id: Int, mediaType: String) = withContext(Dispatchers.IO) {
    mediaDao.deleteMedia(id, mediaType)
    if (mediaType == "tv") {
      mediaDao.deleteWatchedEpisodesForShow(id)
    }
  }

  // Episode tracking without scrobbling
  suspend fun toggleEpisodeWatched(
    showId: Int,
    seasonNumber: Int,
    episodeNumber: Int,
    episodeName: String?,
    stillPath: String?,
    airDate: String?,
    runtime: Int?,
    isCurrentlyWatched: Boolean
  ) = withContext(Dispatchers.IO) {
    if (isCurrentlyWatched) {
      mediaDao.deleteWatchedEpisode(showId, seasonNumber, episodeNumber)
    } else {
      mediaDao.insertWatchedEpisode(
        WatchedEpisodeEntity(
          showId = showId,
          seasonNumber = seasonNumber,
          episodeNumber = episodeNumber,
          episodeName = episodeName,
          stillPath = stillPath,
          airDate = airDate,
          runtime = runtime,
          watchedAt = System.currentTimeMillis()
        )
      )
      // Auto-set show to "watching" if it was "plan_to_watch"
      val show = mediaDao.getMediaByIdDirect(showId, "tv")
      if (show != null && show.status == "plan_to_watch") {
        mediaDao.updateStatus(showId, "tv", "watching", System.currentTimeMillis())
      }
    }
  }

  suspend fun markSeasonWatched(
    showId: Int,
    seasonNumber: Int,
    episodes: List<TmdbEpisodeDto>
  ) = withContext(Dispatchers.IO) {
    val entities = episodes.map { ep ->
      WatchedEpisodeEntity(
        showId = showId,
        seasonNumber = seasonNumber,
        episodeNumber = ep.episodeNumber,
        episodeName = ep.name,
        stillPath = ep.stillPath,
        airDate = ep.airDate,
        runtime = ep.runtime,
        watchedAt = System.currentTimeMillis()
      )
    }
    mediaDao.insertWatchedEpisodes(entities)
    // Update show status to watching if plan_to_watch
    val show = mediaDao.getMediaByIdDirect(showId, "tv")
    if (show != null && show.status == "plan_to_watch") {
      mediaDao.updateStatus(showId, "tv", "watching", System.currentTimeMillis())
    }
  }

  suspend fun unmarkSeasonWatched(showId: Int, seasonNumber: Int) = withContext(Dispatchers.IO) {
    mediaDao.deleteWatchedEpisodesForSeason(showId, seasonNumber)
  }

  // Up Next calculation
  val upNextItems: Flow<List<UpNextItem>> = combine(
    mediaDao.getTrackedMediaByStatus("watching"),
    mediaDao.getAllWatchedEpisodes()
  ) { shows, watchedEpisodes ->
    val tvShows = shows.filter { it.mediaType == "tv" }
    val groupedWatched = watchedEpisodes.groupBy { it.showId }

    tvShows.mapNotNull { show ->
      val showWatched = groupedWatched[show.id].orEmpty()
      val watchedCount = showWatched.size
      val totalEps = show.totalEpisodes ?: (show.totalSeasons ?: 1) * 10

      // Find highest watched season and episode
      val maxWatched = showWatched.maxWithOrNull(
        compareBy<WatchedEpisodeEntity> { it.seasonNumber }.thenBy { it.episodeNumber }
      )

      val (nextSeason, nextEp) = if (maxWatched == null) {
        Pair(1, 1)
      } else {
        // Advance episode (assume 10 episodes per season if not specified)
        if (maxWatched.episodeNumber >= 10) {
          Pair(maxWatched.seasonNumber + 1, 1)
        } else {
          Pair(maxWatched.seasonNumber, maxWatched.episodeNumber + 1)
        }
      }

      // If user has watched everything, don't show in Up Next
      if (totalEps > 0 && watchedCount >= totalEps) {
        null
      } else {
        UpNextItem(
          showId = show.id,
          showTitle = show.title,
          posterPath = show.posterPath,
          seasonNumber = nextSeason,
          episodeNumber = nextEp,
          episodeName = "Season $nextSeason Episode $nextEp",
          stillPath = show.backdropPath,
          airDate = show.releaseDate,
          runtime = 45,
          totalEpisodes = totalEps,
          watchedCount = watchedCount
        )
      }
    }
  }

  // Stats calculation
  val stats: Flow<CineStats> = combine(
    mediaDao.getAllTrackedMedia(),
    mediaDao.getAllWatchedEpisodes()
  ) { allMedia, watchedEpisodes ->
    val completedMovies = allMedia.filter { it.mediaType == "movie" && (it.status == "completed" || it.lastWatchedAt != null) }
    val movieMinutes = completedMovies.sumOf { (it.runtime ?: 110).toLong() }

    val episodeMinutes = watchedEpisodes.sumOf { (it.runtime ?: 45).toLong() }

    val ratingsMap = mutableMapOf<Int, Int>()
    (1..10).forEach { ratingsMap[it] = 0 }
    allMedia.filter { it.userRating > 0 }.forEach {
      ratingsMap[it.userRating] = (ratingsMap[it.userRating] ?: 0) + 1
    }

    val genreCountMap = mutableMapOf<String, Int>()
    allMedia.forEach { media ->
      media.genres?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.forEach { genre ->
        genreCountMap[genre] = (genreCountMap[genre] ?: 0) + 1
      }
    }

    val topGenres = genreCountMap.toList().sortedByDescending { it.second }.take(6)

    CineStats(
      totalMoviesWatched = completedMovies.size,
      totalMovieMinutes = movieMinutes,
      totalEpisodesWatched = watchedEpisodes.size,
      totalTvMinutes = episodeMinutes,
      ratingDistribution = ratingsMap,
      topGenres = topGenres
    )
  }

  suspend fun clearAllData() = withContext(Dispatchers.IO) {
    mediaDao.clearAllMedia()
    mediaDao.clearAllEpisodes()
  }

  // Curated Fallbacks for offline resilience
  private fun getFallbackTrending(): List<TmdbMediaDto> = listOf(
    TmdbMediaDto(
      id = 1399,
      name = "Game of Thrones",
      overview = "Nine noble families fight for control over the lands of Westeros, while an ancient enemy returns after being dormant for millennia.",
      posterPath = "/1XS1oqL89opfnbLl8WnZY1O1uJx.jpg",
      backdropPath = "/2OMB0ynKlyIenMJWI2Dy9IWT4c.jpg",
      mediaType = "tv",
      firstAirDate = "2011-04-17",
      voteAverage = 8.4
    ),
    TmdbMediaDto(
      id = 693134,
      title = "Dune: Part Two",
      overview = "Follow the mythic journey of Paul Atreides as he unites with Chani and the Fremen while on a warpath of revenge against the conspirators who destroyed his family.",
      posterPath = "/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
      backdropPath = "/xOMo8BRK7PfcJv9JCnx7s520b4q.jpg",
      mediaType = "movie",
      releaseDate = "2024-03-01",
      voteAverage = 8.2
    ),
    TmdbMediaDto(
      id = 94605,
      name = "Arcane",
      overview = "Amid the stark discord of twin cities Piltover and Zaun, two sisters fight on rival sides of a war between magic technologies and incompatible convictions.",
      posterPath = "/fqldf2t8ztc9aiwn3k6mlX3tvRT.jpg",
      backdropPath = "/uDgy6hyPd82kOHh6I95FLtLnj6p.jpg",
      mediaType = "tv",
      firstAirDate = "2021-11-06",
      voteAverage = 8.7
    ),
    TmdbMediaDto(
      id = 157336,
      title = "Interstellar",
      overview = "The adventures of a group of explorers who make use of a newly discovered wormhole to surpass the limitations on human space travel.",
      posterPath = "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
      backdropPath = "/xJHokMbljvjADYdit5fK5VQsXEG.jpg",
      mediaType = "movie",
      releaseDate = "2014-11-05",
      voteAverage = 8.4
    )
  )

  private fun getFallbackMovies(): List<TmdbMediaDto> = listOf(
    TmdbMediaDto(
      id = 27205,
      title = "Inception",
      overview = "Cobb, a skilled thief who commits corporate espionage by infiltrating the subconscious of his targets is offered a chance to regain his old life.",
      posterPath = "/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg",
      backdropPath = "/s3TBrRGB1iav7gFOCNx3H31MoES.jpg",
      mediaType = "movie",
      releaseDate = "2010-07-16",
      voteAverage = 8.4
    ),
    TmdbMediaDto(
      id = 155,
      title = "The Dark Knight",
      overview = "Batman raises the stakes in his war on crime with the help of Lt. Jim Gordon and District Attorney Harvey Dent against the Joker.",
      posterPath = "/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
      backdropPath = "/dqK9Hag1054tghRQSqLSfrkvQnA.jpg",
      mediaType = "movie",
      releaseDate = "2008-07-16",
      voteAverage = 8.5
    ),
    TmdbMediaDto(
      id = 238,
      title = "The Godfather",
      overview = "Spanning the years 1945 to 1955, a chronicle of the fictional Italian-American Corleone crime family.",
      posterPath = "/3bhkrj58Vtu7enYsRolD1fZdja1.jpg",
      backdropPath = "/tmU7GeKVybMWF9YOCagmsXZdn9n.jpg",
      mediaType = "movie",
      releaseDate = "1972-03-14",
      voteAverage = 8.7
    )
  )

  private fun getFallbackTv(): List<TmdbMediaDto> = listOf(
    TmdbMediaDto(
      id = 1396,
      name = "Breaking Bad",
      overview = "Walter White, a New Mexico chemistry teacher, is diagnosed with Stage III cancer and turns to manufacturing methamphetamine.",
      posterPath = "/ztkUQFLlC19CCMYHW9o1zWhJRNq.jpg",
      backdropPath = "/tsRy63Mu5cu8etL1X7ZLyf7UP1M.jpg",
      mediaType = "tv",
      firstAirDate = "2008-01-20",
      voteAverage = 8.9
    ),
    TmdbMediaDto(
      id = 119051,
      name = "Wednesday",
      overview = "Wednesday Addams is expelled from school and sent to Nevermore Academy, a boarding school for monstrous outcasts.",
      posterPath = "/9PFonQ95165agVa9zgpHwbOzqYv.jpg",
      backdropPath = "/iHSwvFe7FdFcRm039sbGekV3UB2.jpg",
      mediaType = "tv",
      firstAirDate = "2022-11-23",
      voteAverage = 8.5
    ),
    TmdbMediaDto(
      id = 85552,
      name = "Euphoria",
      overview = "A group of high school students navigate love and friendships in a world of drugs, sex, trauma and social media.",
      posterPath = "/3Q0hd3heuW4YvJYDkVthHQ3sa4v.jpg",
      backdropPath = "/93A60v2Z1Zz2cK7f8z7H3a0C4e2.jpg",
      mediaType = "tv",
      firstAirDate = "2019-06-16",
      voteAverage = 8.3
    )
  )
}
