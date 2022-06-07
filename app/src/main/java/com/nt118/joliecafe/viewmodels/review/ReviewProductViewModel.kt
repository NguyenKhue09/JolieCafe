package com.nt118.joliecafe.viewmodels.review

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.nt118.joliecafe.data.DataStoreRepository
import com.nt118.joliecafe.data.Repository
import com.nt118.joliecafe.models.ApiResponseMultiData
import com.nt118.joliecafe.models.ApiResponseSingleData
import com.nt118.joliecafe.models.Comment
import com.nt118.joliecafe.models.User
import com.nt118.joliecafe.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.Exception
import javax.inject.Inject


@HiltViewModel
class ReviewProductViewModel @Inject constructor(
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

    var getCommentProductResponse: MutableLiveData<ApiResult<List<Comment>>> = MutableLiveData()

    fun getComment(token: String,productId: String) =
        viewModelScope.launch {
            getCommentProductResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.getCommentProduct(token,productId)
                getCommentProductResponse.value = handleCommentApiMultiResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
                getCommentProductResponse.value = ApiResult.Error(e.message.toString())
            }
        }

    private fun handleCommentApiMultiResponse(response: Response<ApiResponseMultiData<Comment>>): ApiResult<List<Comment>> {
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
                    ApiResult.Error("comment not found!")
                }
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
            saveBackOnline(true)
            networkMessage.value = "No Internet Connection"
        } else if (networkStatus) {
            if (backOnline) {
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