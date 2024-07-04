package com.walletconnect.ui.screen.connect

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.data.PreferenceHelper
import com.walletconnect.domain.getPersonalSignBody
import com.walletconnect.ui.navgraph.AppEvents
import com.walletconnect.ui.util.DappDelegate
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import com.walletconnect.web3.modal.presets.Web3ModalChainsPresets
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

val ETH = Web3ModalChainsPresets.ethChains.values.first()

private const val PERSONAL_SIGN_METHOD = "personal_sign"

@Immutable
data class ConnectUiState(
    val isLoading: Boolean = false,
    val signedUri: Uri? = null,
    val signedAddress: String? = null
)

class ConnectViewModel(application: Application) : AndroidViewModel(application) {
    private val preferenceHelper = PreferenceHelper(application)

    private val _uiState = MutableStateFlow(
        ConnectUiState(
            signedAddress = preferenceHelper.signedEth
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _sessionEvents: MutableSharedFlow<AppEvents> = MutableSharedFlow()
    val sessionEvent: SharedFlow<AppEvents>
        get() = _sessionEvents.asSharedFlow()

    val walletEvents = DappDelegate.wcEventModels.map { walletEvent: Modal.Model? ->
        when (walletEvent) {
            is Modal.Model.ApprovedSession -> AppEvents.SessionApproved
            is Modal.Model.RejectedSession -> AppEvents.SessionRejected
            is Modal.Model.SessionAuthenticateResponse -> {
                if (walletEvent is Modal.Model.SessionAuthenticateResponse.Result) {
                    AppEvents.SessionAuthenticateApproved(if (walletEvent.session == null) "Authenticated successfully!" else null)
                } else {
                    AppEvents.SessionAuthenticateRejected
                }
            }

            is Modal.Model.SessionRequestResponse -> {
                val request = when (walletEvent.result) {
                    is Modal.Model.JsonRpcResponse.JsonRpcResult -> {
                        _uiState.update { it.copy(isLoading = false) }
                        val successResult =
                            (walletEvent.result as Modal.Model.JsonRpcResponse.JsonRpcResult)
                        _uiState.update { it.copy(signedAddress = DappDelegate.selectedSessionTopic.orEmpty()) }
                        preferenceHelper.signedEth = DappDelegate.selectedSessionTopic
                        AppEvents.RequestSuccess(successResult.result)

                    }

                    is Modal.Model.JsonRpcResponse.JsonRpcError -> {
                        _uiState.update { it.copy(isLoading = false) }
                        disconnect()
                        AppEvents.RequestPeerError("Error in signing")
                    }
                }

                _sessionEvents.emit(request)
            }

            is Modal.Model.ExpiredProposal -> AppEvents.ProposalExpired

            else -> AppEvents.NoAction
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    fun checkSessions() {
        viewModelScope.launch {
            val activeSession = WalletConnectModal.getListOfActiveSessions().firstOrNull()
            if (activeSession == null) {
                preferenceHelper.signedEth = null
                _uiState.update { it.copy(signedAddress = null) }
                return@launch
            }
            DappDelegate.selectTopic(activeSession.topic)
            if (activeSession.topic == preferenceHelper.signedEth) {
                // show connected
                _uiState.update {
                    it.copy(signedAddress = activeSession.topic)
                }
            } else {
                disconnect()
            }

        }
    }

    fun requestSignin() {
        requestMethod { uri ->
            _uiState.update { it.copy(signedUri = uri) }
        }
    }

    fun disconnect() {
        if (DappDelegate.selectedSessionTopic != null) {
            try {
                viewModelScope.launch { _sessionEvents.emit(AppEvents.DisconnectLoading) }
                val disconnectParams =
                    Modal.Params.Disconnect(sessionTopic = requireNotNull(DappDelegate.selectedSessionTopic))
                WalletConnectModal.disconnect(disconnectParams,
                    onSuccess = {
                        Log.d("Delegate", "disconnect success ")
                        preferenceHelper.signedEth = null
                        _uiState.update { it.copy(signedAddress = null) }
                        DappDelegate.deselectAccountDetails()
                        viewModelScope.launch {
                            _sessionEvents.emit(AppEvents.Disconnect)
                        }
                    },
                    onError = { error ->
                        Log.e("xxx disconnect", error.throwable.stackTraceToString())
                        viewModelScope.launch {
                            _sessionEvents.emit(
                                AppEvents.DisconnectError(
                                    error.throwable.message
                                        ?: "Unknown error, please try again or contact support"
                                )
                            )
                        }
                    })

            } catch (e: Exception) {
                viewModelScope.launch {
                    _sessionEvents.emit(
                        AppEvents.DisconnectError(
                            e.message ?: "Unknown error, please try again or contact support"
                        )
                    )
                }
            }
        }
    }

    fun awaitingProposalResponse(isAwaiting: Boolean) {
        _uiState.update { it.copy(isLoading = isAwaiting) }
    }

    fun getSessionParams() = Modal.Params.SessionParams(
        requiredNamespaces = getNamespaces(),
        properties = getProperties()
    )

    private fun getNamespaces(): Map<String, Modal.Model.Namespace.Proposal> {
        return mapOf(
            ETH.chainNamespace to Modal.Model.Namespace.Proposal(
                chains = listOf("${ETH.chainNamespace}:${ETH.chainReference}"),
                methods = listOf(PERSONAL_SIGN_METHOD),
                events = ETH.events
            )
        )
    }

    private fun getProperties(): Map<String, String> {
        //note: this property is not used in the SDK, only for demonstration purposes
        val expiry =
            (System.currentTimeMillis() / 1000) + TimeUnit.SECONDS.convert(7, TimeUnit.DAYS)
        return mapOf("sessionExpiry" to "$expiry")
    }

    private fun requestMethod(
        sendSessionRequestDeepLink: (Uri) -> Unit
    ) {
        DappDelegate.selectedSessionTopic?.let { topic ->
            try {
                _uiState.update { it.copy(isLoading = true) }
                val (parentChain, chainId, account) = Triple(
                    ETH.chainNamespace, ETH.chainReference, topic
                )
                val params: String = getPersonalSignBody(account)
                val requestParams = Modal.Params.Request(
                    sessionTopic = requireNotNull(DappDelegate.selectedSessionTopic),
                    method = PERSONAL_SIGN_METHOD,
                    params = params,
                    chainId = "$parentChain:$chainId"
                )

                WalletConnectModal.request(requestParams,
                    onSuccess = {
                        WalletConnectModal.getActiveSessionByTopic(requestParams.sessionTopic)?.redirect?.toUri()
                            ?.let { deepLinkUri -> sendSessionRequestDeepLink(deepLinkUri) }
                    },
                    onError = {
                        viewModelScope.launch {
                            _uiState.update { it.copy(isLoading = false) }
                            _sessionEvents.emit(
                                AppEvents.RequestError(
                                    it.throwable.localizedMessage ?: "Error trying to send request"
                                )
                            )
                        }
                    })
            } catch (e: Exception) {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = false) }
                    _sessionEvents.emit(
                        AppEvents.RequestError(
                            e.localizedMessage ?: "Error trying to send request"
                        )
                    )
                }
            }
        }
    }
}