package com.walletconnect.ui.navgraph

sealed class Route(val path: String) {
    data object Connect : Route("chain_selection")
}