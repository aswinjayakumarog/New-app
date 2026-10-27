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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.CineTrackViewModel
import com.example.ui.components.EpisodeItemRow
import com.example.ui.components.InteractiveStarRating
import com.example.ui.components.StatusBadge
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
fun MediaDetailScreen(
  viewModel: CineTrackViewModel,
  id: Int,
  mediaType: String,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val detailState by viewModel.detailState.collectAsStateWithLifecycle()
  var notesText by remember { mutableStateOf("") }
  var isEditingNotes by remember { mutableStateOf(false) }

  val tracked = detailState.trackedEntity
  val title = detailState.movieDetails?.title ?: detailState.tvDetails?.name ?: ""
  val backdropPath = detailState.movieDetails?.backdropPath ?: detailState.tvDetails?.backdropPath
  val posterPath = detailState.movieDetails?.posterPath ?: detailState.tvDetails?.posterPath
  val overview = detailState.movieDetails?.overview ?: detailState.tvDetails?.overview ?: ""
  val releaseDate = detailState.movieDetails?.releaseDate ?: detailState.tvDetails?.firstAirDate ?: ""
  val voteAvg = detailState.movieDetails?.voteAverage ?: detailState.tvDetails?.voteAverage ?: 0.0
  val tagline = detailState.movieDetails?.tagline ?: detailState.tvDetails?.tagline

  val isMovie = mediaType == "movie"
  val runtime = if (isMovie) detailState.movieDetails?.runtime else detailState.tvDetails?.episodeRunTime?.firstOrNull()

  // Sync notes text when tracked entity loads
  remember(tracked?.userNotes) {
    notesText = tracked?.userNotes.orEmpty()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CinemaMidnight)
  ) {
    if (detailState.isLoading) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = CineAmber)
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
      ) {
        // Hero Backdrop with Back Button and Gradient
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(280.dp)
          ) {
            val backdrop = TmdbClient.backdropUrl(backdropPath, "original")
            if (backdrop != null) {
              AsyncImage(
                model = backdrop,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            } else {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(CinemaSurfaceCard)
              )
            }

            // Gradient fade to midnight
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    listOf(
                      CinemaMidnight.copy(alpha = 0.4f),
                      CinemaMidnight.copy(alpha = 0.7f),
                      CinemaMidnight
                    )
                  )
                )
            )

            // Back button
            IconButton(
              onClick = onBackClick,
              modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(CinemaMidnight.copy(alpha = 0.7f))
                .testTag("detail_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
              )
            }

            // Favorite button
            IconButton(
              onClick = {
                viewModel.toggleFavorite(id, mediaType, tracked?.isFavorite == true)
              },
              modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(CinemaMidnight.copy(alpha = 0.7f))
                .testTag("detail_fav_button")
            ) {
              Icon(
                imageVector = if (tracked?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (tracked?.isFavorite == true) CineRose else TextPrimary
              )
            }
          }
        }

        // Header Info & Poster Overlay
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp)
              .padding(top = 0.dp),
            verticalAlignment = Alignment.Bottom
          ) {
            // Poster
            Box(
              modifier = Modifier
                .width(105.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(CinemaSurfaceCard)
            ) {
              val poster = TmdbClient.posterUrl(posterPath)
              if (poster != null) {
                AsyncImage(
                  model = poster,
                  contentDescription = title,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title & Meta info
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )

              if (!tagline.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "“$tagline”",
                  style = MaterialTheme.typography.bodySmall,
                  color = CineAmber,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
              }

              Spacer(modifier = Modifier.height(6.dp))

              Row(verticalAlignment = Alignment.CenterVertically) {
                if (releaseDate.isNotBlank()) {
                  Text(
                    text = releaseDate.take(4),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp
                  )
                  Text(text = " • ", color = TextTertiary, fontSize = 12.sp)
                }

                if (runtime != null && runtime > 0) {
                  Text(
                    text = "${runtime}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp
                  )
                  Text(text = " • ", color = TextTertiary, fontSize = 12.sp)
                }

                Surface(
                  color = CineGold.copy(alpha = 0.15f),
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                  ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = CineGold, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                      text = String.format("%.1f", voteAvg),
                      color = TextPrimary,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
        }

        // Status Selection Buttons (Watching, Watchlist, Watched, Dropped)
        item {
          Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
              text = "Watch Status",
              style = MaterialTheme.typography.labelMedium,
              color = TextSecondary,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              val currentStatus = tracked?.status

              listOf(
                Triple("watching", "Watching", CineCyan),
                Triple("plan_to_watch", "Watchlist", CineAmber),
                Triple("completed", "Watched", CineEmerald),
                Triple("dropped", "Dropped", CineRose)
              ).forEach { (statusKey, label, color) ->
                val isSelected = currentStatus == statusKey
                Surface(
                  color = if (isSelected) color else CinemaSurfaceCard,
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.updateCurrentDetailStatus(statusKey) }
                    .testTag("status_btn_$statusKey")
                ) {
                  Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = label,
                      color = if (isSelected) CinemaMidnight else TextPrimary,
                      fontSize = 12.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
        }

        // Personal Star Rating Bar (1 to 10)
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaSurfaceCard),
            shape = RoundedCornerShape(12.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Your Rating",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )

                Text(
                  text = if ((tracked?.userRating ?: 0) > 0) "${tracked?.userRating} / 10" else "Tap star to rate",
                  style = MaterialTheme.typography.bodySmall,
                  color = if ((tracked?.userRating ?: 0) > 0) CineGold else TextTertiary,
                  fontWeight = FontWeight.Bold
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              InteractiveStarRating(
                rating = tracked?.userRating ?: 0,
                maxStars = 10,
                onRatingChanged = { newRating ->
                  viewModel.updateRating(id, mediaType, newRating)
                },
                modifier = Modifier.fillMaxWidth()
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
        }

        // Synopsis / Overview
        if (overview.isNotBlank()) {
          item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
              Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = overview,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 20.sp
              )
            }

            Spacer(modifier = Modifier.height(16.dp))
          }
        }

        // TV SHOWS ONLY: Seasons & Episodes Progress Tracker (Trakt/Simkl experience)
        if (!isMovie && detailState.tvDetails != null) {
          val tv = detailState.tvDetails!!
          val seasons = tv.seasons.filter { (it.seasonNumber ?: 0) > 0 }

          item {
            Column(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "Episode Tracker",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                  )
                  Text(
                    text = "${detailState.watchedEpisodes.size} episodes logged",
                    style = MaterialTheme.typography.bodySmall,
                    color = CineCyan
                  )
                }

                // Bulk season watched button
                OutlinedButton(
                  onClick = {
                    viewModel.markSeasonWatched(detailState.selectedSeasonNumber)
                  },
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = CineEmerald),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                  modifier = Modifier.testTag("mark_season_watched_btn")
                ) {
                  Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Mark Season Watched", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Season Selector Tabs
              if (seasons.isNotEmpty()) {
                ScrollableTabRow(
                  selectedTabIndex = seasons.indexOfFirst { it.seasonNumber == detailState.selectedSeasonNumber }.coerceAtLeast(0),
                  containerColor = CinemaSurfaceCard,
                  contentColor = CineCyan,
                  edgePadding = 16.dp,
                  indicator = { tabPositions ->
                    val index = seasons.indexOfFirst { it.seasonNumber == detailState.selectedSeasonNumber }.coerceAtLeast(0)
                    if (index < tabPositions.size) {
                      TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = CineCyan
                      )
                    }
                  }
                ) {
                  seasons.forEach { season ->
                    val isSelected = season.seasonNumber == detailState.selectedSeasonNumber
                    Tab(
                      selected = isSelected,
                      onClick = { viewModel.selectSeason(season.seasonNumber) },
                      text = {
                        Text(
                          text = "Season ${season.seasonNumber}",
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                          color = if (isSelected) CineCyan else TextSecondary,
                          fontSize = 13.sp
                        )
                      }
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
          }

          // Episodes List for Selected Season
          if (detailState.isLoadingSeason) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(100.dp),
                contentAlignment = Alignment.Center
              ) {
                CircularProgressIndicator(color = CineCyan, modifier = Modifier.size(24.dp))
              }
            }
          } else {
            items(detailState.seasonEpisodes, key = { it.id }) { ep ->
              val isWatched = detailState.watchedEpisodes.any {
                it.seasonNumber == ep.seasonNumber && it.episodeNumber == ep.episodeNumber
              }
              EpisodeItemRow(
                episode = ep,
                isWatched = isWatched,
                onToggleWatched = { viewModel.toggleEpisodeWatched(ep) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
              )
            }
          }

          item {
            Spacer(modifier = Modifier.height(20.dp))
          }
        }

        // Personal Notes Section
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaSurfaceCard),
            shape = RoundedCornerShape(12.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Personal Notes & Review",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )

              Spacer(modifier = Modifier.height(8.dp))

              OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                placeholder = { Text("Add private notes, favorite quotes, or thoughts…", color = TextTertiary, fontSize = 13.sp) },
                colors = TextFieldDefaults.colors(
                  focusedContainerColor = CinemaMidnight,
                  unfocusedContainerColor = CinemaMidnight,
                  focusedTextColor = TextPrimary,
                  unfocusedTextColor = TextPrimary,
                  focusedIndicatorColor = CineCyan,
                  unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(90.dp)
                  .testTag("notes_input")
              )

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
              ) {
                Button(
                  onClick = { viewModel.updateNotes(id, mediaType, notesText.ifBlank { null }) },
                  colors = ButtonDefaults.buttonColors(containerColor = CineCyan),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Text("Save Notes", color = CinemaMidnight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))
        }

        // Remove from Tracking Option
        if (tracked != null) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
              contentAlignment = Alignment.Center
            ) {
              OutlinedButton(
                onClick = {
                  viewModel.removeTracking(id, mediaType)
                  onBackClick()
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CineRose),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("remove_tracking_btn")
              ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Remove from Library", color = CineRose, fontSize = 13.sp)
              }
            }
            Spacer(modifier = Modifier.height(30.dp))
          }
        }
      }
    }
  }
}
