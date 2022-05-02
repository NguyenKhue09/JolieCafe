package com.nt118.joliecafe.viewmodels.profile_activity

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nt118.joliecafe.data.DataStoreRepository
import com.nt118.joliecafe.data.Repository
import com.nt118.joliecafe.models.ApiResponseSingleData
import com.nt118.joliecafe.models.User
import com.nt118.joliecafe.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ProfileActivityViewModel@Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
): AndroidViewModel(application) {

    var readBackOnline = dataStoreRepository.readBackOnline
    var readUserToken = dataStoreRepository.readUserToken

    var updateUserDataResponse: MutableLiveData<ApiResult<User>> = MutableLiveData()

    var userToken = ""
    var networkStatus = false
    var backOnline = false

    fun updateUserInfos(token: String, newUserData: Map<String, String>) =
        viewModelScope.launch {
            updateUserDataResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.updateUserInfos(token = token, newUserData = newUserData)
                updateUserDataResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                updateUserDataResponse.value = ApiResult.Error(e.message)
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
                ApiResult.Success(result!!.data!!)
            }
            else -> {
                ApiResult.Error(response.message())
            }
        }
    }

    private fun saveBackOnline(backOnline: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveBackOnline(backOnline)
        }

    fun saveIsUserDataChange(isDataChange: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveIsUserDataChange(isDataChange)
        }

    fun saveUserToken(userToken: String) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveUserToken(userToken = userToken)
        }

    fun showNetworkStatus() {
        if (!networkStatus) {
            Toast.makeText(getApplication(), "No Internet Connection", Toast.LENGTH_SHORT).show()
            saveBackOnline(true)
        } else if (networkStatus) {
            if (backOnline) {
                Toast.makeText(getApplication(), "We're back online", Toast.LENGTH_SHORT).show()
                saveBackOnline(false)
            }
        }
    }

}