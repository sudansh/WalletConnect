@file:OptIn(ExperimentalMaterialNavigationApi::class)

package com.walletconnect.ui.navgraph

import androidx.compose.runtime.Composable
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.walletconnect.ui.screen.connect.ConnectScreen
import com.walletconnect.wcmodal.ui.walletConnectModalGraph

@Composable
fun AppNavGraph(
    bottomSheetNavigator: BottomSheetNavigator,
    navController: NavHostController,
) {
    ModalBottomSheetLayout(bottomSheetNavigator = bottomSheetNavigator) {
        NavHost(
            navController = navController,
            startDestination = Route.Connect.path
        ) {
            composable(
                Route.Connect.path,
                deepLinks = listOf(NavDeepLink("kotlin-dapp-wc://request"))
            ) {
                ConnectScreen(navController)
            }
            walletConnectModalGraph(navController)
        }
    }
}