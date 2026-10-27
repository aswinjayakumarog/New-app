package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.api.TmdbClient
import com.example.data.api.TmdbEpisodeDto
import com.example.data.api.TmdbMediaDto
import com.example.ui.theme.CineAmber
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineEmerald
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineRose
import com.example.ui.theme.CinemaMidnight
import com.example.ui.theme.CinemaSurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun StatusBadge(
  status: String,
  modifier: Modifier = Modifier
) {
  val (color, label, icon) = when (status) {
    "watching" -> Triple(CineCyan, "Watching", Icons.Default.PlayArrow)
    "plan_to_watch" -> Triple(CineAmber, "Watchlist", Icons.Default.Bookmark)
    "completed" -> Triple(CineEmerald, "Watched", Icons.Default.CheckCircle)
    "dropped" -> Triple(CineRose, "Dropped", Icons.Default.Close)
    else -> Triple(Color.Gray, status.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }, Icons.Default.Bookmark)
  }

  Surface(
    color = color.copy(alpha = 0.2f),
    shape = RoundedCornerShape(12.dp),
    modifier = modifier.border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(12.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}

@Composable
fun MediaPosterCard(
  media: TmdbMediaDto,
  currentStatus: String? = null,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .width(136.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .testTag("media_card_${media.id}"),
    colors = CardDefaults.cardColors(containerColor = CinemaSurfaceCard),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(2f / 3f)
          .background(Color(0xFF1E293B))
      ) {
        val poster = TmdbClient.posterUrl(media.posterPath)
        if (poster != null) {
          AsyncImage(
            model = poster,
            contentDescription = media.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (media.resolvedMediaType == "tv") Icons.Default.Tv else Icons.Default.Movie,
              contentDescription = null,
              tint = TextTertiary,
              modifier = Modifier.size(36.dp)
            )
          }
        }

        // TMDB Rating badge in top-left
        if ((media.voteAverage ?: 0.0) > 0) {
          Surface(
            color = CinemaMidnight.copy(alpha = 0.85f),
            shape = RoundedCornerShape(topStart = 12.dp, bottomEnd = 8.dp),
            modifier = Modifier.align(Alignment.TopStart)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = CineGold,
                modifier = Modifier.size(11.dp)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = String.format("%.1f", media.voteAverage),
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Current status badge if tracked
        if (currentStatus != null) {
          Box(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(4.dp)
          ) {
            StatusBadge(status = currentStatus)
          }
        }
      }

      // Title & Year info
      Column(modifier = Modifier.padding(8.dp)) {
        Text(
          text = media.displayTitle,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = media.displayYear.ifBlank { media.resolvedMediaType.uppercase() },
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 11.sp
          )
          Text(
            text = media.resolvedMediaType.uppercase(),
            style = MaterialTheme.typography.bodySmall,
            color = CineCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
fun InteractiveStarRating(
  rating: Int,
  maxStars: Int = 10,
  onRatingChanged: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    for (i in 1..maxStars) {
      val isFilled = i <= rating
      IconButton(
        onClick = {
          if (rating == i) onRatingChanged(0) else onRatingChanged(i)
        },
        modifier = Modifier
          .size(32.dp)
          .testTag("star_$i")
      ) {
        Icon(
          imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
          contentDescription = "Rate $i",
          tint = if (isFilled) CineGold else TextTertiary,
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}

@Composable
fun EpisodeItemRow(
  episode: TmdbEpisodeDto,
  isWatched: Boolean,
  onToggleWatched: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .testTag("episode_row_${episode.episodeNumber}"),
    colors = CardDefaults.cardColors(
      containerColor = if (isWatched) CinemaSurfaceCard.copy(alpha = 0.6f) else CinemaSurfaceCard
    ),
    shape = RoundedCornerShape(12.dp),
    border = if (isWatched) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CineEmerald.copy(alpha = 0.4f), Color.Transparent))) else null
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Episode still image
      Box(
        modifier = Modifier
          .width(88.dp)
          .aspectRatio(16f / 9f)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF0F172A))
      ) {
        val still = TmdbClient.stillUrl(episode.stillPath)
        if (still != null) {
          AsyncImage(
            model = still,
            contentDescription = episode.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
              text = "E${episode.episodeNumber}",
              color = TextTertiary,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
        }

        if (isWatched) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(CinemaMidnight.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Watched",
              tint = CineEmerald,
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Episode details
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = CineCyan.copy(alpha = 0.15f),
            shape = RoundedCornerShape(4.dp)
          ) {
            Text(
              text = "E${episode.episodeNumber}",
              color = CineCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = episode.name ?: "Episode ${episode.episodeNumber}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isWatched) TextSecondary else TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (!episode.airDate.isNullOrBlank()) {
            Text(
              text = episode.airDate,
              style = MaterialTheme.typography.bodySmall,
              color = TextTertiary,
              fontSize = 11.sp
            )
          }
          if (episode.runtime != null && episode.runtime > 0) {
            Text(
              text = " • ${episode.runtime}m",
              style = MaterialTheme.typography.bodySmall,
              color = TextTertiary,
              fontSize = 11.sp
            )
          }
        }

        if (!episode.overview.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = episode.overview,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // 1-Tap Check toggle button (Instant tracking without scrobbling!)
      IconButton(
        onClick = onToggleWatched,
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(if (isWatched) CineEmerald.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
          .testTag("toggle_ep_${episode.episodeNumber}")
      ) {
        Icon(
          imageVector = if (isWatched) Icons.Default.CheckCircle else Icons.Default.Check,
          contentDescription = if (isWatched) "Mark unwatched" else "Mark watched",
          tint = if (isWatched) CineEmerald else TextTertiary,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
