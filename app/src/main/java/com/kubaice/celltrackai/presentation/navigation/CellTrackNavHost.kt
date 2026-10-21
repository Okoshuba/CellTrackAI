package com.kubaice.celltrackai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kubaice.celltrackai.presentation.screens.culturedetail.CultureDetailScreen
import com.kubaice.celltrackai.presentation.screens.culturedetail.CultureDetailViewModel
import com.kubaice.celltrackai.presentation.screens.cultureform.CreateEditCultureScreen
import com.kubaice.celltrackai.presentation.screens.cultureform.CreateEditCultureViewModel
import com.kubaice.celltrackai.presentation.screens.culturelist.CultureListScreen
import com.kubaice.celltrackai.presentation.screens.culturelist.CultureListViewModel
import com.kubaice.celltrackai.presentation.screens.eventform.CreateEditEventScreen
import com.kubaice.celltrackai.presentation.screens.eventform.CreateEditEventViewModel
import com.kubaice.celltrackai.presentation.screens.home.HomeScreen
import com.kubaice.celltrackai.presentation.screens.imagepreview.ImagePreviewScreen
import com.kubaice.celltrackai.presentation.screens.literature.LiteratureSearchScreen
import com.kubaice.celltrackai.presentation.screens.literature.LiteratureSearchViewModel

@Composable
fun CellTrackNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Home Screen
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToCultureList = {
                    navController.navigate(Screen.CultureList.route)
                },
                onNavigateToCultureDetail = { cultureId ->
                    navController.navigate(Screen.CultureDetail.createRoute(cultureId))
                },
                onNavigateToCreateCulture = {
                    navController.navigate(Screen.CreateEditCulture.createRoute())
                }
            )
        }

        // Culture List Screen
        composable(route = Screen.CultureList.route) {
            val viewModel: CultureListViewModel = hiltViewModel()
            CultureListScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { cultureId ->
                    navController.navigate(Screen.CultureDetail.createRoute(cultureId))
                },
                onNavigateToCreateCulture = {
                    navController.navigate(Screen.CreateEditCulture.createRoute())
                }
            )
        }

        // Culture Detail Screen
        composable(
            route = Screen.CultureDetail.route,
            arguments = listOf(
                navArgument("cultureId") { type = NavType.StringType }
            )
        ) {
            val viewModel: CultureDetailViewModel = hiltViewModel()
            CultureDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditCulture = { cultureId ->
                    navController.navigate(Screen.CreateEditCulture.createRoute(cultureId))
                },
                onNavigateToAddEvent = { cultureId ->
                    navController.navigate(Screen.CreateEditEvent.createRoute(cultureId))
                },
                onNavigateToLiteratureSearch = { cultureId ->
                    navController.navigate(Screen.LiteratureSearch.createRoute(cultureId))
                },
                onNavigateToImagePreview = { imageUri ->
                    navController.navigate(Screen.ImagePreview.createRoute(imageUri))
                }
            )
        }

        // Create / Edit Culture Screen
        composable(
            route = Screen.CreateEditCulture.route,
            arguments = listOf(
                navArgument("cultureId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val viewModel: CreateEditCultureViewModel = hiltViewModel()
            CreateEditCultureScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Create / Edit Event Screen
        composable(
            route = Screen.CreateEditEvent.route,
            arguments = listOf(
                navArgument("cultureId") { type = NavType.StringType },
                navArgument("eventId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val viewModel: CreateEditEventViewModel = hiltViewModel()
            CreateEditEventScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Literature Search Screen
        composable(
            route = Screen.LiteratureSearch.route,
            arguments = listOf(
                navArgument("cultureId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val viewModel: LiteratureSearchViewModel = hiltViewModel()
            LiteratureSearchScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Fullscreen Image Preview Screen
        composable(
            route = Screen.ImagePreview.route,
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            ImagePreviewScreen(
                imageUri = imageUri,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
