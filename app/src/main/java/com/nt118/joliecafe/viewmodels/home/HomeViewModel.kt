package com.nt118.joliecafe.viewmodels.home

import android.app.Application
import androidx.lifecycle.*
import com.google.protobuf.Api
import com.nt118.joliecafe.data.DataStoreRepository
import com.nt118.joliecafe.data.Repository
import com.nt118.joliecafe.models.*
import com.nt118.joliecafe.util.ApiResult.Success
import com.nt118.joliecafe.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application) {


    var readUserToken = dataStoreRepository.readUserToken
    var getProductsResponse: MutableLiveData<ApiResult<List<Product>>> = MutableLiveData()

    var userToken = ""

    private val _momo = MutableStateFlow<ApiResult<SuspendUserMoneyResponse>>(ApiResult.Loading())
    val momo: StateFlow<ApiResult<SuspendUserMoneyResponse>> = _momo

    fun getProducts(productQuery: Map<String, String>,token: String) {
        println("Token $token")
        viewModelScope.launch {
            getProductsResponse.value = ApiResult.Loading()
            val response = repository.remote.getProduct(productQuery = productQuery, token = token)
            getProductsResponse.value = handleApiResponse(response)
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
}