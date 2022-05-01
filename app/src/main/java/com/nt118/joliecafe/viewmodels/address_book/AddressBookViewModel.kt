package com.nt118.joliecafe.viewmodels.address_book

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nt118.joliecafe.data.DataStoreRepository
import com.nt118.joliecafe.data.Repository
import com.nt118.joliecafe.models.Address
import com.nt118.joliecafe.models.ApiResponseSingleData
import com.nt118.joliecafe.models.User
import com.nt118.joliecafe.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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

    var userToken = ""
    var networkStatus = false
    var backOnline = false


    fun addNewAddress(data: Map<String, String>, token: String)  =
        viewModelScope.launch {
            addNewAddressResponse.value = ApiResult.Loading()
            val response = repository.remote.addNewAddress(data = data, token = token)
            addNewAddressResponse.value = handleApiResponse(response = response)
        }

    private fun handleApiResponse(response: Response<ApiResponseSingleData<Address>>): ApiResult<Address>? {
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