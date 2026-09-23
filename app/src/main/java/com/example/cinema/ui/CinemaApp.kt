package com.example.cinema.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cinema.ui.favourites.FavouritesScreen
import com.example.cinema.ui.home.HomeScreen
import com.example.cinema.ui.profile.ProfileScreen
import com.example.cinema.ui.search.SearchScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.cinema.ui.details.MovieDetailsScreen

private enum class MainDestination(
    val route: String,
    val label: String,
    val symbol: String
) {
    HOME("home", "Home", "H"),
    FAVOURITES("favourites", "Favourites", "F"),
    SEARCH("search", "Search", "S"),
    PROFILE("profile", "Profile", "P")
}

@Composable
fun CinemaApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = MainDestination.entries.any { destination ->
        destination.route == currentRoute
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    MainDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(
                                        navController.graph
                                            .findStartDestination().id
                                    ) {
                                        saveState = true
                                    }

                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Text(text = destination.symbol)
                            },
                            label = {
                                Text(text = destination.label)
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainDestination.HOME.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(route = MainDestination.HOME.route) {
                HomeScreen(
                    onMovieClick = { movieId ->
                        navController.navigate("movie_details/$movieId") {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(route = MainDestination.FAVOURITES.route) {
                FavouritesScreen()
            }

            composable(route = MainDestination.SEARCH.route) {
                SearchScreen()
            }

            composable(route = MainDestination.PROFILE.route) {
                ProfileScreen()
            }
            composable(
                route = "movie_details/{movieId}",
                arguments = listOf(
                    navArgument("movieId") {
                        type = NavType.IntType
                    }
                )
            ) { entry ->
                val movieId = requireNotNull(entry.arguments)
                    .getInt("movieId")

                MovieDetailsScreen(
                    movieId = movieId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}