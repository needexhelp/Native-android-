package com.example.ui.connect.common

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.connectDataStore by preferencesDataStore(name = "connect_prefs")

object ConnectKeys {
    val ACTIVE_PROFILE_TYPE = stringPreferencesKey("active_profile_type")  // "PERSONAL" | "BUSINESS"
    val SELECTED_CITY       = stringPreferencesKey("selected_city")
}
