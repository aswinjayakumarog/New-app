package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Generic paginated response returned by TMDB list endpoints.
 */
@JsonClass(generateAdapter = true)
data class TmdbPaginatedResponse<T>(
  val page: Int = 1,
  val results: List<T> = emptyList(),
  @property:Json(name = "total_pages") val totalPages: Int? = 1,
  @property:Json(name = "total_results") val totalResults: Int? = 0
)

/**
 * Dedicated data model representing a Movie from TMDB.
 */
@JsonClass(generateAdapter = true)
data class TmdbMovie(
  val id: Int,
  val title: String,
  val overview: String? = null,
  @property:Json(name = "poster_path") val posterPath: String? = null,
  @property:Json(name = "backdrop_path") val backdropPath: String? = null,
  @property:Json(name = "release_date") val releaseDate: String? = null,
  @property:Json(name = "vote_average") val voteAverage: Double? = 0.0,
  @property:Json(name = "vote_count") val voteCount: Int? = 0,
  @property:Json(name = "genre_ids") val genreIds: List<Int>? = emptyList(),
  val popularity: Double? = 0.0,
  val adult: Boolean? = false
) {
  val releaseYear: String get() = releaseDate?.take(4).orEmpty()
}

/**
 * Dedicated data model representing a TV Show from TMDB.
 */
@JsonClass(generateAdapter = true)
data class TmdbTvShow(
  val id: Int,
  val name: String,
  val overview: String? = null,
  @property:Json(name = "poster_path") val posterPath: String? = null,
  @property:Json(name = "backdrop_path") val backdropPath: String? = null,
  @property:Json(name = "first_air_date") val firstAirDate: String? = null,
  @property:Json(name = "vote_average") val voteAverage: Double? = 0.0,
  @property:Json(name = "vote_count") val voteCount: Int? = 0,
  @property:Json(name = "genre_ids") val genreIds: List<Int>? = emptyList(),
  val popularity: Double? = 0.0
) {
  val firstAirYear: String get() = firstAirDate?.take(4).orEmpty()
}

/**
 * Unified media DTO used for mixed multi-search and trending (both movies & TV shows).
 */
@JsonClass(generateAdapter = true)
data class TmdbMediaDto(
  val id: Int,
  val title: String? = null,
  val name: String? = null,
  val overview: String? = null,
  @property:Json(name = "poster_path") val posterPath: String? = null,
  @property:Json(name = "backdrop_path") val backdropPath: String? = null,
  @property:Json(name = "media_type") val mediaType: String? = null,
  @property:Json(name = "release_date") val releaseDate: String? = null,
  @property:Json(name = "first_air_date") val firstAirDate: String? = null,
  @property:Json(name = "vote_average") val voteAverage: Double? = 0.0,
  @property:Json(name = "vote_count") val voteCount: Int? = 0,
  @property:Json(name = "genre_ids") val genreIds: List<Int>? = emptyList()
) {
  val displayTitle: String get() = title ?: name ?: "Unknown"
  val displayDate: String get() = releaseDate ?: firstAirDate ?: ""
  val displayYear: String get() = displayDate.take(4)
  val resolvedMediaType: String get() = mediaType ?: if (name != null) "tv" else "movie"
}

@JsonClass(generateAdapter = true)
data class TmdbGenreDto(
  val id: Int,
  val name: String
)

@JsonClass(generateAdapter = true)
data class TmdbCastDto(
  val id: Int,
  val name: String,
  val character: String? = null,
  @property:Json(name = "profile_path") val profilePath: String? = null
)

@JsonClass(generateAdapter = true)
data class TmdbCreditsDto(
  val cast: List<TmdbCastDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbSeasonDto(
  val id: Int,
  @property:Json(name = "season_number") val seasonNumber: Int,
  val name: String? = null,
  val overview: String? = null,
  @property:Json(name = "poster_path") val posterPath: String? = null,
  @property:Json(name = "episode_count") val episodeCount: Int? = 0,
  @property:Json(name = "air_date") val airDate: String? = null
)

@JsonClass(generateAdapter = true)
data class TmdbEpisodeDto(
  val id: Int,
  @property:Json(name = "episode_number") val episodeNumber: Int,
  @property:Json(name = "season_number") val seasonNumber: Int,
  val name: String? = null,
  val overview: String? = null,
  @property:Json(name = "still_path") val stillPath: String? = null,
  @property:Json(name = "air_date") val airDate: String? = null,
  val runtime: Int? = null,
  @property:Json(name = "vote_average") val voteAverage: Double? = 0.0
)

@JsonClass(generateAdapter = true)
data class TmdbSeasonDetailsDto(
  @property:Json(name = "season_number") val seasonNumber: Int,
  val name: String? = null,
  val overview: String? = null,
  @property:Json(name = "poster_path") val posterPath: String? = null,
  @property:Json(name = "air_date") val airDate: String? = null,
  val episodes: List<TmdbEpisodeDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TmdbMovieDetailsDto(
  val id: Int,
  val title: String,
  val overview: String? = null,
  @property:Json(name = "poster_path") val posterPath: String? = null,
  @property:Json(name = "backdrop_path") val backdropPath: String? = null,
  @property:Json(name = "release_date") val releaseDate: String? = null,
  val runtime: Int? = null,
  @property:Json(name = "vote_average") val voteAverage: Double? = 0.0,
  @property:Json(name = "vote_count") val voteCount: Int? = 0,
  val tagline: String? = null,
  val status: String? = null,
  val genres: List<TmdbGenreDto> = emptyList(),
  val credits: TmdbCreditsDto? = null
)

@JsonClass(generateAdapter = true)
data class TmdbTvDetailsDto(
  val id: Int,
  val name: String,
  val overview: String? = null,
  @property:Json(name = "poster_path") val posterPath: String? = null,
  @property:Json(name = "backdrop_path") val backdropPath: String? = null,
  @property:Json(name = "first_air_date") val firstAirDate: String? = null,
  @property:Json(name = "number_of_seasons") val numberOfSeasons: Int? = 1,
  @property:Json(name = "number_of_episodes") val numberOfEpisodes: Int? = 0,
  @property:Json(name = "episode_run_time") val episodeRunTime: List<Int>? = emptyList(),
  @property:Json(name = "vote_average") val voteAverage: Double? = 0.0,
  @property:Json(name = "vote_count") val voteCount: Int? = 0,
  val tagline: String? = null,
  val status: String? = null,
  val genres: List<TmdbGenreDto> = emptyList(),
  val seasons: List<TmdbSeasonDto> = emptyList(),
  val credits: TmdbCreditsDto? = null
)
