package com.nt118.joliecafe.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
class MainActivityViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application) {

    var readUserToken = dataStoreRepository.readUserToken
    var reaUserNoticeToken = dataStoreRepository.readUserNoticeToken

    var updateUserNoticeTokenResponse: MutableLiveData<ApiResult<User>> = MutableLiveData()

    var userToken = ""
    var userNoticeToken = ""


    init {
        viewModelScope.launch(Dispatchers.IO) {
            launch(Dispatchers.IO) {
                readUserToken.collectLatest { token ->
                    println("token read $token")
                    userToken = token
                }
            }
            launch(Dispatchers.IO) {
                reaUserNoticeToken.collectLatest { token ->
                    println("notice token read $token")
                    userNoticeToken = token
                }
            }
        }
    }

    fun updateUserNoticeToken(notificationToken: String) =
        viewModelScope.launch {
            updateUserNoticeTokenResponse.value = ApiResult.Loading()
            println("Tokennnnnnnnnnnnnnnnnnnnnnnnnn $userToken")
            try {
                val response = repository.remote.updateUserNoticeToken(userToken, notificationToken)
                updateUserNoticeTokenResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                updateUserNoticeTokenResponse.value = ApiResult.Error(e.message)
            }
        }

    fun saveUserNoticeToken(token: String) = viewModelScope.launch {
        dataStoreRepository.saveUserNoticeToken(token)
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
                ApiResult.Success(result?.data!!)
            }
            else -> {
                ApiResult.Error(response.message())
            }
        }
    }

}