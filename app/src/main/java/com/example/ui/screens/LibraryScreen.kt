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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.example.data.db.MediaItemEntity
import com.example.ui.CineTrackViewModel
import com.example.ui.LibraryFilter
import com.example.ui.MediaTypeFilter
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CineAmber
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineRose
import com.example.ui.theme.CinemaMidnight
import com.example.ui.theme.CinemaSurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun LibraryScreen(
  viewModel: CineTrackViewModel,
  onMediaClick: (Int, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val allTracked by viewModel.allTracked.collectAsStateWithLifecycle()
  val statusFilter by viewModel.libraryStatusFilter.collectAsStateWithLifecycle()
  val typeFilter by viewModel.libraryTypeFilter.collectAsStateWithLifecycle()

  // Apply filters
  val filteredList = allTracked.filter { item ->
    val matchesType = when (typeFilter) {
      MediaTypeFilter.ALL -> true
      MediaTypeFilter.MOVIES -> item.mediaType == "movie"
      MediaTypeFilter.TV -> item.mediaType == "tv"
    }

    val matchesStatus = when (statusFilter) {
      LibraryFilter.ALL -> true
      LibraryFilter.WATCHING -> item.status == "watching"
      LibraryFilter.PLAN_TO_WATCH -> item.status == "plan_to_watch"
      LibraryFilter.COMPLETED -> item.status == "completed"
      LibraryFilter.FAVORITES -> item.isFavorite
      LibraryFilter.DROPPED -> item.status == "dropped"
    }

    matchesType && matchesStatus
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CinemaMidnight)
  ) {
    // Header
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "My Library",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = TextPrimary
          )
          Text(
            text = "${filteredList.size} titles tracked",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }

        // Type filter pills (All / Movies / TV)
        Row(
          modifier = Modifier
            .background(CinemaSurfaceCard, RoundedCornerShape(20.dp))
            .padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          MediaTypeFilter.values().forEach { filter ->
            val isSelected = typeFilter == filter
            Surface(
              color = if (isSelected) CineCyan else Color.Transparent,
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier
                .clickable { viewModel.setLibraryTypeFilter(filter) }
                .testTag("filter_type_${filter.name}")
            ) {
              Text(
                text = filter.label,
                color = if (isSelected) CinemaMidnight else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Status horizontal chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(LibraryFilter.values()) { filter ->
          val isSelected = statusFilter == filter
          FilterChip(
            selected = isSelected,
            onClick = { viewModel.setLibraryStatusFilter(filter) },
            label = {
              Text(
                text = filter.label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              containerColor = CinemaSurfaceCard,
              labelColor = TextSecondary,
              selectedContainerColor = CineAmber,
              selectedLabelColor = CinemaMidnight
            ),
            border = null,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.testTag("status_chip_${filter.name}")
          )
        }
      }
    }

    if (filteredList.isEmpty()) {
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
            modifier = Modifier.size(72.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = null,
                tint = CineAmber,
                modifier = Modifier.size(36.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "No Items in this View",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Titles you add to your watchlist or mark as watching will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Adaptive(110.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredList, key = { "${it.id}_${it.mediaType}" }) { item ->
          TrackedMediaCard(
            item = item,
            onClick = { onMediaClick(item.id, item.mediaType) }
          )
        }
      }
    }
  }
}

@Composable
fun TrackedMediaCard(
  item: MediaItemEntity,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .width(136.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .testTag("tracked_card_${item.id}"),
    colors = CardDefaults.cardColors(containerColor = CinemaSurfaceCard),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(2f / 3f)
          .background(Color(0xFF1E293B))
      ) {
        val poster = TmdbClient.posterUrl(item.posterPath)
        if (poster != null) {
          AsyncImage(
            model = poster,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
              imageVector = if (item.mediaType == "tv") Icons.Default.Tv else Icons.Default.Movie,
              contentDescription = null,
              tint = TextTertiary,
              modifier = Modifier.size(32.dp)
            )
          }
        }

        // Top badges: Favorite and User Rating
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (item.userRating > 0) {
            Surface(
              color = CinemaMidnight.copy(alpha = 0.85f),
              shape = RoundedCornerShape(6.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
              ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = CineGold, modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                  text = "${item.userRating}/10",
                  color = TextPrimary,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          } else {
            Spacer(modifier = Modifier.width(1.dp))
          }

          if (item.isFavorite) {
            Surface(
              color = CinemaMidnight.copy(alpha = 0.85f),
              shape = CircleShape
            ) {
              Icon(
                Icons.Default.Favorite,
                contentDescription = "Favorite",
                tint = CineRose,
                modifier = Modifier
                  .size(20.dp)
                  .padding(3.dp)
              )
            }
          }
        }

        // Bottom status badge
        Box(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(4.dp)
        ) {
          StatusBadge(status = item.status)
        }
      }

      // Title & Year info
      Column(modifier = Modifier.padding(8.dp)) {
        Text(
          text = item.title,
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
            text = item.releaseDate?.take(4) ?: item.mediaType.uppercase(),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 11.sp
          )

          Text(
            text = item.mediaType.uppercase(),
            color = CineCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
