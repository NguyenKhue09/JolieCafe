package com.nt118.joliecafe.viewmodels.home

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.protobuf.Api
import com.nt118.joliecafe.data.DataStoreRepository
import com.nt118.joliecafe.data.Repository
import com.nt118.joliecafe.models.*
import com.nt118.joliecafe.ui.activities.login.LoginActivity
import com.nt118.joliecafe.util.ApiResult.Success
import com.nt118.joliecafe.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application) {

    var readBackOnline = dataStoreRepository.readBackOnline
    var readUserToken = dataStoreRepository.readUserToken
    var getProductsResponse: MutableLiveData<ApiResult<List<Product>>> = MutableLiveData()

    var userToken = ""

    var networkStatus = false
    var backOnline = false


    fun getProducts(productQuery: Map<String, String>, token: String) {
        println("Token $token")
        if (token.isNotEmpty()) {
            viewModelScope.launch {
                getProductsResponse.value = ApiResult.Loading()
                try {
                    val response = repository.remote.getProduct(
                        productQuery = productQuery,
                        token = "Bearer $token"
                    )
                    getProductsResponse.value = handleApiResponse(response)
                } catch (e: Exception) {
                    getProductsResponse.value = ApiResult.Error("Product not found.")
                    println(e.message)
                }
            }
        } else {
            handleTokenEmpty()
        }

    }

    private fun handleTokenEmpty() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        viewModelScope.launch {
            if (currentUser != null && networkStatus) {
                println("Token empty")
                val data = hashMapOf<String, Any>()
                val user = FirebaseAuth.getInstance().currentUser
                data["_id"] = user!!.uid
                data["fullname"] = user.displayName ?: ""
                data["email"] = user.email ?: ""
                val response = repository.remote.createUser(data = data)
                handleGetTokenResponse(response)
            }
        }
    }

    private fun handleApiResponse(response: retrofit2.Response<ApiResponseMultiData<Product>>): ApiResult<List<Product>> {
        val result = response.body()
        return when {
            response.message().toString().contains("timeout") -> {
                ApiResult.Error("Timeout")
            }
            response.code() == 500 -> {
                ApiResult.Error(response.message())
            }
            response.isSuccessful -> {
                ApiResult.Success(result!!.data)
            }
            else -> {
                ApiResult.Error(response.message())
            }
        }
    }

    private fun handleGetTokenResponse(response: retrofit2.Response<ApiResponseSingleData<User>>) {
        val result = response.body()
        when {
            response.isSuccessful -> {
                saveUserToken(userToken = result!!.data!!.token)
            }
            else -> {
                getProductsResponse.value = ApiResult.Error("Unauthorized. Please try login again.")
                saveUserToken("")
            }
        }
    }

    private fun saveUserToken(userToken: String) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveUserToken(userToken = userToken)
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