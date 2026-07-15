package com.sakurasano.reposearch.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sakurasano.reposearch.ui.RepoDetailScreen
import com.sakurasano.reposearch.ui.RepoSearchScreen
import com.sakurasano.reposearch.ui.ThemeViewModel
import kotlinx.serialization.Serializable

@Serializable
data object RepoSearchRoute

@Serializable
data class RepoDetailRoute(val owner: String, val name: String)

@Composable
fun AppNavHost(
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = RepoSearchRoute,
        modifier = modifier,
    ) {
        composable<RepoSearchRoute> {
            RepoSearchScreen(
                onRepoClick = { repo ->
                    navController.navigate(RepoDetailRoute(owner = repo.ownerName, name = repo.name))
                },
                themeViewModel = themeViewModel,
            )
        }
        composable<RepoDetailRoute> {
            RepoDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
