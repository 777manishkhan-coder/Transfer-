package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.EngineSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bolindia_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val PREFER_FORMAL_TONE = booleanPreferencesKey("prefer_formal_tone")
        val AUTO_SHOW_PHONETICS = booleanPreferencesKey("auto_show_phonetics")
        val OFFLINE_FIRST_MODE = booleanPreferencesKey("offline_first_mode")
        val AUTO_COLLECT_CHAT = booleanPreferencesKey("auto_collect_chat")
        val INSTALLED_PACK_CODES = stringSetPreferencesKey("installed_pack_codes")
    }

    val settingsFlow: Flow<EngineSettings> = context.dataStore.data.map { prefs ->
        EngineSettings(
            preferFormalTone = prefs[Keys.PREFER_FORMAL_TONE] ?: true,
            autoShowPhonetics = prefs[Keys.AUTO_SHOW_PHONETICS] ?: true,
            offlineFirstMode = prefs[Keys.OFFLINE_FIRST_MODE] ?: true,
            autoCollectFromChat = prefs[Keys.AUTO_COLLECT_CHAT] ?: false,
            installedPackCodes = prefs[Keys.INSTALLED_PACK_CODES] ?: setOf("EN", "HI", "TA", "TE")
        )
    }

    suspend fun updatePreferFormalTone(value: Boolean) {
        context.dataStore.edit { it[Keys.PREFER_FORMAL_TONE] = value }
    }

    suspend fun updateAutoShowPhonetics(value: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SHOW_PHONETICS] = value }
    }

    suspend fun updateOfflineFirstMode(value: Boolean) {
        context.dataStore.edit { it[Keys.OFFLINE_FIRST_MODE] = value }
    }

    suspend fun updateAutoCollectFromChat(value: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_COLLECT_CHAT] = value }
    }

    suspend fun toggleOfflinePack(code: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.INSTALLED_PACK_CODES] ?: setOf("EN", "HI", "TA", "TE")
            val updated = if (code in current && code != "EN") {
                current - code
            } else {
                current + code
            }
            prefs[Keys.INSTALLED_PACK_CODES] = updated
        }
    }
}
