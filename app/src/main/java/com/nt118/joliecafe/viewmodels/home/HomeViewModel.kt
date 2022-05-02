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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    var userToken = ""

    var networkStatus = false
    var backOnline = false


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

    private fun handleGetTokenResponse(response: retrofit2.Response<ApiResponseSingleData<User>>) {
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