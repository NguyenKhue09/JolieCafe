package com.nt118.joliecafe.viewmodels.detail_product

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.data.DataStoreRepository
import com.nt118.joliecafe.data.Repository
import com.nt118.joliecafe.models.*
import com.nt118.joliecafe.util.ApiResult
import com.nt118.joliecafe.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.Exception
import javax.inject.Inject


@HiltViewModel
class DetailProductViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application) {
    var readBackOnline = dataStoreRepository.readBackOnline
    var readUserToken = dataStoreRepository.readUserToken
    val networkMessage = MutableLiveData<String>()

    var userToken = ""

    var networkStatus = false
    var backOnline = false

    init {
        viewModelScope.launch {
            readUserToken.collectLatest { token ->
                println(token)
                userToken = token
            }
        }
    }

    //detail product
    var getDetailProductResponse: MutableLiveData<ApiResult<Product>> = MutableLiveData()

    fun getDetailProduct(token: String, productId: String) =
        viewModelScope.launch {
            println("m có qua đây không tl")
            getDetailProductResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.getDetailFavoriteProductsId(token = token, productId = productId)
                getDetailProductResponse.value = handleApiResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
                getDetailProductResponse.value = ApiResult.Error(e.message.toString())
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
                ApiResult.Success(data = result?.data!!)
            }
            else -> {
                ApiResult.Error(response.message())
            }
        }
    }


    var getFavoriteProductResponse: MutableLiveData<ApiResult<List<FavProductId>>> = MutableLiveData()
    var addFavoriteProductResponse: MutableLiveData<ApiResult<Unit>> = MutableLiveData()
    var deleteFavoriteProductResponse: MutableLiveData<ApiResult<Unit>> = MutableLiveData()

    fun getFavorite(token: String) =
        viewModelScope.launch {
            getFavoriteProductResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.getUserFavoriteProductsId(token)
                getFavoriteProductResponse.value = handleApiMultiResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
                getFavoriteProductResponse.value = ApiResult.Error(e.message.toString())
            }
        }

    private fun handleApiMultiResponse(response: Response<ApiResponseMultiData<FavProductId>>): ApiResult<List<FavProductId>> {
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
                    ApiResult.Error("favorite not found!")
                }
            }

            else -> {
                ApiResult.Error(response.message())
            }
        }
    }

    fun addFavoriteProduct(token: String, productId: String)  =
        viewModelScope.launch {
            addFavoriteProductResponse.value = ApiResult.Loading()
            try {
                if (token.isEmpty()) Throwable("Unauthorized")
                val response = repository.remote.addUserFavoriteProduct(token = token, productId = productId)
                addFavoriteProductResponse.value = handleApiResponseFavorite(response)
            } catch (e: Exception) {
                e.printStackTrace()
                addFavoriteProductResponse.value = ApiResult.Error(e.message)
            }
        }


    fun removeFavoriteProduct(token: String, productId: String) =
        viewModelScope.launch {
            deleteFavoriteProductResponse.value = ApiResult.Loading()
            try {
                if (token.isEmpty()) handleTokenEmpty()
                val response = repository.remote.removeUserFavoriteProductByProductId(
                    token = token,
                    productId = productId
                )
                deleteFavoriteProductResponse.value = handleApiResponseFavorite(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                deleteFavoriteProductResponse.value = ApiResult.Error(e.message)
            }
        }

    private fun <T> handleApiResponseFavorite(response: Response<ApiResponseSingleData<T>>): ApiResult<T> {
        println(response)
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

    //comment


    // add cart
    var addCartResponse: MutableLiveData<ApiResult<Unit>> = MutableLiveData()

    fun addCart(data: Map<String, String>, token: String)  =
        viewModelScope.launch {
            addCartResponse.value = ApiResult.Loading()
            try {
                if (token.isEmpty()) Throwable("Unauthorized")
                val response = repository.remote.addCart(data = data, token = token)
                addCartResponse.value = handleApiResponseFavorite(response)
            } catch (e: Exception) {
                e.printStackTrace()
                addCartResponse.value = ApiResult.Error(e.message)
            }
        }


    //    products
    fun getProducts(productQuery: Map<String, String>, token: String): Flow<PagingData<Product>> {
        return if (token.isNotEmpty()) {
            try {
                repository.remote.getProducts(
                    productQuery = productQuery,
                    token = "Bearer $token"
                )
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



    private fun handleTokenEmpty() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        viewModelScope.launch {
            if (currentUser != null && networkStatus) {
                println("Token empty")
                val user = FirebaseAuth.getInstance().currentUser
                user?.let {
                    val response = repository.remote.userLogin(userId = user.uid)
                    handleGetTokenResponse(response)
                }
            }
        }
    }

    private fun handleGetTokenResponse(response: Response<ApiResponseSingleData<User>>) {
        val result = response.body()
        when {
            response.isSuccessful -> {
                saveUserToken(userToken = result!!.data!!.token)
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

    private fun saveBackOnline(backOnline: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveBackOnline(backOnline)
        }

    fun showNetworkStatus() {
        if (!networkStatus) {
            //Toast.makeText(getApplication(), "No Internet Connection", Toast.LENGTH_SHORT).show()
            saveBackOnline(true)
            networkMessage.value = "No Internet Connection"
        } else if (networkStatus) {
            if (backOnline) {
                //Toast.makeText(getApplication(), "We're back online", Toast.LENGTH_SHORT).show()
                networkMessage.value = "We're back online"
                saveBackOnline(false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("onCleared")
    }
}