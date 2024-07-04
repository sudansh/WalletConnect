package com.walletconnect.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Save and retrieve data from shared preferences
 */
class PreferenceHelper(context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("walletconnect", Context.MODE_PRIVATE)

    var signedEth: String?
        get() = getString(ETH_SIGNED, null)
        set(value) {
            if (value == null) {
                sharedPref.edit().remove(ETH_SIGNED).apply()
            } else saveString(ETH_SIGNED, value)
        }

    private fun saveString(key: String, value: String) {
        with(sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }

    private fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPref.getString(key, defaultValue)
    }

    companion object {
        private const val ETH_SIGNED = "ETH_SIGNED"
    }
}