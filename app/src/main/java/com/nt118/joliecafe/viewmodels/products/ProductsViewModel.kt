package com.nt118.joliecafe.viewmodels.products

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application) {
    var readBackOnline = dataStoreRepository.readBackOnline
    var readUserToken = dataStoreRepository.readUserToken

    private var _tabSelected = MutableLiveData<String>()
    val tabSelected: LiveData<String> = _tabSelected

    var userToken = ""

    var networkStatus = false
    var backOnline = false

    //favorite
    var getFavoriteProductResponse: MutableLiveData<ApiResult<List<FavProductId>>> = MutableLiveData()
    var addFavoriteProductResponse: MutableLiveData<ApiResult<Unit>> = MutableLiveData()
    var deleteFavoriteProductResponse: MutableLiveData<ApiResult<Unit>> = MutableLiveData()

    fun getFavorite(token: String) =
        viewModelScope.launch {
            println("getFavorite")
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
                addFavoriteProductResponse.value = handleApiResponse(response)
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
                deleteFavoriteProductResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                deleteFavoriteProductResponse.value = ApiResult.Error(e.message)
            }
        }

    private fun <T> handleApiResponse(response: Response<ApiResponseSingleData<T>>): ApiResult<T> {
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

    fun setTabSelected(tab: String) {
        _tabSelected.value = tab
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