package com.example.data.api

/**
 * Convenience wrapper delegating to NetworkModule for backwards compatibility
 * across repository, UI components, and image URL resolution.
 */
object TmdbClient {
  val apiService: TmdbApiService get() = NetworkModule.tmdbApiService

  var customApiKey: String?
    get() = NetworkModule.customApiKey
    set(value) {
      NetworkModule.customApiKey = value
    }

  fun getApiKey(): String = NetworkModule.getApiKey()

  fun posterUrl(path: String?, size: String = "w500"): String? =
    NetworkModule.posterUrl(path, size)

  fun backdropUrl(path: String?, size: String = "w780"): String? =
    NetworkModule.backdropUrl(path, size)

  fun stillUrl(path: String?, size: String = "w300"): String? =
    NetworkModule.stillUrl(path, size)

  fun profileUrl(path: String?, size: String = "w185"): String? =
    NetworkModule.profileUrl(path, size)
}
