package com.example.ui.partners.common

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.partnerDataStore by preferencesDataStore("partner_prefs")

object PartnerKeys {
    fun identityCode(type: PartnerType) = stringPreferencesKey("${type.name}_identity_code")
    fun passwordHash(type: PartnerType)  = stringPreferencesKey("${type.name}_password_hash")
    fun partnerName(type: PartnerType)   = stringPreferencesKey("${type.name}_name")
    fun partnerPhone(type: PartnerType)  = stringPreferencesKey("${type.name}_phone")
    fun isRegistered(type: PartnerType)  = booleanPreferencesKey("${type.name}_registered")
}
