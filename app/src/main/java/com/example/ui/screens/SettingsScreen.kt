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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.TmdbClient
import com.example.ui.CineTrackViewModel
import com.example.ui.theme.CineAmber
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineEmerald
import com.example.ui.theme.CineRose
import com.example.ui.theme.CinemaMidnight
import com.example.ui.theme.CinemaSurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun SettingsScreen(
  viewModel: CineTrackViewModel,
  modifier: Modifier = Modifier
) {
  val allTracked by viewModel.allTracked.collectAsStateWithLifecycle()
  var apiKeyInput by remember { mutableStateOf(TmdbClient.customApiKey ?: "") }
  var showSavedBanner by remember { mutableStateOf(false) }
  var showClearDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CinemaMidnight)
  ) {
    // Header
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = "Settings",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
        color = TextPrimary
      )
      Text(
        text = "TMDB configuration & data management",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
      )
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // TMDB API Configuration Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = CinemaSurfaceCard),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                color = CineCyan.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.Key, contentDescription = null, tint = CineCyan, modifier = Modifier.size(20.dp))
                }
              }

              Spacer(modifier = Modifier.width(10.dp))

              Column {
                Text(
                  text = "TMDB API Key",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CineEmerald, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Connected & Active",
                    color = CineEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "The app has a default TMDB API key enabled for live metadata, cast, and episode browsing. You can enter your own TMDB v3 API key below if you prefer:",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = apiKeyInput,
              onValueChange = {
                apiKeyInput = it
                showSavedBanner = false
              },
              placeholder = { Text("Enter your custom TMDB API Key…", color = TextTertiary, fontSize = 13.sp) },
              colors = TextFieldDefaults.colors(
                focusedContainerColor = CinemaMidnight,
                unfocusedContainerColor = CinemaMidnight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = CineCyan,
                unfocusedIndicatorColor = Color.Transparent
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("tmdb_key_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (showSavedBanner) {
                Text(
                  text = "✓ Key saved & reloaded",
                  color = CineEmerald,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              } else {
                Spacer(modifier = Modifier.width(1.dp))
              }

              Button(
                onClick = {
                  viewModel.updateCustomApiKey(apiKeyInput)
                  showSavedBanner = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = CineCyan),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("save_api_key_btn")
              ) {
                Text("Save Key", color = CinemaMidnight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
        }
      }

      // Storage & Data Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = CinemaSurfaceCard),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                color = CineAmber.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(Icons.Default.Storage, contentDescription = null, tint = CineAmber, modifier = Modifier.size(20.dp))
                }
              }

              Spacer(modifier = Modifier.width(10.dp))

              Column {
                Text(
                  text = "Local Storage & Data",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Text(
                  text = "${allTracked.size} media records saved locally",
                  color = TextSecondary,
                  fontSize = 12.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "All your watch progress, custom ratings, episode checkoffs, and notes are securely stored on your device via Room Database without scrobbling.",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
              onClick = { showClearDialog = true },
              colors = ButtonDefaults.outlinedButtonColors(contentColor = CineRose),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("clear_data_btn")
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Reset Library Data", color = CineRose, fontSize = 12.sp)
            }
          }
        }
      }

      // About Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = CinemaSurfaceCard),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Info, contentDescription = null, tint = CineCyan, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "About CineTrack",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "CineTrack gives you the tracking power of Trakt and Simkl — manual episode checkoffs, Up Next queue, watchlists, ratings, and stats — completely scrobble-free and powered by the TMDB community database.",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              lineHeight = 18.sp
            )
          }
        }
      }
    }
  }

  // Clear Confirmation Dialog
  if (showClearDialog) {
    AlertDialog(
      onDismissRequest = { showClearDialog = false },
      title = { Text("Reset Library Data?", color = TextPrimary) },
      text = { Text("This will delete all tracked movies, TV shows, and logged episodes from your local device.", color = TextSecondary) },
      confirmButton = {
        Button(
          onClick = {
            viewModel.clearAllData()
            showClearDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = CineRose)
        ) {
          Text("Reset", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      },
      containerColor = CinemaSurfaceCard
    )
  }
}
