package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.TmdbClient
import com.example.data.api.TmdbEpisodeDto
import com.example.data.api.TmdbMediaDto
import com.example.data.api.TmdbMovieDetailsDto
import com.example.data.api.TmdbSeasonDetailsDto
import com.example.data.api.TmdbTvDetailsDto
import com.example.data.db.AppDatabase
import com.example.data.db.MediaItemEntity
import com.example.data.db.WatchedEpisodeEntity
import com.example.data.repository.CineStats
import com.example.data.repository.CineTrackRepository
import com.example.data.repository.UpNextItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Screen {
  object Discover : Screen
  object UpNext : Screen
  object Library : Screen
  object Stats : Screen
  object Settings : Screen
  data class MediaDetail(val id: Int, val mediaType: String) : Screen
}

enum class LibraryFilter(val label: String) {
  ALL("All"),
  WATCHING("Watching"),
  PLAN_TO_WATCH("Watchlist"),
  COMPLETED("Watched"),
  FAVORITES("Favorites"),
  DROPPED("Dropped")
}

enum class MediaTypeFilter(val label: String) {
  ALL("All Types"),
  MOVIES("Movies"),
  TV("TV Shows")
}

data class DiscoverUiState(
  val isLoading: Boolean = true,
  val trendingToday: List<TmdbMediaDto> = emptyList(),
  val trendingWeek: List<TmdbMediaDto> = emptyList(),
  val trendingMoviesWeek: List<TmdbMediaDto> = emptyList(),
  val trendingTvWeek: List<TmdbMediaDto> = emptyList(),
  val popularMovies: List<TmdbMediaDto> = emptyList(),
  val popularTv: List<TmdbMediaDto> = emptyList(),
  val topRatedMovies: List<TmdbMediaDto> = emptyList(),
  val error: String? = null
) {
  val trending: List<TmdbMediaDto> get() = trendingToday.ifEmpty { trendingWeek }
}

data class DetailUiState(
  val isLoading: Boolean = true,
  val movieDetails: TmdbMovieDetailsDto? = null,
  val tvDetails: TmdbTvDetailsDto? = null,
  val selectedSeasonNumber: Int = 1,
  val seasonEpisodes: List<TmdbEpisodeDto> = emptyList(),
  val isLoadingSeason: Boolean = false,
  val trackedEntity: MediaItemEntity? = null,
  val watchedEpisodes: List<WatchedEpisodeEntity> = emptyList(),
  val error: String? = null
)

class CineTrackViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: CineTrackRepository

  init {
    val db = AppDatabase.getInstance(application)
    repository = CineTrackRepository(db.mediaDao())
  }

  // Navigation State
  private val _currentScreen = MutableStateFlow<Screen>(Screen.Discover)
  val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

  private val _navigationStack = mutableListOf<Screen>()

  fun navigateTo(screen: Screen) {
    if (_currentScreen.value != screen) {
      _navigationStack.add(_currentScreen.value)
      _currentScreen.value = screen
    }
  }

  fun navigateBack(): Boolean {
    if (_navigationStack.isNotEmpty()) {
      _currentScreen.value = _navigationStack.removeAt(_navigationStack.lastIndex)
      return true
    }
    return false
  }

  // Discover state
  private val _discoverState = MutableStateFlow(DiscoverUiState())
  val discoverState: StateFlow<DiscoverUiState> = _discoverState.asStateFlow()

  // Search state
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _searchResults = MutableStateFlow<List<TmdbMediaDto>>(emptyList())
  val searchResults: StateFlow<List<TmdbMediaDto>> = _searchResults.asStateFlow()

  private val _isSearching = MutableStateFlow(false)
  val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

  private var searchJob: Job? = null

  // Up Next state
  val upNextList: StateFlow<List<UpNextItem>> = repository.upNextItems
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Tracked Library state
  val allTracked: StateFlow<List<MediaItemEntity>> = repository.allTrackedMedia
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Stats state
  val stats: StateFlow<CineStats> = repository.stats
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CineStats())

  // Library filters
  private val _libraryStatusFilter = MutableStateFlow(LibraryFilter.ALL)
  val libraryStatusFilter: StateFlow<LibraryFilter> = _libraryStatusFilter.asStateFlow()

  private val _libraryTypeFilter = MutableStateFlow(MediaTypeFilter.ALL)
  val libraryTypeFilter: StateFlow<MediaTypeFilter> = _libraryTypeFilter.asStateFlow()

  fun setLibraryStatusFilter(filter: LibraryFilter) {
    _libraryStatusFilter.value = filter
  }

  fun setLibraryTypeFilter(filter: MediaTypeFilter) {
    _libraryTypeFilter.value = filter
  }

  // Detail Screen State
  private val _detailState = MutableStateFlow(DetailUiState())
  val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

  init {
    loadDiscoverData()
  }

  fun loadDiscoverData() {
    viewModelScope.launch {
      _discoverState.value = _discoverState.value.copy(isLoading = true, error = null)
      val trendingTodayRes = repository.getTrending()
      val trendingWeekRes = repository.getTrendingThisWeek()
      val trendingMoviesWeekRes = repository.getTrendingMoviesThisWeek()
      val trendingTvWeekRes = repository.getTrendingTvThisWeek()
      val popularMoviesRes = repository.getPopularMovies()
      val popularTvRes = repository.getPopularTv()
      val topMoviesRes = repository.getTopRatedMovies()

      _discoverState.value = DiscoverUiState(
        isLoading = false,
        trendingToday = trendingTodayRes.getOrDefault(emptyList()),
        trendingWeek = trendingWeekRes.getOrDefault(emptyList()),
        trendingMoviesWeek = trendingMoviesWeekRes.getOrDefault(emptyList()),
        trendingTvWeek = trendingTvWeekRes.getOrDefault(emptyList()),
        popularMovies = popularMoviesRes.getOrDefault(emptyList()),
        popularTv = popularTvRes.getOrDefault(emptyList()),
        topRatedMovies = topMoviesRes.getOrDefault(emptyList())
      )
    }
  }

  fun onSearchQueryChanged(query: String) {
    _searchQuery.value = query
    searchJob?.cancel()

    if (query.isBlank()) {
      _searchResults.value = emptyList()
      _isSearching.value = false
      return
    }

    searchJob = viewModelScope.launch {
      _isSearching.value = true
      delay(300) // debounce
      val result = repository.searchMedia(query)
      _searchResults.value = result.getOrDefault(emptyList())
      _isSearching.value = false
    }
  }

  fun openMediaDetails(id: Int, mediaType: String) {
    navigateTo(Screen.MediaDetail(id, mediaType))
    loadMediaDetails(id, mediaType)
  }

  fun loadMediaDetails(id: Int, mediaType: String) {
    viewModelScope.launch {
      _detailState.value = DetailUiState(isLoading = true)

      // Observe tracked entity from Room
      launch {
        repository.getMediaById(id, mediaType).collect { tracked ->
          _detailState.value = _detailState.value.copy(trackedEntity = tracked)
        }
      }

      if (mediaType == "movie") {
        val movieRes = repository.getMovieDetails(id)
        if (movieRes.isSuccess) {
          _detailState.value = _detailState.value.copy(
            isLoading = false,
            movieDetails = movieRes.getOrNull()
          )
        } else {
          _detailState.value = _detailState.value.copy(
            isLoading = false,
            error = "Failed to load movie details"
          )
        }
      } else {
        // Observe watched episodes
        launch {
          repository.getWatchedEpisodesForShow(id).collect { eps ->
            _detailState.value = _detailState.value.copy(watchedEpisodes = eps)
          }
        }

        val tvRes = repository.getTvDetails(id)
        if (tvRes.isSuccess) {
          val tvDetails = tvRes.getOrNull()
          _detailState.value = _detailState.value.copy(
            isLoading = false,
            tvDetails = tvDetails,
            selectedSeasonNumber = 1
          )
          loadSeasonEpisodes(id, 1)
        } else {
          _detailState.value = _detailState.value.copy(
            isLoading = false,
            error = "Failed to load TV show details"
          )
        }
      }
    }
  }

  fun selectSeason(seasonNumber: Int) {
    val tv = _detailState.value.tvDetails ?: return
    _detailState.value = _detailState.value.copy(selectedSeasonNumber = seasonNumber)
    loadSeasonEpisodes(tv.id, seasonNumber)
  }

  private fun loadSeasonEpisodes(tvId: Int, seasonNumber: Int) {
    viewModelScope.launch {
      _detailState.value = _detailState.value.copy(isLoadingSeason = true)
      val res = repository.getSeasonDetails(tvId, seasonNumber)
      _detailState.value = _detailState.value.copy(
        isLoadingSeason = false,
        seasonEpisodes = res.getOrNull()?.episodes ?: emptyList()
      )
    }
  }

  // Quick Tracking Actions
  fun setStatus(media: TmdbMediaDto, status: String) {
    viewModelScope.launch {
      repository.trackMedia(
        id = media.id,
        mediaType = media.resolvedMediaType,
        title = media.displayTitle,
        posterPath = media.posterPath,
        backdropPath = media.backdropPath,
        overview = media.overview,
        releaseDate = media.displayDate,
        genres = null,
        runtime = if (media.resolvedMediaType == "movie") 110 else 45,
        tmdbVoteAverage = media.voteAverage ?: 0.0,
        status = status
      )
    }
  }

  fun updateCurrentDetailStatus(status: String) {
    val state = _detailState.value
    viewModelScope.launch {
      if (state.movieDetails != null) {
        val m = state.movieDetails
        repository.trackMedia(
          id = m.id,
          mediaType = "movie",
          title = m.title,
          posterPath = m.posterPath,
          backdropPath = m.backdropPath,
          overview = m.overview,
          releaseDate = m.releaseDate,
          genres = m.genres.joinToString(", ") { it.name },
          runtime = m.runtime ?: 110,
          tmdbVoteAverage = m.voteAverage ?: 0.0,
          status = status
        )
      } else if (state.tvDetails != null) {
        val tv = state.tvDetails
        repository.trackMedia(
          id = tv.id,
          mediaType = "tv",
          title = tv.name,
          posterPath = tv.posterPath,
          backdropPath = tv.backdropPath,
          overview = tv.overview,
          releaseDate = tv.firstAirDate,
          genres = tv.genres.joinToString(", ") { it.name },
          runtime = tv.episodeRunTime?.firstOrNull() ?: 45,
          tmdbVoteAverage = tv.voteAverage ?: 0.0,
          status = status,
          totalSeasons = tv.numberOfSeasons,
          totalEpisodes = tv.numberOfEpisodes
        )
      }
    }
  }

  fun updateRating(id: Int, mediaType: String, rating: Int) {
    viewModelScope.launch {
      repository.updateRating(id, mediaType, rating)
    }
  }

  fun toggleFavorite(id: Int, mediaType: String, currentFav: Boolean) {
    viewModelScope.launch {
      repository.toggleFavorite(id, mediaType, currentFav)
    }
  }

  fun updateNotes(id: Int, mediaType: String, notes: String?) {
    viewModelScope.launch {
      repository.updateNotes(id, mediaType, notes)
    }
  }

  fun removeTracking(id: Int, mediaType: String) {
    viewModelScope.launch {
      repository.removeTracking(id, mediaType)
    }
  }

  // TV Episode Tracking (Without Scrobbling)
  fun toggleEpisodeWatched(episode: TmdbEpisodeDto) {
    val tv = _detailState.value.tvDetails ?: return
    val isWatched = _detailState.value.watchedEpisodes.any {
      it.seasonNumber == episode.seasonNumber && it.episodeNumber == episode.episodeNumber
    }

    viewModelScope.launch {
      // Ensure show exists in tracked media first
      if (_detailState.value.trackedEntity == null) {
        repository.trackMedia(
          id = tv.id,
          mediaType = "tv",
          title = tv.name,
          posterPath = tv.posterPath,
          backdropPath = tv.backdropPath,
          overview = tv.overview,
          releaseDate = tv.firstAirDate,
          genres = tv.genres.joinToString(", ") { it.name },
          runtime = tv.episodeRunTime?.firstOrNull() ?: 45,
          tmdbVoteAverage = tv.voteAverage ?: 0.0,
          status = "watching",
          totalSeasons = tv.numberOfSeasons,
          totalEpisodes = tv.numberOfEpisodes
        )
      }

      repository.toggleEpisodeWatched(
        showId = tv.id,
        seasonNumber = episode.seasonNumber,
        episodeNumber = episode.episodeNumber,
        episodeName = episode.name,
        stillPath = episode.stillPath,
        airDate = episode.airDate,
        runtime = episode.runtime ?: 45,
        isCurrentlyWatched = isWatched
      )
    }
  }

  // Quick 1-tap "Watch Next Episode" from Up Next card!
  fun markUpNextEpisodeWatched(item: UpNextItem) {
    viewModelScope.launch {
      repository.toggleEpisodeWatched(
        showId = item.showId,
        seasonNumber = item.seasonNumber,
        episodeNumber = item.episodeNumber,
        episodeName = item.episodeName,
        stillPath = item.stillPath,
        airDate = item.airDate,
        runtime = item.runtime ?: 45,
        isCurrentlyWatched = false
      )
    }
  }

  fun markSeasonWatched(seasonNumber: Int) {
    val tv = _detailState.value.tvDetails ?: return
    val episodes = _detailState.value.seasonEpisodes
    if (episodes.isEmpty()) return

    viewModelScope.launch {
      if (_detailState.value.trackedEntity == null) {
        repository.trackMedia(
          id = tv.id,
          mediaType = "tv",
          title = tv.name,
          posterPath = tv.posterPath,
          backdropPath = tv.backdropPath,
          overview = tv.overview,
          releaseDate = tv.firstAirDate,
          genres = tv.genres.joinToString(", ") { it.name },
          runtime = tv.episodeRunTime?.firstOrNull() ?: 45,
          tmdbVoteAverage = tv.voteAverage ?: 0.0,
          status = "watching",
          totalSeasons = tv.numberOfSeasons,
          totalEpisodes = tv.numberOfEpisodes
        )
      }
      repository.markSeasonWatched(tv.id, seasonNumber, episodes)
    }
  }

  fun unmarkSeasonWatched(seasonNumber: Int) {
    val tv = _detailState.value.tvDetails ?: return
    viewModelScope.launch {
      repository.unmarkSeasonWatched(tv.id, seasonNumber)
    }
  }

  fun updateCustomApiKey(newKey: String) {
    TmdbClient.customApiKey = newKey.trim()
    loadDiscoverData()
  }

  fun clearAllData() {
    viewModelScope.launch {
      repository.clearAllData()
    }
  }
}
