package com.nt118.joliecafe.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.nt118.joliecafe.util.Constants.Companion.PREFERENCES_BACK_ONLINE
import com.nt118.joliecafe.util.Constants.Companion.PREFERENCES_COIN
import com.nt118.joliecafe.util.Constants.Companion.PREFERENCES_IS_USER_DATA_CHANGE
import com.nt118.joliecafe.util.Constants.Companion.PREFERENCES_NAME
import com.nt118.joliecafe.util.Constants.Companion.PREFERENCES_USER_AUTH_TYPE
import com.nt118.joliecafe.util.Constants.Companion.PREFERENCES_USER_DEFAULT_ADDRESS_ID
import com.nt118.joliecafe.util.Constants.Companion.PREFERENCES_USER_NOTICE_TOKEN
import com.nt118.joliecafe.util.Constants.Companion.PREFERENCES_USER_TOKEN
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
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
        val defaultAddressId = stringPreferencesKey(PREFERENCES_USER_DEFAULT_ADDRESS_ID)
        val userNoticeToken = stringPreferencesKey(PREFERENCES_USER_NOTICE_TOKEN)
        val coin = intPreferencesKey(PREFERENCES_COIN)
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

    suspend fun saveCoin(coin: Int) {
        Log.d("get", "Save coin")
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.coin] = coin
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

    suspend fun saveUserDefaultAddressId(addressId: String) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.defaultAddressId] = addressId
        }
    }

    suspend fun saveUserNoticeToken(token: String) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.userNoticeToken] = token
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
        }.distinctUntilChanged()

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
        }.distinctUntilChanged()

    val readCoin: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw  exception
            }
        }
        .map { preferences ->
            val coin = preferences[PreferenceKeys.coin] ?: 0
            coin
        }.distinctUntilChanged()

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
        }.distinctUntilChanged()

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
        }.distinctUntilChanged()

    val readUserDefaultAddressId: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw  exception
            }
        }
        .map { preferences ->
            val defaultAddressId = preferences[PreferenceKeys.defaultAddressId] ?: ""
            defaultAddressId
        }.distinctUntilChanged()

    val readUserNoticeToken: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw  exception
            }
        }
        .map { preferences ->
            val defaultAddressId = preferences[PreferenceKeys.userNoticeToken] ?: ""
            defaultAddressId
        }.distinctUntilChanged()
}