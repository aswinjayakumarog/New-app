package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network module providing Retrofit instance, OkHttpClient, Moshi converter,
 * and the TmdbApiService configured with the TMDB API key from BuildConfig.
 */
object NetworkModule {
  private const val TAG = "NetworkModule"
  private const val BASE_URL = "https://api.themoviedb.org/3/"
  const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"

  // Fallback educational key if BuildConfig key is unconfigured or blank
  private const val DEFAULT_TMDB_KEY = "2dca580c2a14b55200e784d157207b4d"

  @Volatile
  var customApiKey: String? = null

  /**
   * Retrieves the API key to use for TMDB requests.
   * Prioritizes customApiKey set in UI, then BuildConfig.TMDB_API_KEY,
   * and falls back gracefully if unconfigured.
   */
  fun getApiKey(): String {
    customApiKey?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }

    val buildKey = try {
      BuildConfig.TMDB_API_KEY
    } catch (e: Exception) {
      Log.e(TAG, "Error accessing BuildConfig.TMDB_API_KEY", e)
      ""
    }

    if (buildKey.isNullOrBlank() || buildKey == "MY_TMDB_API_KEY") {
      Log.w(TAG, "BuildConfig.TMDB_API_KEY is missing or placeholder. Using fallback key.")
      return DEFAULT_TMDB_KEY
    }
    return buildKey
  }

  /**
   * OkHttp Interceptor that automatically attaches the TMDB API key
   * to every outgoing request query string.
   */
  private val apiKeyInterceptor = Interceptor { chain ->
    val originalRequest = chain.request()
    val originalUrl = originalRequest.url

    val newUrl = if (originalUrl.queryParameter("api_key") == null) {
      originalUrl.newBuilder()
        .addQueryParameter("api_key", getApiKey())
        .build()
    } else {
      originalUrl
    }

    val newRequest = originalRequest.newBuilder()
      .url(newUrl)
      .build()

    chain.proceed(newRequest)
  }

  private val loggingInterceptor: HttpLoggingInterceptor by lazy {
    HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BASIC
    }
  }

  val okHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
      .addInterceptor(apiKeyInterceptor)
      .addInterceptor(loggingInterceptor)
      .connectTimeout(15, TimeUnit.SECONDS)
      .readTimeout(15, TimeUnit.SECONDS)
      .build()
  }

  val moshi: Moshi by lazy {
    Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()
  }

  val retrofit: Retrofit by lazy {
    Retrofit.Builder()
      .baseUrl(BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
  }

  val tmdbApiService: TmdbApiService by lazy {
    retrofit.create(TmdbApiService::class.java)
  }

  // Image URL helpers for Coil
  fun posterUrl(path: String?, size: String = "w500"): String? {
    if (path.isNullOrBlank()) return null
    return if (path.startsWith("http")) path else "$IMAGE_BASE_URL$size$path"
  }

  fun backdropUrl(path: String?, size: String = "w780"): String? {
    if (path.isNullOrBlank()) return null
    return if (path.startsWith("http")) path else "$IMAGE_BASE_URL$size$path"
  }

  fun stillUrl(path: String?, size: String = "w300"): String? {
    if (path.isNullOrBlank()) return null
    return if (path.startsWith("http")) path else "$IMAGE_BASE_URL$size$path"
  }

  fun profileUrl(path: String?, size: String = "w185"): String? {
    if (path.isNullOrBlank()) return null
    return if (path.startsWith("http")) path else "$IMAGE_BASE_URL$size$path"
  }
}
