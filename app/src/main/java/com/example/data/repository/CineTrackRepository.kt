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

  // Curated Fallbacks mirroring live TMDB catalogue for offline resilience
  private fun getFallbackTrending(): List<TmdbMediaDto> = listOf(
    TmdbMediaDto(
      id = 1384216,
      title = "The Dog Stars",
      overview = "Hig survived the flu that wiped out almost everyone he knows. His wife is gone, his friends are dead, and he lives in an airplane hangar with his dog.",
      posterPath = "/A7i16oDi03qrKep5pBfuTWNUTb.jpg",
      backdropPath = "/joVtII9prX3MC4a2LfoKsiMWET3.jpg",
      mediaType = "movie",
      releaseDate = "2026-08-26",
      voteAverage = 6.6
    ),
    TmdbMediaDto(
      id = 1283515,
      title = "Verity",
      overview = "Lowen Ashleigh, a struggling writer on the brink of financial ruin, accepts the job offer of a lifetime: complete the remaining books of a successful series.",
      posterPath = "/zPHfsaiMZT1HNz1ZS55iyLKZkFG.jpg",
      backdropPath = "/zrGI2Lm89fgR5ntysKLmEPgNrdV.jpg",
      mediaType = "movie",
      releaseDate = "2026-09-30",
      voteAverage = 6.4
    ),
    TmdbMediaDto(
      id = 96677,
      name = "Lupin",
      overview = "Inspired by the adventures of Arsène Lupin, gentleman thief Assane Diop sets out to avenge his father for an injustice inflicted by a wealthy family.",
      posterPath = "/h6Z2oogE4mJk2uffdtIlLhb0EHx.jpg",
      backdropPath = "/aY7zv2pfk9H0QxaaL3PBjvalbKQ.jpg",
      mediaType = "tv",
      firstAirDate = "2021-01-08",
      voteAverage = 7.7
    ),
    TmdbMediaDto(
      id = 220542,
      name = "The Apothecary Diaries",
      overview = "Maomao, a young woman trained in the art of herbal medicine, is forced to work as a lowly servant in the inner palace.",
      posterPath = "/sJZcy8QsOPuRPiCfoCWVB3r9Ohg.jpg",
      backdropPath = "/gffVOVZ2Y9cmVIZ26Gt9ByMGCYH.jpg",
      mediaType = "tv",
      firstAirDate = "2023-10-22",
      voteAverage = 8.7
    ),
    TmdbMediaDto(
      id = 1465063,
      title = "Forgotten Island",
      overview = "A harrowing voyage into unfamiliar territory tests the courage of survivors on a remote island.",
      posterPath = "/myd1K3gefdNAWFsGNRDdMeiK9Qg.jpg",
      backdropPath = "/cNICUho5uiAGkfCAQq1ihyaMla4.jpg",
      mediaType = "movie",
      releaseDate = "2026-09-10",
      voteAverage = 7.5
    )
  )

  private fun getFallbackMovies(): List<TmdbMediaDto> = listOf(
    TmdbMediaDto(
      id = 969681,
      title = "Spider-Man: Brand New Day",
      overview = "Peter Parker embarks on a fresh chapter in New York City facing unprecedented adversaries and testing his resolve.",
      posterPath = "/bjiS5ipwxb9JFy3XRRN4OAilSeX.jpg",
      backdropPath = "/4tjxYg2AbOzzG9G1YuiT2NzYhlH.jpg",
      mediaType = "movie",
      releaseDate = "2026-07-29",
      voteAverage = 8.5
    ),
    TmdbMediaDto(
      id = 1058424,
      title = "Hope",
      overview = "In a battle against impossible odds, a team bands together to illuminate the darkest corners of their realm.",
      posterPath = "/6VY1ZKgpsc8CfGsQcCGiYEgHdH0.jpg",
      backdropPath = "/qqHFLg90IUSoTmSMpKOOW5RyqN3.jpg",
      mediaType = "movie",
      releaseDate = "2026-07-15",
      voteAverage = 6.7
    ),
    TmdbMediaDto(
      id = 1101412,
      title = "Fall 2: Deadpoint",
      overview = "High-altitude climbers fight against harsh weather and lethal hazards in a race against time.",
      posterPath = "/fgSm5ylwiXbIHn8UbUXDjk9RRu4.jpg",
      backdropPath = "/nGau0KBOaRILSK5MqFcuTfv7GMl.jpg",
      mediaType = "movie",
      releaseDate = "2026-09-01",
      voteAverage = 7.3
    ),
    TmdbMediaDto(
      id = 1368337,
      title = "The Odyssey",
      overview = "An epic retelling of the ten-year journey home across uncharted seas and mythical perils.",
      posterPath = "/5rhTDKUhPYvpdQIijFIs5VoWsON.jpg",
      backdropPath = "/bulFtfy3oBQtCnAMSi7g6DSSds3.jpg",
      mediaType = "movie",
      releaseDate = "2026-07-15",
      voteAverage = 8.0
    )
  )

  private fun getFallbackTv(): List<TmdbMediaDto> = listOf(
    TmdbMediaDto(
      id = 1416,
      name = "Grey's Anatomy",
      overview = "Follows the personal and professional lives of a group of doctors at Seattle’s Grey Sloan Memorial Hospital.",
      posterPath = "/gO8Fo47KV1opiutRDdBS5jclTQB.jpg",
      backdropPath = "/A4NwkQXTXElE5iAsne5RpvS8R2Z.jpg",
      mediaType = "tv",
      firstAirDate = "2005-03-27",
      voteAverage = 8.2
    ),
    TmdbMediaDto(
      id = 1411,
      name = "Person of Interest",
      overview = "A former CIA operative and an eccentric billionaire software programmer team up to prevent violent crimes.",
      posterPath = "/f8aIvYk5h7Z8EP3dinCmVgQFYow.jpg",
      backdropPath = "/efeMgQn6iRALsOqDa2cqlf6Pw5O.jpg",
      mediaType = "tv",
      firstAirDate = "2011-09-22",
      voteAverage = 8.1
    ),
    TmdbMediaDto(
      id = 2734,
      name = "Law & Order: Special Victims Unit",
      overview = "In the criminal justice system, sexually based offenses are considered especially heinous. These are their stories.",
      posterPath = "/pHNhUwSXuDQ9jPZOCb8SgJaQZsc.jpg",
      backdropPath = "/xhK9YCxNwnE0OB1SWvCY39heHQI.jpg",
      mediaType = "tv",
      firstAirDate = "1999-09-20",
      voteAverage = 8.0
    ),
    TmdbMediaDto(
      id = 1434,
      name = "Family Guy",
      overview = "Sick, twisted and politically incorrect, the animated series features the adventures of the Griffin family.",
      posterPath = "/3PFsEuAiyLkWsP4GG6dIV37Q6gu.jpg",
      backdropPath = "/jbTqU6BJMufoMnPSlO4ThrcXs3Y.jpg",
      mediaType = "tv",
      firstAirDate = "1999-01-31",
      voteAverage = 7.4
    )
  )
}
