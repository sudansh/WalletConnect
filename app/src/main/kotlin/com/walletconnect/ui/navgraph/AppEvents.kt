package com.walletconnect.ui.navgraph


sealed class AppEvents {

    data object SessionApproved : AppEvents()

    data object SessionRejected : AppEvents()

    data class SessionAuthenticateApproved(val message: String?) : AppEvents()
    data object SessionAuthenticateRejected : AppEvents()
    data object Disconnect : AppEvents()
    data class DisconnectError(val message: String) : AppEvents()

    data object DisconnectLoading : AppEvents()

    data class RequestPeerError(val errorMsg: String) : AppEvents()

    data class RequestError(val exceptionMsg: String) : AppEvents()

    data object NoAction : AppEvents()

    data class RequestSuccess(val result: String) : AppEvents()

    data object SessionExtend : AppEvents()

    data class ConnectionEvent(val isAvailable: Boolean) : AppEvents()


    data object ProposalExpired : AppEvents()
}