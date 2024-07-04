package com.walletconnect.ui.screen.connect

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walletconnect.ui.navgraph.AppEvents
import com.walletconnect.wcmodal.client.WalletConnectModal
import com.walletconnect.wcmodal.ui.openWalletConnectModal
import com.walletconnect.wcmodal.ui.state.rememberModalState
import timber.log.Timber

@Composable
fun ConnectScreen(
    navController: NavController,
    viewModel: ConnectViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    rememberModalState(navController = navController)
    handleSignEvents(
        viewModel = viewModel,
        context = context,
        onAvailable = viewModel::checkSessions,
        onAuthenticateReject = {
        }
    )
    LaunchedEffect(Unit) {
        viewModel.sessionEvent.collect { event ->
            when (event) {
                is AppEvents.Disconnect -> {
                    viewModel.checkSessions()
                }

                is AppEvents.DisconnectError -> {
                    viewModel.checkSessions()
                    Toast.makeText(context, "Error: ${event.message}", Toast.LENGTH_SHORT).show()
                }

                is AppEvents.DisconnectLoading -> {

                }

                is AppEvents.RequestPeerError -> {
                    Toast.makeText(context, event.errorMsg, Toast.LENGTH_SHORT).show()
                }

                else -> Unit
            }
        }
    }
    LaunchedEffect(uiState.signedUri) {
        uiState.signedUri?.let {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, uiState.signedUri))
            } catch (e: Exception) {
                Timber.tag("AccountRoute").d("Activity not found: $e")
            }
        }
    }
    ConnectContent(
        uiState = uiState,
        onConnectClick = { onConnectClick(viewModel, navController) },
        onDisconnect = viewModel::disconnect
    )
}

@Composable
private fun ConnectContent(
    uiState: ConnectUiState,
    onConnectClick: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (uiState.isLoading) {
            Loader()
        } else {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!uiState.signedAddress.isNullOrEmpty()) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Address: ${uiState.signedAddress}",
                        style = MaterialTheme.typography.subtitle1
                    )
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        onClick = onDisconnect
                    ) {
                        Text(text = "Disconnect")
                    }
                } else {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        onClick = onConnectClick
                    ) {
                        Text(text = "Sign in with Ethereum")
                    }
                }
            }
        }
    }
}

@Composable
private fun handleSignEvents(
    viewModel: ConnectViewModel,
    context: Context,
    onAuthenticateReject: () -> Unit,
    onAvailable: () -> Unit,
) {
    var isFirstCheckDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.walletEvents.collect { event ->
            when (event) {
                AppEvents.SessionApproved -> {
                    viewModel.awaitingProposalResponse(false)
                    viewModel.requestSignin()
                }

                AppEvents.SessionRejected -> {
                    viewModel.awaitingProposalResponse(false)
                    Toast.makeText(context, "Session has been rejected", Toast.LENGTH_SHORT).show()
                }

                AppEvents.ProposalExpired -> {
                    viewModel.awaitingProposalResponse(false)
                    Toast.makeText(context, "Proposal has been expired", Toast.LENGTH_SHORT).show()
                }

                is AppEvents.SessionAuthenticateApproved -> {
                    viewModel.awaitingProposalResponse(false)
                    if (event.message != null) {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.checkSessions()
                    }
                }

                AppEvents.SessionAuthenticateRejected -> {
                    viewModel.awaitingProposalResponse(false)
                    onAuthenticateReject()
                    Toast.makeText(
                        context,
                        "Session authenticate has been rejected",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is AppEvents.ConnectionEvent -> {
                    if (!isFirstCheckDone && event.isAvailable) {
                        isFirstCheckDone = true
                        onAvailable()
                    }
                }

                else -> Unit
            }
        }
    }
}

private fun onConnectClick(
    viewModel: ConnectViewModel,
    navController: NavController
) {
    WalletConnectModal.setSessionParams(viewModel.getSessionParams())
    navController.openWalletConnectModal()
}

@Composable
private fun BoxScope.Loader() {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .clip(RoundedCornerShape(34.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            strokeWidth = 8.dp,
            modifier = Modifier
                .size(75.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Awaiting response...",
            maxLines = 1,
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
            )
        )
    }
}

@Preview
@Composable
private fun PreviewConnect() {
    ConnectContent(
        uiState = ConnectUiState(
            isLoading = false,
            signedUri = null,
            signedAddress = "null"
        ),
        onConnectClick = {},
        onDisconnect = {}
    )
}