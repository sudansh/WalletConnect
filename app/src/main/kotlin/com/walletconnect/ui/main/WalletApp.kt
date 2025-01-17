@file:OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialNavigationApi::class)

package com.walletconnect.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.app.R
import com.walletconnect.ui.navgraph.AppEvents
import com.walletconnect.ui.navgraph.AppNavGraph
import com.walletconnect.ui.navgraph.Route
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WalletApp(viewModel: MainViewModel = viewModel()) {
    val scaffoldState: ScaffoldState = rememberScaffoldState()
    var isOfflineState: Boolean? by remember { mutableStateOf(null) }
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val bottomSheetNavigator = BottomSheetNavigator(sheetState)
    val navController = rememberNavController(bottomSheetNavigator)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AppEvents.ConnectionEvent -> isOfflineState = !event.isAvailable
                is AppEvents.Disconnect -> navController.navigate(Route.Connect.path)
                is AppEvents.RequestError -> scaffoldState.snackbarHostState.showSnackbar(
                    event.exceptionMsg
                )

                is AppEvents.SessionExtend -> scaffoldState.snackbarHostState.showSnackbar("Session extended")
                else -> Unit
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = {
            if (isOfflineState != null) {
                if (isOfflineState == true) {
                    NoConnectionIndicator()
                } else {
                    RestoredConnectionIndicator()
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AppNavGraph(
                bottomSheetNavigator = bottomSheetNavigator,
                navController = navController,
            )
        }
    }
}

@Composable
private fun NoConnectionIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF3496ff))
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(text = "No internet connection", color = Color.White)
        Spacer(modifier = Modifier.width(4.dp))
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_offline),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(color = Color.White)
        )
    }
}

@Composable
private fun RestoredConnectionIndicator() {
    var shouldShow by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = Unit) {
        delay(2000)
        shouldShow = false
    }
    if (shouldShow) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF93c47d))
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(text = "Network connection is OK", color = Color.White)
            Spacer(modifier = Modifier.width(4.dp))
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_white_check),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(color = Color.White)
            )
        }
    }
}