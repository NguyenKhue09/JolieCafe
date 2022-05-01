package com.nt118.joliecafe.viewmodels.address_book

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nt118.joliecafe.data.DataStoreRepository
import com.nt118.joliecafe.data.Repository
import com.nt118.joliecafe.models.Address
import com.nt118.joliecafe.models.ApiResponseSingleData
import com.nt118.joliecafe.models.User
import com.nt118.joliecafe.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class AddressBookViewModel@Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
): AndroidViewModel(application) {

    var readBackOnline = dataStoreRepository.readBackOnline
    var readUserToken = dataStoreRepository.readUserToken

    var addNewAddressResponse: MutableLiveData<ApiResult<Address>> = MutableLiveData()
    var addNewDefaultAddressResponse: MutableLiveData<ApiResult<User>> = MutableLiveData()

    var userToken = ""
    var networkStatus = false
    var backOnline = false

    fun getAddresses(token: String): Flow<PagingData<Address>> {
        println(token)
        return if(token.isNotEmpty()) {
            try {
                repository.remote.getAddresses(token = token).cachedIn(viewModelScope)
            } catch (e: Exception) {
                e.printStackTrace()
                flowOf()
            }

        } else {
            flowOf()
        }
    }


    fun addNewAddress(data: Map<String, String>, token: String)  =
        viewModelScope.launch {
            addNewAddressResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.addNewAddress(data = data, token = token)
                addNewAddressResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                addNewAddressResponse.value = ApiResult.Error(e.message)
            }
        }

    fun addNewDefaultAddress(data: Map<String, String>, token: String)  =
        viewModelScope.launch {
            addNewDefaultAddressResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.addNewDefaultAddress(data = data, token = token)
                addNewDefaultAddressResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                addNewDefaultAddressResponse.value = ApiResult.Error(e.message)
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
                    ApiResult.Success(result.data!!)
                } else {
                    ApiResult.Error("Address not found!")
                }
            }
            else -> {
                ApiResult.Error(response.message())
            }
        }
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