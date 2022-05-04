package com.nt118.joliecafe.viewmodels.favorite

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.data.DataStoreRepository
import com.nt118.joliecafe.data.Repository
import com.nt118.joliecafe.models.ApiResponseSingleData
import com.nt118.joliecafe.models.FavoriteProduct
import com.nt118.joliecafe.models.User
import com.nt118.joliecafe.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application) {

    var readBackOnline = dataStoreRepository.readBackOnline
    var readUserToken = dataStoreRepository.readUserToken

    val removeUserFavoriteProductResponse: MutableLiveData<ApiResult<Unit>> = MutableLiveData()

    private var _tabSelected = MutableLiveData<String>()
    val tabSelected: LiveData<String> = _tabSelected

    var userToken = ""

    var networkStatus = false
    var backOnline = false


    fun getUserFavoriteProducts(
        productQuery: Map<String, String>,
        token: String
    ): Flow<PagingData<FavoriteProduct>> {
        return if (token.isNotEmpty()) {
            try {
                repository.remote.getUserFavoriteProducts(
                    productQuery = productQuery,
                    token = token
                ).cachedIn(viewModelScope)
            } catch (e: Exception) {
                e.printStackTrace()
                flowOf()
            }
        } else {
            println("Token empty")
            handleTokenEmpty()
            flowOf()
        }
    }

    fun removeUserFavoriteProduct(token: String, favoriteProductId: String) =
        viewModelScope.launch {
            removeUserFavoriteProductResponse.value = ApiResult.Loading()
            try {
                if (token.isEmpty()) Throwable("Unauthorized")
                val response = repository.remote.removeUserFavoriteProduct(
                    token = token,
                    favoriteProductId = favoriteProductId
                )
                removeUserFavoriteProductResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                removeUserFavoriteProductResponse.value = ApiResult.Error(e.message)
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
                val data = mutableMapOf<String, String>()
                val user = FirebaseAuth.getInstance().currentUser
                data["_id"] = user!!.uid
                data["fullname"] = user.displayName ?: ""
                data["email"] = user.email ?: ""
                val response = repository.remote.createUser(data = data)
                handleGetTokenResponse(response)
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