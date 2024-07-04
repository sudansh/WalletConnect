package com.walletconnect

import android.app.Application
import android.util.Log
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal

class WalletApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val projectId = "fd2458a46d601ccf26ff85ba70eebd40"
        val relayUrl = "relay.walletconnect.com"
        val serverUri = "wss://$relayUrl?projectId=${projectId}"
        val appMetaData = Core.Model.AppMetaData(
            name = "Wallet Connect",
            description = "Connect Ethreum wallet",
            url = "kotlin.app.walletconnect.com",
            icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
            redirect = "kotlin-dapp-wc://request"
        )

        CoreClient.initialize(
            relayServerUrl = serverUri,
            connectionType = ConnectionType.AUTOMATIC,
            application = this,
            metaData = appMetaData,
        ) {
        }

        WalletConnectModal.initialize(
            init = Modal.Params.Init(core = CoreClient),
        ) { error ->
            Log.e("Delegate", error.throwable.stackTraceToString())
        }
    }
}