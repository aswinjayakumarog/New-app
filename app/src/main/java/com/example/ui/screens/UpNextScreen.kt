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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.api.TmdbClient
import com.example.data.repository.UpNextItem
import com.example.ui.CineTrackViewModel
import com.example.ui.Screen
import com.example.ui.theme.CineAmber
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineEmerald
import com.example.ui.theme.CinemaMidnight
import com.example.ui.theme.CinemaSurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun UpNextScreen(
  viewModel: CineTrackViewModel,
  onShowClick: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val upNextItems by viewModel.upNextList.collectAsStateWithLifecycle()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CinemaMidnight)
  ) {
    // Top Bar
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = "Up Next Queue",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
        color = TextPrimary
      )
      Text(
        text = "Instant 1-tap progress • No scrobbling required",
        style = MaterialTheme.typography.bodySmall,
        color = CineCyan
      )
    }

    if (upNextItems.isEmpty()) {
      // Empty state
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Surface(
            color = CinemaSurfaceCard,
            shape = CircleShape,
            modifier = Modifier.size(80.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.LiveTv,
                contentDescription = null,
                tint = CineCyan,
                modifier = Modifier.size(40.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "No TV Shows in Progress",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Browse trending TV shows in Discover or Watchlist and set status to \"Watching\" to queue up next episodes here!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = { viewModel.navigateTo(Screen.Discover) },
            colors = ButtonDefaults.buttonColors(containerColor = CineCyan),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CinemaMidnight)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Find Shows to Watch", color = CinemaMidnight, fontWeight = FontWeight.Bold)
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        items(upNextItems, key = { it.showId }) { item ->
          UpNextCard(
            item = item,
            onShowClick = { onShowClick(item.showId) },
            onMarkWatched = { viewModel.markUpNextEpisodeWatched(item) }
          )
        }
      }
    }
  }
}

@Composable
fun UpNextCard(
  item: UpNextItem,
  onShowClick: () -> Unit,
  onMarkWatched: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable { onShowClick() }
      .testTag("up_next_card_${item.showId}"),
    colors = CardDefaults.cardColors(containerColor = CinemaSurfaceCard),
    shape = RoundedCornerShape(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Poster thumbnail
        Box(
          modifier = Modifier
            .width(64.dp)
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
        ) {
          val poster = TmdbClient.posterUrl(item.posterPath)
          if (poster != null) {
            AsyncImage(
              model = poster,
              contentDescription = item.showTitle,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Icon(Icons.Default.Tv, contentDescription = null, tint = TextTertiary)
            }
          }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Show details + Episode tag
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = item.showTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(modifier = Modifier.height(4.dp))

          Surface(
            color = CineCyan.copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = "NEXT: S${item.seasonNumber} • E${item.episodeNumber}",
                color = CineCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "${item.watchedCount} / ${item.totalEpisodes} episodes watched",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 11.sp
          )
        }

        // 1-Tap Watch Action Button
        Button(
          onClick = onMarkWatched,
          colors = ButtonDefaults.buttonColors(containerColor = CineEmerald),
          shape = CircleShape,
          contentPadding = PaddingValues(0.dp),
          modifier = Modifier
            .size(46.dp)
            .testTag("mark_watched_btn_${item.showId}")
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Mark S${item.seasonNumber}E${item.episodeNumber} watched",
            tint = CinemaMidnight,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Progress bar
      LinearProgressIndicator(
        progress = { item.progressPercent },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = CineCyan,
        trackColor = Color.White.copy(alpha = 0.1f)
      )
    }
  }
}
