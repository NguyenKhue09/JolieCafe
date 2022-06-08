package com.nt118.joliecafe.viewmodels.home

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.data.DataStoreRepository
import com.nt118.joliecafe.data.Repository
import com.nt118.joliecafe.models.*
import com.nt118.joliecafe.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application) {

    var getUserInfosResponse: MutableLiveData<ApiResult<User>> = MutableLiveData()

    var readBackOnline = dataStoreRepository.readBackOnline
    var readUserToken = dataStoreRepository.readUserToken
    var readIsUserFaceOrGGLogin = dataStoreRepository.readIsUserFaceOrGGLogin

    var userToken = ""

    var networkStatus = false
    var backOnline = false
    var isFaceOrGGLogin = false


    fun getProducts(productQuery: Map<String, String>, token: String): Flow<PagingData<Product>> {
        return if (token.isNotEmpty()) {
            try {
                repository.remote.getProducts(
                    productQuery = productQuery,
                    token = "Bearer $token"
                ).cachedIn(viewModelScope)
            } catch (e: Exception) {
                println(e.message)
                flowOf()
            }
        } else {
            println("Token empty")
            handleTokenEmpty()
            flowOf()
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

    private fun handleTokenEmpty() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        viewModelScope.launch {
            if (currentUser != null && networkStatus) {
                println("Token empty")
                val user = FirebaseAuth.getInstance().currentUser
                val response = repository.remote.userLogin(userId = user!!.uid)
                handleGetTokenResponse(response)
            }

        }
    }

    private fun handleGetTokenResponse(response: Response<ApiResponseSingleData<User>>) {
        val result = response.body()
        when {
            response.isSuccessful -> {
                saveUserToken(userToken = result!!.data!!.token)
                saveCoin(result.data!!.coins.toString())
            }
            else -> {
                saveUserToken("")
            }
        }
    }

    private fun saveUserToken(userToken: String) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveUserToken(userToken = userToken)
        }

    private fun saveCoin(coin: String) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveCoin(coin = coin)
        }

    private fun saveBackOnline(backOnline: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveBackOnline(backOnline)
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