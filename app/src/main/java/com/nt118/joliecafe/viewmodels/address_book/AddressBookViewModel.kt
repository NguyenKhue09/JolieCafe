package com.nt118.joliecafe.viewmodels.address_book

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
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
    var readDefaultAddress = dataStoreRepository.readUserDefaultAddressId

    var addNewAddressResponse: MutableLiveData<ApiResult<Address>> = MutableLiveData()
    var addNewDefaultAddressResponse: MutableLiveData<ApiResult<User>> = MutableLiveData()
    var deleteAddressResponse: MutableLiveData<ApiResult<Address>> = MutableLiveData()
    var updateAddressResponse: MutableLiveData<ApiResult<Address>> = MutableLiveData()
    var updateUserDataResponse: MutableLiveData<ApiResult<User>> = MutableLiveData()
    private var _updateAddressStatus: MutableLiveData<Boolean> = MutableLiveData()
    val updateAddressStatus: LiveData<Boolean> = _updateAddressStatus

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

    fun getAddresses(token: String): Flow<PagingData<Address>> {
        println(token)
        return if(token.isNotEmpty()) {
            try {
                repository.remote.getAddresses(token = token).cachedIn(viewModelScope)
            } catch (e: Exception) {
                e.printStackTrace()
                flowOf(PagingData.empty())
            }

        } else {
            flowOf(PagingData.empty())
        }
    }


    fun addNewAddress(data: Map<String, String>, token: String)  =
        viewModelScope.launch {
            addNewAddressResponse.value = ApiResult.Loading()
            try {
                if (token.isEmpty()) Throwable("Unauthorized")
                val response = repository.remote.addNewAddress(data = data, token = token)
                addNewAddressResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                addNewAddressResponse.value = ApiResult.Error(e.message)
            }
        }

    fun deleteAddress(addressId: String, token: String)  =
        viewModelScope.launch {
            deleteAddressResponse.value = ApiResult.Loading()
            try {
                if (token.isEmpty()) Throwable("Unauthorized")
                val response = repository.remote.deleteAddress(addressId = addressId, token = token)
                deleteAddressResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                deleteAddressResponse.value = ApiResult.Error(e.message)
            }
        }

    fun updateAddress(newAddressData: Map<String, String>, token: String)  =
        viewModelScope.launch {
            updateAddressResponse.value = ApiResult.Loading()
            try {
                if (token.isEmpty()) Throwable("Unauthorized")
                val response = repository.remote.updateAddress(newAddressData = newAddressData, token = token)
                updateAddressResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                updateAddressResponse.value = ApiResult.Error(e.message)
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

    fun updateUserInfos(token: String, newUserData: Map<String, String>) =
        viewModelScope.launch {
            updateUserDataResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.updateUserInfos(token = token, newUserData = newUserData)
                println(response)
                updateUserDataResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                updateUserDataResponse.value = ApiResult.Error(e.message)
            }
        }

    fun setUpdateAddressStatus(status: Boolean) {
        _updateAddressStatus.value = status
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

    fun saveDefaultAddressId(defaultAddressId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveUserDefaultAddressId(addressId = defaultAddressId)
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
}