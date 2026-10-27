package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.api.TmdbClient
import com.example.data.api.TmdbMediaDto
import com.example.ui.CineTrackViewModel
import com.example.ui.components.MediaPosterCard
import com.example.ui.theme.CineAmber
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineGold
import com.example.ui.theme.CinemaMidnight
import com.example.ui.theme.CinemaSurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun DiscoverScreen(
  viewModel: CineTrackViewModel,
  onMediaClick: (Int, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val discoverState by viewModel.discoverState.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
  val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
  val allTracked by viewModel.allTracked.collectAsStateWithLifecycle()

  val trackedMap = allTracked.associateBy { "${it.id}_${it.mediaType}" }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CinemaMidnight)
  ) {
    // Top Bar & Search
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "CineTrack",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = TextPrimary
          )
          Text(
            text = "Explore & track your journey",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }

        Surface(
          color = CineAmber.copy(alpha = 0.15f),
          shape = RoundedCornerShape(20.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = CineGold,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "TMDB Live",
              color = CineAmber,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Search Field
      OutlinedTextField(
        value = searchQuery,
        onValueChange = viewModel::onSearchQueryChanged,
        placeholder = { Text("Search movies, TV shows, anime…", color = TextTertiary, fontSize = 14.sp) },
        leadingIcon = {
          Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = CineCyan)
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
              Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
            }
          }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
          focusedContainerColor = CinemaSurfaceCard,
          unfocusedContainerColor = CinemaSurfaceCard,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
          focusedIndicatorColor = CineCyan,
          unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_input")
      )
    }

    if (searchQuery.isNotBlank()) {
      // Search Results Grid
      if (isSearching) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = CineCyan)
        }
      } else if (searchResults.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text("No titles found for \"$searchQuery\"", color = TextSecondary)
        }
      } else {
        LazyVerticalGrid(
          columns = GridCells.Adaptive(110.dp),
          contentPadding = PaddingValues(16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(searchResults, key = { "${it.id}_${it.resolvedMediaType}" }) { media ->
            val tracked = trackedMap["${media.id}_${media.resolvedMediaType}"]
            MediaPosterCard(
              media = media,
              currentStatus = tracked?.status,
              onClick = { onMediaClick(media.id, media.resolvedMediaType) }
            )
          }
        }
      }
    } else {
      // Discover Content Feed
      if (discoverState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = CineAmber)
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(bottom = 90.dp)
        ) {
          // Hero Featured Banner
          val heroItem = discoverState.trending.firstOrNull()
          if (heroItem != null) {
            item {
              FeaturedHeroBanner(
                media = heroItem,
                onMediaClick = { onMediaClick(heroItem.id, heroItem.resolvedMediaType) },
                onWatchlistClick = { viewModel.setStatus(heroItem, "plan_to_watch") },
                onStartWatchingClick = { viewModel.setStatus(heroItem, "watching") }
              )
              Spacer(modifier = Modifier.height(20.dp))
            }
          }

          // Trending This Week (User requested!)
          item {
            MediaShelf(
              title = "📈 Trending This Week",
              items = discoverState.trendingWeek.ifEmpty { discoverState.trendingToday },
              trackedMap = trackedMap,
              onMediaClick = onMediaClick
            )
            Spacer(modifier = Modifier.height(20.dp))
          }

          // Popular Movies (User requested!)
          item {
            MediaShelf(
              title = "🎬 Popular Movies",
              items = discoverState.popularMovies,
              trackedMap = trackedMap,
              onMediaClick = onMediaClick
            )
            Spacer(modifier = Modifier.height(20.dp))
          }

          // Popular TV Shows (User requested!)
          item {
            MediaShelf(
              title = "📺 Popular TV Shows",
              items = discoverState.popularTv,
              trackedMap = trackedMap,
              onMediaClick = onMediaClick
            )
            Spacer(modifier = Modifier.height(20.dp))
          }

          // Trending Movies This Week (User requested!)
          if (discoverState.trendingMoviesWeek.isNotEmpty()) {
            item {
              MediaShelf(
                title = "🍿 Trending Movies This Week",
                items = discoverState.trendingMoviesWeek,
                trackedMap = trackedMap,
                onMediaClick = onMediaClick
              )
              Spacer(modifier = Modifier.height(20.dp))
            }
          }

          // Trending Shows This Week (User requested!)
          if (discoverState.trendingTvWeek.isNotEmpty()) {
            item {
              MediaShelf(
                title = "⚡ Trending Shows This Week",
                items = discoverState.trendingTvWeek,
                trackedMap = trackedMap,
                onMediaClick = onMediaClick
              )
              Spacer(modifier = Modifier.height(20.dp))
            }
          }

          // Trending Today
          item {
            MediaShelf(
              title = "🔥 Trending Today",
              items = discoverState.trendingToday.drop(1).ifEmpty { discoverState.trending.drop(1) },
              trackedMap = trackedMap,
              onMediaClick = onMediaClick
            )
            Spacer(modifier = Modifier.height(20.dp))
          }

          item {
            MediaShelf(
              title = "⭐ Top Rated Movies",
              items = discoverState.topRatedMovies,
              trackedMap = trackedMap,
              onMediaClick = onMediaClick
            )
            Spacer(modifier = Modifier.height(20.dp))
          }
        }
      }
    }
  }
}

@Composable
fun FeaturedHeroBanner(
  media: TmdbMediaDto,
  onMediaClick: () -> Unit,
  onWatchlistClick: () -> Unit,
  onStartWatchingClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .height(240.dp)
      .clip(RoundedCornerShape(20.dp))
      .clickable { onMediaClick() }
      .testTag("hero_banner"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = CinemaSurfaceCard)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      val backdrop = TmdbClient.backdropUrl(media.backdropPath)
      if (backdrop != null) {
        AsyncImage(
          model = backdrop,
          contentDescription = media.displayTitle,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }

      // Dark gradient overlay
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(
                Color.Transparent,
                CinemaMidnight.copy(alpha = 0.6f),
                CinemaMidnight.copy(alpha = 0.95f)
              )
            )
          )
      )

      // Content at bottom
      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(16.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = CineAmber,
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = "FEATURED",
              color = CinemaMidnight,
              fontSize = 9.sp,
              fontWeight = FontWeight.Black,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = media.resolvedMediaType.uppercase(),
            color = CineCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(8.dp))
          if ((media.voteAverage ?: 0.0) > 0) {
            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = CineGold, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = String.format("%.1f", media.voteAverage), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = media.displayTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = TextPrimary,
          maxLines = 1
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = onStartWatchingClick,
            colors = ButtonDefaults.buttonColors(containerColor = CineCyan),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("hero_start_watching")
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CinemaMidnight, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Watching", color = CinemaMidnight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }

          OutlinedButton(
            onClick = onWatchlistClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("hero_add_watchlist")
          ) {
            Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = CineAmber, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Watchlist", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
          }
        }
      }
    }
  }
}

@Composable
fun MediaShelf(
  title: String,
  items: List<TmdbMediaDto>,
  trackedMap: Map<String, com.example.data.db.MediaItemEntity>,
  onMediaClick: (Int, String) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = TextPrimary,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(items, key = { "${it.id}_${it.resolvedMediaType}" }) { media ->
        val tracked = trackedMap["${media.id}_${media.resolvedMediaType}"]
        MediaPosterCard(
          media = media,
          currentStatus = tracked?.status,
          onClick = { onMediaClick(media.id, media.resolvedMediaType) }
        )
      }
    }
  }
}
