package com.example.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service interface defining endpoints for The Movie Database (TMDB) API v3.
 * Configured via NetworkModule with automatic BuildConfig API key injection.
 */
interface TmdbApiService {

  /**
   * Fetch all trending items (movies, TV shows) for a specified time window ('day' or 'week').
   */
  @GET("trending/all/{time_window}")
  suspend fun getTrendingAll(
    @Path("time_window") timeWindow: String = "day",
    @Query("page") page: Int = 1,
    @Query("api_key") apiKey: String? = null
  ): TmdbPaginatedResponse<TmdbMediaDto>

  /**
   * Fetch trending movies for a specified time window ('day' or 'week').
   */
  @GET("trending/movie/{time_window}")
  suspend fun getTrendingMovies(
    @Path("time_window") timeWindow: String = "day",
    @Query("page") page: Int = 1,
    @Query("api_key") apiKey: String? = null
  ): TmdbPaginatedResponse<TmdbMovie>

  /**
   * Fetch trending TV shows for a specified time window ('day' or 'week').
   */
  @GET("trending/tv/{time_window}")
  suspend fun getTrendingTv(
    @Path("time_window") timeWindow: String = "day",
    @Query("page") page: Int = 1,
    @Query("api_key") apiKey: String? = null
  ): TmdbPaginatedResponse<TmdbTvShow>

  /**
   * Generic trending endpoint allowing dynamic mediaType ('all', 'movie', 'tv').
   */
  @GET("trending/{media_type}/{time_window}")
  suspend fun getTrending(
    @Path("media_type") mediaType: String = "all",
    @Path("time_window") timeWindow: String = "day",
    @Query("page") page: Int = 1,
    @Query("api_key") apiKey: String? = null
  ): TmdbPaginatedResponse<TmdbMediaDto>

  @GET("movie/popular")
  suspend fun getPopularMovies(
    @Query("page") page: Int = 1,
    @Query("api_key") apiKey: String? = null
  ): TmdbPaginatedResponse<TmdbMediaDto>

  @GET("movie/top_rated")
  suspend fun getTopRatedMovies(
    @Query("page") page: Int = 1,
    @Query("api_key") apiKey: String? = null
  ): TmdbPaginatedResponse<TmdbMediaDto>

  @GET("tv/popular")
  suspend fun getPopularTv(
    @Query("page") page: Int = 1,
    @Query("api_key") apiKey: String? = null
  ): TmdbPaginatedResponse<TmdbMediaDto>

  @GET("tv/top_rated")
  suspend fun getTopRatedTv(
    @Query("page") page: Int = 1,
    @Query("api_key") apiKey: String? = null
  ): TmdbPaginatedResponse<TmdbMediaDto>

  @GET("search/multi")
  suspend fun searchMulti(
    @Query("query") query: String,
    @Query("page") page: Int = 1,
    @Query("include_adult") includeAdult: Boolean = false,
    @Query("api_key") apiKey: String? = null
  ): TmdbPaginatedResponse<TmdbMediaDto>

  @GET("movie/{movie_id}")
  suspend fun getMovieDetails(
    @Path("movie_id") movieId: Int,
    @Query("append_to_response") appendToResponse: String = "credits",
    @Query("api_key") apiKey: String? = null
  ): TmdbMovieDetailsDto

  @GET("tv/{tv_id}")
  suspend fun getTvDetails(
    @Path("tv_id") tvId: Int,
    @Query("append_to_response") appendToResponse: String = "credits",
    @Query("api_key") apiKey: String? = null
  ): TmdbTvDetailsDto

  @GET("tv/{tv_id}/season/{season_number}")
  suspend fun getSeasonDetails(
    @Path("tv_id") tvId: Int,
    @Path("season_number") seasonNumber: Int,
    @Query("api_key") apiKey: String? = null
  ): TmdbSeasonDetailsDto
}
