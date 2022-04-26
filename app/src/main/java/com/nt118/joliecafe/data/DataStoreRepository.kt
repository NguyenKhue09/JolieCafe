package com.nt118.joliecafe.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.nt118.joliecafe.util.Constants.Companion.PREFERENCES_BACK_ONLINE
import com.nt118.joliecafe.util.Constants.Companion.PREFERENCES_NAME
import com.nt118.joliecafe.util.Constants.Companion.PREFERENCES_USER_TOKEN
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject


private val Context.dataStore by preferencesDataStore(
    name = PREFERENCES_NAME
)

@ViewModelScoped
class DataStoreRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private object PreferenceKeys {
        val backOnline = booleanPreferencesKey(PREFERENCES_BACK_ONLINE)
        val userToken = stringPreferencesKey(PREFERENCES_USER_TOKEN)
    }
    private val dataStore: DataStore<Preferences> = context.dataStore

    suspend fun saveBackOnline(backOnline: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.backOnline] = backOnline
        }
    }

    suspend fun saveUserToken(userToken: String) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.userToken] = userToken
        }
    }

    val readBackOnline: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw  exception
            }
        }
        .map { preferences ->
            val backOnline = preferences[PreferenceKeys.backOnline] ?: false
            backOnline
        }

    val readUserToken: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw  exception
            }
        }
        .map { preferences ->
            val userToken = preferences[PreferenceKeys.userToken] ?: ""
            userToken
        }
}