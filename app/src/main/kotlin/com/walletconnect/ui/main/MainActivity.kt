package com.walletconnect.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.walletconnect.ui.theme.WalletTheme

class MainActivity : ComponentActivity() {
    @ExperimentalMaterialNavigationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalletTheme {
                WalletApp()
            }
        }
    }
}