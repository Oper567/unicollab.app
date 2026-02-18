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
import com.unicollabapp.ui.chat.DirectChatScreen
import com.unicollabapp.ui.chat.GroupChatScreen
import com.unicollabapp.ui.friends.FriendsScreen
import com.unicollabapp.ui.screens.GroupDetailsScreen
import com.unicollabapp.ui.screens.HomeScreen
import com.unicollabapp.ui.screens.LoginScreen
import com.unicollabapp.ui.screens.RegisterScreen
import com.unicollabapp.ui.tournaments.GroupTournamentsScreen
import com.unicollabapp.ui.wallet.WalletScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"

    const val GROUP_ID_ARG = "groupId"
    const val GROUP_DETAILS = "group/{$GROUP_ID_ARG}"

    const val PEER_UID_ARG = "peerUid"

    const val FRIENDS = "friends"
    const val WALLET = "wallet"
    const val PROFILE = "profile"
    const val GROUP_CHAT = "groupChat/{$GROUP_ID_ARG}"
    const val GROUP_TOURNAMENTS = "tournaments/{$GROUP_ID_ARG}"
    const val DIRECT_CHAT = "directChat/{$PEER_UID_ARG}"

    fun groupDetails(groupId: String): String = "group/${Uri.encode(groupId)}"

    fun groupChat(groupId: String): String = "groupChat/${Uri.encode(groupId)}"
    fun groupTournaments(groupId: String): String = "tournaments/${Uri.encode(groupId)}"
    fun directChat(peerUid: String): String = "directChat/${Uri.encode(peerUid)}"

    fun profile(): String = PROFILE
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
                },
                onOpenFriends = { nav.navigate(Routes.FRIENDS) },
                onOpenWallet = { nav.navigate(Routes.WALLET) },
                onOpenProfile = { nav.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.PROFILE) {
            com.unicollabapp.ui.profile.ProfileScreen(
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.FRIENDS) {
            FriendsScreen(
                onBack = { nav.popBackStack() },
                onOpenChat = { peerUid -> nav.navigate(Routes.directChat(peerUid)) }
            )
        }

        composable(Routes.WALLET) {
            WalletScreen(
                onBack = { nav.popBackStack() }
            )
        }

        composable(
            route = Routes.GROUP_DETAILS,
            arguments = listOf(navArgument(Routes.GROUP_ID_ARG) { type = NavType.StringType })
        ) { backStack ->
            val groupId = backStack.arguments?.getString(Routes.GROUP_ID_ARG) ?: return@composable
            GroupDetailsScreen(
                groupId = groupId,
                onBack = { nav.popBackStack() },
                onOpenChat = { nav.navigate(Routes.groupChat(it)) },
                onOpenTournaments = { nav.navigate(Routes.groupTournaments(it)) }
            )
        }

        composable(
            route = Routes.GROUP_CHAT,
            arguments = listOf(navArgument(Routes.GROUP_ID_ARG) { type = NavType.StringType })
        ) { backStack ->
            val groupId = backStack.arguments?.getString(Routes.GROUP_ID_ARG) ?: return@composable
            GroupChatScreen(groupId = groupId, onBack = { nav.popBackStack() })
        }

        composable(
            route = Routes.GROUP_TOURNAMENTS,
            arguments = listOf(navArgument(Routes.GROUP_ID_ARG) { type = NavType.StringType })
        ) { backStack ->
            val groupId = backStack.arguments?.getString(Routes.GROUP_ID_ARG) ?: return@composable
            GroupTournamentsScreen(groupId = groupId, onBack = { nav.popBackStack() })
        }

        composable(
            route = Routes.DIRECT_CHAT,
            arguments = listOf(navArgument(Routes.PEER_UID_ARG) { type = NavType.StringType })
        ) { backStack ->
            val peerUid = backStack.arguments?.getString(Routes.PEER_UID_ARG) ?: return@composable
            DirectChatScreen(peerUid = peerUid, onBack = { nav.popBackStack() })
        }
    }
}
