package com.nt118.joliecafe.viewmodels.cart

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nt118.joliecafe.data.DataStoreRepository
import com.nt118.joliecafe.data.Repository
import com.nt118.joliecafe.models.ApiResponseMultiData
import com.nt118.joliecafe.models.ApiResponseSingleData
import com.nt118.joliecafe.models.CartItem
import com.nt118.joliecafe.models.CartItemByCategory
import com.nt118.joliecafe.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.http.Body
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application) {

    var readBackOnline = dataStoreRepository.readBackOnline
    var readUserToken = dataStoreRepository.readUserToken
    var userToken = ""
    var networkStatus = false
    var backOnline = false
    var cartEmptyCount: MutableStateFlow<Int> = MutableStateFlow(0) // cái này để đếm xem bao nhiêu RV trống
//    var cartCount: MutableStateFlow<Int> = MutableStateFlow(0) // bộ đếm RV chạy xong
    var numOfSelectedRv: MutableStateFlow<Int> = MutableStateFlow(0)
    var itemCount: MutableStateFlow<Int> = MutableStateFlow(0)
    var getCartItemV2Response: MutableLiveData<ApiResult<List<CartItemByCategory>>> = MutableLiveData()
    var deleteCartItemResponse: MutableLiveData<ApiResult<Unit>> = MutableLiveData()

    fun getCartItems(token: String, type: String): Flow<PagingData<CartItem>> {
        return if (token.isNotEmpty()) {
            try {
                repository.remote.getCartItems(token, type).cachedIn(viewModelScope)
            } catch (e: Exception) {
                e.printStackTrace()
                flowOf(PagingData.empty())
            }
        } else {
            flowOf(PagingData.empty())
        }
    }

    suspend fun updateCartItem(newCartItemData: Map<String, String>, token: String) {
        try {
            if (token.isEmpty()) Throwable("Unauthorized")
            repository.remote.updateCartItem(newCartItemData, token)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteCartItem(productId: String, token: String) {
        Log.d("CartViewModel", "deleteCartItem: $productId")
        viewModelScope.launch {
            deleteCartItemResponse.value = ApiResult.Loading()
            Log.d("CartViewModel", "deleteCartItem: loading")
            try {
                if (token.isEmpty()) Throwable("Unauthorized")
                val response = repository.remote.deleteCartItem(productId, token)
                deleteCartItemResponse.value = handleApiResponse(response)
                Log.d("CartViewModel", "deleteCartItem: done")
            } catch (e: Exception) {
                e.printStackTrace()
                deleteCartItemResponse.value = ApiResult.Error(e.message)
            }
        }
    }

    fun getCartItemV2(token: String) =
        viewModelScope.launch {
            getCartItemV2Response.value = ApiResult.Loading()
            try {
                val response = repository.remote.getCartItemsV2(token)
                getCartItemV2Response.value = handleApiMultiResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
                getCartItemV2Response.value = ApiResult.Error(e.message.toString())
            }
        }

    private fun handleApiMultiResponse(response: Response<ApiResponseMultiData<CartItemByCategory>>): ApiResult<List<CartItemByCategory>> {
        return when {
            response.message().toString().contains("timeout") -> {
                ApiResult.Error("Timeout")
            }

            response.code() == 500 -> {
                ApiResult.Error(response.message())
            }

            response.isSuccessful -> {
                val result = response.body()
                if (result != null) {
                    ApiResult.Success(result.data!!)
                } else {
                    ApiResult.Error("Cart not found!")
                }
            }

            else -> {
                ApiResult.Error(response.message())
            }
        }
    }

    private fun saveBackOnline(backOnline: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveBackOnline(backOnline)
        }
    }
    private fun <T> handleApiResponse(response: Response<ApiResponseSingleData<T>>): ApiResult<T> {
        return when {
            response.message().toString().contains("timeout") -> {
                ApiResult.Error("Timeout")
            }
            response.code() == 500 -> {
                ApiResult.Error(response.message())
            }
            response.isSuccessful -> {
                val result = response.body()
                if(result != null) {
                    ApiResult.NullDataSuccess()
                } else {
                    ApiResult.Error("Address not found!")
                }
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