package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CineTrackViewModel
import com.example.ui.theme.CineAmber
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineEmerald
import com.example.ui.theme.CineGold
import com.example.ui.theme.CinePurple
import com.example.ui.theme.CinemaMidnight
import com.example.ui.theme.CinemaSurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun StatsScreen(
  viewModel: CineTrackViewModel,
  modifier: Modifier = Modifier
) {
  val stats by viewModel.stats.collectAsStateWithLifecycle()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CinemaMidnight)
  ) {
    // Header
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = "Stats & Insights",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
        color = TextPrimary
      )
      Text(
        text = "Your personal cinema & TV viewing history",
        style = MaterialTheme.typography.bodySmall,
        color = CineGold
      )
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Hero Total Time Watched Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("total_time_card"),
          colors = CardDefaults.cardColors(containerColor = CinemaSurfaceCard),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                color = CineAmber.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.AccessTime, contentDescription = null, tint = CineAmber, modifier = Modifier.size(20.dp))
                }
              }

              Spacer(modifier = Modifier.width(10.dp))

              Text(
                text = "Total Watch Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Time big numbers: Days, Hours, Minutes
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              TimeStatItem(number = stats.days.toString(), unit = "DAYS", color = CineAmber)
              TimeStatItem(number = stats.remainingHours.toString(), unit = "HOURS", color = CineCyan)
              TimeStatItem(number = stats.remainingMinutes.toString(), unit = "MINS", color = CineEmerald)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Movies vs TV time split
            val totalMins = stats.totalMinutes.coerceAtLeast(1L)
            val moviePct = (stats.totalMovieMinutes.toFloat() / totalMins).coerceIn(0f, 1f)

            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Movies: ${stats.totalMovieMinutes / 60}h",
                  style = MaterialTheme.typography.bodySmall,
                  color = CineAmber,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "TV Shows: ${stats.totalTvMinutes / 60}h",
                  style = MaterialTheme.typography.bodySmall,
                  color = CineCyan,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }

              Spacer(modifier = Modifier.height(6.dp))

              LinearProgressIndicator(
                progress = { moviePct },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = CineAmber,
                trackColor = CineCyan
              )
            }
          }
        }
      }

      // Counts Overview (Movies & Episodes)
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = CinemaSurfaceCard),
            shape = RoundedCornerShape(14.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Icon(Icons.Default.Movie, contentDescription = null, tint = CineAmber, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "${stats.totalMoviesWatched}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = TextPrimary
              )
              Text(
                text = "Movies Logged",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
              )
            }
          }

          Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = CinemaSurfaceCard),
            shape = RoundedCornerShape(14.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Icon(Icons.Default.Tv, contentDescription = null, tint = CineCyan, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "${stats.totalEpisodesWatched}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = TextPrimary
              )
              Text(
                text = "Episodes Logged",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
              )
            }
          }
        }
      }

      // Rating Distribution Histogram (1 to 10)
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("ratings_distribution_card"),
          colors = CardDefaults.cardColors(containerColor = CinemaSurfaceCard),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.BarChart, contentDescription = null, tint = CineGold, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Rating Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val maxRatingCount = stats.ratingDistribution.values.maxOrNull()?.coerceAtLeast(1) ?: 1

            (10 downTo 1).forEach { star ->
              val count = stats.ratingDistribution[star] ?: 0
              val fraction = count.toFloat() / maxRatingCount

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "$star ★",
                  color = TextSecondary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.width(36.dp)
                )

                LinearProgressIndicator(
                  progress = { fraction },
                  modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                  color = CineGold,
                  trackColor = Color.White.copy(alpha = 0.05f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                  text = "$count",
                  color = TextTertiary,
                  fontSize = 11.sp,
                  modifier = Modifier.width(24.dp)
                )
              }
            }
          }
        }
      }

      // Top Genres Section
      if (stats.topGenres.isNotEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CinemaSurfaceCard),
            shape = RoundedCornerShape(16.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Category, contentDescription = null, tint = CinePurple, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Top Genres Watched",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              val maxGenre = stats.topGenres.firstOrNull()?.second?.coerceAtLeast(1) ?: 1

              stats.topGenres.forEach { (genre, count) ->
                val fraction = count.toFloat() / maxGenre
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(text = genre, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(text = "$count titles", color = TextSecondary, fontSize = 11.sp)
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp)),
                    color = CinePurple,
                    trackColor = Color.White.copy(alpha = 0.05f)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun TimeStatItem(
  number: String,
  unit: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
  ) {
    Text(
      text = number,
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Black,
      color = color
    )
    Text(
      text = unit,
      style = MaterialTheme.typography.labelSmall,
      color = TextSecondary,
      fontWeight = FontWeight.Bold,
      fontSize = 10.sp
    )
  }
}
