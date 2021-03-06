package com.nt118.joliecafe.viewmodels.sign_up

import android.app.Application
import android.widget.Toast
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
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel@Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application) {

    var readBackOnline = dataStoreRepository.readBackOnline

    var createUserResponse: MutableLiveData<ApiResult<User>> = MutableLiveData()
    var userLoginGGOrFaceResponse: MutableLiveData<ApiResult<User>> = MutableLiveData()

    var networkStatus = false
    var backOnline = false

    fun createUser(userData: Map<String, String>) =
        viewModelScope.launch {
            createUserResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.createUser(data = userData)
                createUserResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                createUserResponse.value = ApiResult.Error(e.message)
            }

        }

    fun userLogin(userId: String) =
        viewModelScope.launch {
            userLoginGGOrFaceResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.userLogin(userId = userId)
                userLoginGGOrFaceResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                userLoginGGOrFaceResponse.value = ApiResult.Error(e.message)
            }
        }

    private fun saveBackOnline(backOnline: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveBackOnline(backOnline)
        }

    fun saveIsUserFaceOrGGLogin(isUserFaceOrGGLogin: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveIsUserFaceOrGGLogin(isUserFaceOrGGLogin)
        }

    private fun saveUserToken(userToken: String) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveUserToken(userToken = userToken)
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
                saveUserToken(result!!.data!!.token)
                ApiResult.Success(result.data!!)
            }
            else -> {
                ApiResult.Error(response.message())
            }
        }
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