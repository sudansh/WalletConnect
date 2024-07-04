package com.walletconnect.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable

@Composable
internal fun WalletTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        content = {
            Surface(content = content)
        }
    )
}