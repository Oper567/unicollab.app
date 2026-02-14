package com.unicollabapp.ui.nav

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.unicollabapp.ui.screens.GroupDetailsScreen
import com.unicollabapp.ui.screens.HomeScreen
import com.unicollabapp.ui.screens.LoginScreen
import com.unicollabapp.ui.screens.RegisterScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"

    const val GROUP_ID_ARG = "groupId"
    const val GROUP_DETAILS = "group/{$GROUP_ID_ARG}"

    fun groupDetails(groupId: String): String = "group/${Uri.encode(groupId)}"
}

@Composable
fun AppNav() {
    val nav = rememberNavController()

    // Start at home if already logged in
    val start = if (FirebaseAuth.getInstance().currentUser != null) Routes.HOME else Routes.LOGIN

    AppNavHost(nav = nav, startDestination = start)
}

@Composable
private fun AppNavHost(
    nav: NavHostController,
    startDestination: String
) {
    NavHost(navController = nav, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onGoRegister = {
                    nav.navigate(Routes.REGISTER) {
                        launchSingleTop = true
                    }
                },
                onSuccess = {
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onGoLogin = { nav.popBackStack() },
                onSuccess = {
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onLogoutToAuth = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onOpenGroup = { groupId ->
                    nav.navigate(Routes.groupDetails(groupId))
                }
            )
        }

        composable(
            route = Routes.GROUP_DETAILS,
            arguments = listOf(navArgument(Routes.GROUP_ID_ARG) { type = NavType.StringType })
        ) { backStack ->
            val groupId = backStack.arguments?.getString(Routes.GROUP_ID_ARG) ?: return@composable
            GroupDetailsScreen(
                groupId = groupId,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
