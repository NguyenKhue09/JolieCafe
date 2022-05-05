package com.nt118.joliecafe.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.nt118.joliecafe.util.Constants.Companion.PREFERENCES_BACK_ONLINE
import com.nt118.joliecafe.util.Constants.Companion.PREFERENCES_IS_USER_DATA_CHANGE
import com.nt118.joliecafe.util.Constants.Companion.PREFERENCES_NAME
import com.nt118.joliecafe.util.Constants.Companion.PREFERENCES_USER_AUTH_TYPE
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
        val isFaceOrGGLogin = booleanPreferencesKey(PREFERENCES_USER_AUTH_TYPE)
        val isUserDataChange = booleanPreferencesKey(PREFERENCES_IS_USER_DATA_CHANGE)
    }

    private val dataStore: DataStore<Preferences> = context.dataStore

    suspend fun saveBackOnline(backOnline: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.backOnline] = backOnline
        }
    }

    suspend fun saveUserToken(userToken: String) {
        Log.d("get", "Save token")
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.userToken] = userToken
        }
    }

    suspend fun saveIsUserFaceOrGGLogin(isFaceOrGGLogin: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.isFaceOrGGLogin] = isFaceOrGGLogin
        }
    }

    suspend fun saveIsUserDataChange(isDataChange: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.isUserDataChange] = isDataChange
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

    val readIsUserFaceOrGGLogin: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw  exception
            }
        }
        .map { preferences ->
            val isFaceOrGGLogin = preferences[PreferenceKeys.isFaceOrGGLogin] ?: false
            isFaceOrGGLogin
        }

    val readIsUserDataChange: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw  exception
            }
        }
        .map { preferences ->
            val isUserDataChange = preferences[PreferenceKeys.isUserDataChange] ?: false
            isUserDataChange
        }
}