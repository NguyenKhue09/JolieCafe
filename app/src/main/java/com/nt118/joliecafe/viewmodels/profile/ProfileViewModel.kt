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
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
): AndroidViewModel(application) {

    var readBackOnline = dataStoreRepository.readBackOnline
    var readUserToken = dataStoreRepository.readUserToken
    var readIsUserDataChange = dataStoreRepository.readIsUserDataChange
    var readIsUserFaceOrGGLogin = dataStoreRepository.readIsUserFaceOrGGLogin


    var getUserInfosResponse: MutableLiveData<ApiResult<User>> = MutableLiveData()

    var userToken = ""
    var networkStatus = false
    var backOnline = false
    var isFaceOrGGLogin = false


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


//    private fun handleTokenEmpty() {
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        viewModelScope.launch {
//            if (currentUser != null && networkStatus) {
//                println("Token empty")
//                val data = mutableMapOf<String, String>()
//                val user = FirebaseAuth.getInstance().currentUser
//                data["_id"] = user!!.uid
//                data["fullname"] = user.displayName ?: ""
//                data["email"] = user.email ?: ""
//                val response = repository.remote.createUser(data = data)
//                handleGetTokenResponse(response)
//            }
//        }
//    }
//
//    private fun handleGetTokenResponse(response: Response<ApiResponseSingleData<User>>) {
//        val result = response.body()
//        when {
//            response.isSuccessful -> {
//                saveUserToken(userToken = result!!.data!!.token)
//            }
//            else -> {
//                saveUserToken("")
//            }
//        }
//    }

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