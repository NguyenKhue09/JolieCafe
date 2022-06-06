package com.nt118.joliecafe.viewmodels.profile

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.data.DataStoreRepository
import com.nt118.joliecafe.data.Repository
import com.nt118.joliecafe.models.ApiResponseSingleData
import com.nt118.joliecafe.models.User
import com.nt118.joliecafe.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application) {

    var readBackOnline = dataStoreRepository.readBackOnline
    var readUserToken = dataStoreRepository.readUserToken
    var readIsUserDataChange = dataStoreRepository.readIsUserDataChange
    var readIsUserFaceOrGGLogin = dataStoreRepository.readIsUserFaceOrGGLogin

    val networkMessage = MutableLiveData<String>()

    var getUserInfosResponse: MutableLiveData<ApiResult<User>> = MutableLiveData()
    var removeUserNoticeTokenResponse: MutableLiveData<ApiResult<Unit>> = MutableLiveData()

    var userToken = ""
    var networkStatus = false
    var backOnline = false
    var isFaceOrGGLogin = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            readUserToken.collectLatest { token ->
                println(token)
                userToken = token
            }
        }
    }

    fun getUserInfos(token: String) =
        viewModelScope.launch {
            getUserInfosResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.getUserInfos(token = "Bearer $token")
                getUserInfosResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                getUserInfosResponse.value = ApiResult.Error(e.message)
            }
        }

    fun removeUserNoticeToken() = viewModelScope.launch {
        removeUserNoticeTokenResponse.value = ApiResult.Loading()
        try {
            val response = repository.remote.removeUserNoticeToken(token = userToken)
            removeUserNoticeTokenResponse.value =
                handleNullDataSuccessApiResponse(response = response)
        } catch (e: Exception) {
            e.printStackTrace()
            removeUserNoticeTokenResponse.value = ApiResult.Error(e.message)
        }
    }

    private fun handleApiResponse(response: Response<ApiResponseSingleData<User>>): ApiResult<User> {
        val result = response.body()
        return when {
            response.message().toString().contains("timeout") -> {
                ApiResult.Error("Timeout")
            }
            response.code() == 500 -> {
                ApiResult.Error(response.message())
            }
            response.isSuccessful -> {
                //saveUserToken(result!!.data!!.token)
                ApiResult.Success(result!!.data!!)
            }
            else -> {
                ApiResult.Error(response.message())
            }
        }
    }

    private fun handleNullDataSuccessApiResponse(response: Response<ApiResponseSingleData<Unit>>): ApiResult<Unit> {
        return when {
            response.message().toString().contains("timeout") -> {
                ApiResult.Error("Timeout")
            }
            response.code() == 500 -> {
                ApiResult.Error(response.message())
            }
            response.isSuccessful -> {
                ApiResult.NullDataSuccess()
            }
            else -> {
                ApiResult.Error(response.message())
            }
        }
    }


    fun saveUserToken(userToken: String) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveUserToken(userToken = userToken)
        }

    private fun saveBackOnline(backOnline: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveBackOnline(backOnline)
        }

    fun saveIsUserDataChange(isDataChange: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveIsUserDataChange(isDataChange)
        }

    fun showNetworkStatus() {
        if (!networkStatus) {
            saveBackOnline(true)
            networkMessage.value = "No Internet Connection"
        } else if (networkStatus) {
            if (backOnline) {
                networkMessage.value = "We're back online"
                saveBackOnline(false)
            }
        }
    }

}