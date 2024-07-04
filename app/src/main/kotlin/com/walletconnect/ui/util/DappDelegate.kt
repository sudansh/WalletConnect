package com.walletconnect.ui.util

import android.util.Log
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * WalletConnectDelegate implementation
 */
object DappDelegate : WalletConnectModal.ModalDelegate, CoreClient.CoreDelegate {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _wcEventModels: MutableSharedFlow<Modal.Model?> = MutableSharedFlow()
    val wcEventModels: SharedFlow<Modal.Model?> = _wcEventModels.asSharedFlow()
    private val _coreEvents: MutableSharedFlow<Core.Model> = MutableSharedFlow()
    private val _connectionState: MutableSharedFlow<Modal.Model.ConnectionState> =
        MutableSharedFlow(replay = 1)
    val connectionState: SharedFlow<Modal.Model.ConnectionState> = _connectionState.asSharedFlow()

    var selectedSessionTopic: String? = null
        private set

    init {
        WalletConnectModal.setDelegate(this)
        CoreClient.setDelegate(this)
    }

    override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
        Log.d("Delegate", "onConnectionStateChange($state)")
        scope.launch {
            _connectionState.emit(state)
        }
    }

    override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
        Log.d("Delegate", "onSessionApproved: $approvedSession")

        selectedSessionTopic = approvedSession.topic

        scope.launch {
            _wcEventModels.emit(approvedSession)
        }
    }

    override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
        Log.d("Delegate", "onSessionRejected: $rejectedSession")
        scope.launch {
            _wcEventModels.emit(rejectedSession)
        }
    }

    override fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {
        scope.launch {
            Log.d("Delegate", "onSessionUpdate: $updatedSession")
            _wcEventModels.emit(updatedSession)
        }
    }

    override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {
    }

    override fun onSessionEvent(sessionEvent: Modal.Model.Event) {
        Log.d("Delegate", "onSessionEvent: $sessionEvent")
        scope.launch {
            _wcEventModels.emit(sessionEvent)
        }
    }

    override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
        Log.d("Delegate", "onSessionDelete: $deletedSession")
        deselectAccountDetails()

        scope.launch {
            _wcEventModels.emit(deletedSession)
        }
    }

    override fun onSessionExtend(session: Modal.Model.Session) {
        Log.d("Delegate", "onSessionExtend: $session")
        scope.launch {
            _wcEventModels.emit(session)
        }
    }

    override fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse) {
        Log.d("Delegate", "onSessionRequestResponse: $response")
        scope.launch {
            _wcEventModels.emit(response)
        }
    }

    override fun onSessionAuthenticateResponse(sessionUpdateResponse: Modal.Model.SessionAuthenticateResponse) {
        Log.d("Delegate", "onSessionAuthenticateResponse: $sessionUpdateResponse")
        if (sessionUpdateResponse is Modal.Model.SessionAuthenticateResponse.Result) {
            selectedSessionTopic = sessionUpdateResponse.session?.topic
        }
        scope.launch {
            _wcEventModels.emit(sessionUpdateResponse)
        }
    }

    override fun onProposalExpired(proposal: Modal.Model.ExpiredProposal) {
        Log.d("Delegate", "onProposalExpired: $proposal")
        scope.launch {
            _wcEventModels.emit(proposal)
        }
    }

    override fun onRequestExpired(request: Modal.Model.ExpiredRequest) {
        Log.d("Delegate", "onRequestExpired: $request")
        scope.launch {
            _wcEventModels.emit(request)
        }
    }

    fun selectTopic(topic: String) {
        selectedSessionTopic = topic
    }

    fun deselectAccountDetails() {
        selectedSessionTopic = null
    }

    override fun onError(error: Modal.Model.Error) {
        Log.d("Delegate", "onError: ${error.throwable.stackTraceToString()}")
        scope.launch {
            _wcEventModels.emit(error)
        }
    }

    override fun onPairingDelete(deletedPairing: Core.Model.DeletedPairing) {
        Log.d("Delegate", "Pairing deleted: ${deletedPairing.topic}")
    }

    override fun onPairingExpired(expiredPairing: Core.Model.ExpiredPairing) {
        Log.d("Delegate", "onPairingExpired: $expiredPairing")
        scope.launch {
            _coreEvents.emit(expiredPairing)
        }
    }

    override fun onPairingState(pairingState: Core.Model.PairingState) {
        Log.d("Delegate", "onPairingState: $pairingState")
    }
}