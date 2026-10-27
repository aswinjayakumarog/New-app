package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.MediaItemEntity
import com.example.data.db.WatchedEpisodeEntity
import com.example.data.repository.CineTrackRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var database: AppDatabase
  private lateinit var repository: CineTrackRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = CineTrackRepository(database.mediaDao())
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CineTrack", appName)
  }

  @Test
  fun `network module provides api service and configures base url and api key`() {
    val apiService = com.example.data.api.NetworkModule.tmdbApiService
    assertNotNull(apiService)

    val apiKey = com.example.data.api.NetworkModule.getApiKey()
    assertTrue("API key should not be blank", apiKey.isNotBlank())

    val baseUrl = com.example.data.api.NetworkModule.retrofit.baseUrl().toString()
    assertEquals("https://api.themoviedb.org/3/", baseUrl)
  }

  @Test
  fun `tmdb movie and tv data models serialize correctly`() {
    val moshi = com.example.data.api.NetworkModule.moshi

    val movieJson = """
      {"id":550,"title":"Fight Club","overview":"A ticking-time-bomb insomniac...","poster_path":"/poster.jpg","release_date":"1999-10-15","vote_average":8.4,"vote_count":26000}
    """.trimIndent()
    val movieAdapter = moshi.adapter(com.example.data.api.TmdbMovie::class.java)
    val movie = movieAdapter.fromJson(movieJson)
    assertNotNull(movie)
    assertEquals(550, movie?.id)
    assertEquals("Fight Club", movie?.title)
    assertEquals("1999", movie?.releaseYear)
    assertEquals("/poster.jpg", movie?.posterPath)
    assertEquals(8.4, movie?.voteAverage ?: 0.0, 0.01)

    val tvJson = """
      {"id":1396,"name":"Breaking Bad","overview":"A chemistry teacher...","poster_path":"/tv_poster.jpg","first_air_date":"2008-01-20","vote_average":8.9,"vote_count":12000}
    """.trimIndent()
    val tvAdapter = moshi.adapter(com.example.data.api.TmdbTvShow::class.java)
    val tv = tvAdapter.fromJson(tvJson)
    assertNotNull(tv)
    assertEquals(1396, tv?.id)
    assertEquals("Breaking Bad", tv?.name)
    assertEquals("2008", tv?.firstAirYear)
    assertEquals("/tv_poster.jpg", tv?.posterPath)
  }

  @Test
  fun `track movie and retrieve from repository`() = runTest {
    repository.trackMedia(
      id = 157336,
      mediaType = "movie",
      title = "Interstellar",
      posterPath = "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
      backdropPath = "/xJHokMbljvjADYdit5fK5VQsXEG.jpg",
      overview = "A team of explorers travel through a wormhole in space.",
      releaseDate = "2014-11-05",
      genres = "Sci-Fi, Adventure",
      runtime = 169,
      tmdbVoteAverage = 8.4,
      status = "completed"
    )

    val trackedList = repository.allTrackedMedia.first()
    assertEquals(1, trackedList.size)
    assertEquals("Interstellar", trackedList[0].title)
    assertEquals("completed", trackedList[0].status)
  }

  @Test
  fun `episode checkoff updates up next queue without scrobbling`() = runTest {
    // Track a TV show as "watching"
    repository.trackMedia(
      id = 1396,
      mediaType = "tv",
      title = "Breaking Bad",
      posterPath = "/ztkUQFLlC19CCMYHW9o1zWhJRNq.jpg",
      backdropPath = "/tsRy63Mu5cu8etL1X7ZLyf7UP1M.jpg",
      overview = "Chemistry teacher turns to cooking meth.",
      releaseDate = "2008-01-20",
      genres = "Drama, Crime",
      runtime = 47,
      tmdbVoteAverage = 8.9,
      status = "watching",
      totalSeasons = 5,
      totalEpisodes = 62
    )

    // Up Next initially points to S1E1
    val upNextInit = repository.upNextItems.first()
    assertEquals(1, upNextInit.size)
    assertEquals(1, upNextInit[0].seasonNumber)
    assertEquals(1, upNextInit[0].episodeNumber)

    // Mark S1E1 as watched
    repository.toggleEpisodeWatched(
      showId = 1396,
      seasonNumber = 1,
      episodeNumber = 1,
      episodeName = "Pilot",
      stillPath = null,
      airDate = "2008-01-20",
      runtime = 58,
      isCurrentlyWatched = false
    )

    // Up Next should now automatically advance to S1E2!
    val upNextAfter = repository.upNextItems.first()
    assertEquals(1, upNextAfter.size)
    assertEquals(1, upNextAfter[0].seasonNumber)
    assertEquals(2, upNextAfter[0].episodeNumber)
  }
}
