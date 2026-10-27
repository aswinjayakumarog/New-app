package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CineTrackViewModel
import com.example.ui.Screen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.MediaDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.UpNextScreen
import com.example.ui.theme.CineAmber
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineTrackTheme
import com.example.ui.theme.CinemaMidnight
import com.example.ui.theme.CinemaSurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

sealed class NavItem(
  val screen: Screen,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val tag: String
) {
  object Discover : NavItem(Screen.Discover, "Discover", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_discover")
  object UpNext : NavItem(Screen.UpNext, "Up Next", Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle, "nav_up_next")
  object Library : NavItem(Screen.Library, "Library", Icons.Filled.Bookmarks, Icons.Outlined.Bookmarks, "nav_library")
  object Stats : NavItem(Screen.Stats, "Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_stats")
  object Settings : NavItem(Screen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

class MainActivity : ComponentActivity() {

  private val viewModel: CineTrackViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      CineTrackTheme {
        CineTrackApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun CineTrackApp(viewModel: CineTrackViewModel) {
  val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
  val upNextItems by viewModel.upNextList.collectAsStateWithLifecycle()

  // Handle Android hardware/gesture back press
  BackHandler(enabled = currentScreen is Screen.MediaDetail || currentScreen != Screen.Discover) {
    if (!viewModel.navigateBack()) {
      viewModel.navigateTo(Screen.Discover)
    }
  }

  val navItems = listOf(
    NavItem.Discover,
    NavItem.UpNext,
    NavItem.Library,
    NavItem.Stats,
    NavItem.Settings
  )

  val showBottomNav = currentScreen !is Screen.MediaDetail

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = CinemaMidnight,
    bottomBar = {
      if (showBottomNav) {
        NavigationBar(
          containerColor = CinemaSurfaceDark,
          contentColor = TextPrimary,
          tonalElevation = 8.dp
        ) {
          navItems.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
              selected = isSelected,
              onClick = { viewModel.navigateTo(item.screen) },
              icon = {
                if (item is NavItem.UpNext && upNextItems.isNotEmpty()) {
                  BadgedBox(
                    badge = {
                      Badge(
                        containerColor = CineCyan,
                        contentColor = CinemaMidnight
                      ) {
                        Text(
                          text = upNextItems.size.toString(),
                          fontWeight = FontWeight.Bold,
                          fontSize = 10.sp
                        )
                      }
                    }
                  ) {
                    Icon(
                      imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                      contentDescription = item.label,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                } else {
                  Icon(
                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                    modifier = Modifier.size(22.dp)
                  )
                }
              },
              label = {
                Text(
                  text = item.label,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CinemaMidnight,
                selectedTextColor = CineAmber,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = CineAmber
              ),
              modifier = Modifier.testTag(item.tag)
            )
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (val screen = currentScreen) {
        is Screen.Discover -> {
          DiscoverScreen(
            viewModel = viewModel,
            onMediaClick = { id, type -> viewModel.openMediaDetails(id, type) }
          )
        }
        is Screen.UpNext -> {
          UpNextScreen(
            viewModel = viewModel,
            onShowClick = { showId -> viewModel.openMediaDetails(showId, "tv") }
          )
        }
        is Screen.Library -> {
          LibraryScreen(
            viewModel = viewModel,
            onMediaClick = { id, type -> viewModel.openMediaDetails(id, type) }
          )
        }
        is Screen.Stats -> {
          StatsScreen(viewModel = viewModel)
        }
        is Screen.Settings -> {
          SettingsScreen(viewModel = viewModel)
        }
        is Screen.MediaDetail -> {
          MediaDetailScreen(
            viewModel = viewModel,
            id = screen.id,
            mediaType = screen.mediaType,
            onBackClick = { viewModel.navigateBack() }
          )
        }
      }
    }
  }
}
