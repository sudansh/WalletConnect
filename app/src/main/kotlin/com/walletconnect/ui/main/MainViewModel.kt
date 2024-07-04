package com.walletconnect.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.ui.util.DappDelegate
import com.walletconnect.ui.navgraph.AppEvents
import com.walletconnect.wcmodal.client.Modal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn

class MainViewModel : ViewModel() {
    val events = merge(DappDelegate.wcEventModels, DappDelegate.connectionState)
        .map { event ->
            when (event) {
                is Modal.Model.ConnectionState -> AppEvents.ConnectionEvent(event.isAvailable)
                is Modal.Model.DeletedSession -> AppEvents.Disconnect
                is Modal.Model.Session -> AppEvents.SessionExtend
                is Modal.Model.Error -> AppEvents.RequestError(
                    event.throwable.localizedMessage ?: "Something goes wrong"
                )

                is Modal.Model.ApprovedSession -> {
                    AppEvents.SessionApproved
                }

                else -> AppEvents.NoAction
            }
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())
}