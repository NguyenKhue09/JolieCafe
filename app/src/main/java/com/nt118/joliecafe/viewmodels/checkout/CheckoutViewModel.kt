package com.nt118.joliecafe.viewmodels.checkout

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nt118.joliecafe.data.DataStoreRepository
import com.nt118.joliecafe.data.Repository
import com.nt118.joliecafe.models.*
import com.nt118.joliecafe.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel@Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
):AndroidViewModel(application) {
    var readBackOnline = dataStoreRepository.readBackOnline
    var readUserToken = dataStoreRepository.readUserToken
    var readUserDefaultAddressId = dataStoreRepository.readUserDefaultAddressId

    val getAddressByIdResponse: MutableLiveData<ApiResult<Address>> = MutableLiveData()
    val momoPaymentRequestResponse: MutableLiveData<ApiResult<Unit>> = MutableLiveData()
    val getVoucherResponse: MutableLiveData<ApiResult<List<Voucher>>> = MutableLiveData()
    val createBillResponse: MutableLiveData<ApiResult<Unit>> = MutableLiveData()

    var userToken = ""
    var networkStatus = false
    var backOnline = false
    var userDefaultAddressId = ""
    var paymentMethod = "COD"
    var cartItems: List<CartItem> = listOf()
    var isUseJolieCoin: MutableLiveData<Boolean> = MutableLiveData(false)
    var userAddress: Address? = null
    var subTotalPrice: MutableLiveData<Double> = MutableLiveData(0.0)
    var totalPrice: MutableLiveData<Double> = MutableLiveData(0.0)
    var jolieCoin: Int = 200

    init {
        viewModelScope.launch {

            readUserToken.collectLatest { token ->
                userToken = token
            }


        }
        viewModelScope.launch {
            dataStoreRepository.readCoin.collectLatest { coin ->
                println(coin)
                jolieCoin = coin
            }
        }
    }

    fun getVouchers() = viewModelScope.launch {
        getVoucherResponse.value = ApiResult.Loading()
        try {
            val response = repository.remote.getVouchers(token = userToken)
            getVoucherResponse.value = handleApiMultiResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
            getVoucherResponse.value = ApiResult.Error(e.message.toString())
        }
    }

    fun getAddressById(addressId: String) =
        viewModelScope.launch {
            getAddressByIdResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.getAddressById(userToken, addressId)
                getAddressByIdResponse.value = handleApiResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
                getAddressByIdResponse.value = ApiResult.Error(e.message.toString())
            }
        }

    fun createBill(bill: Bill) =
        viewModelScope.launch {
            createBillResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.createBill(token = userToken, bill)
                createBillResponse.value = handleNullDataApiResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
                createBillResponse.value = ApiResult.Error(e.message.toString())
            }
        }

    fun momoPaymentRequest(token: String, data: MomoPaymentRequestBody) =
        viewModelScope.launch {
            momoPaymentRequestResponse.value = ApiResult.Loading()
            try {
                val response = repository.remote.momoRequestPayment(data = data, token = token)
                println(response)
                momoPaymentRequestResponse.value = handleNullDataApiResponse(response)
            } catch (e: Exception) {
                e.printStackTrace()
                momoPaymentRequestResponse.value = ApiResult.Error(e.message.toString())
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
                    ApiResult.Error("Cart not found!")
                }
            }
            else -> {
                ApiResult.Error(response.message())
            }
        }
    }

    private fun <T> handleApiMultiResponse(response: Response<ApiResponseMultiData<T>>): ApiResult<List<T>> {
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
                    ApiResult.Error("Cart not found!")
                }
            }
            else -> {
                ApiResult.Error(response.message())
            }
        }
    }

    private fun <T> handleNullDataApiResponse(response: Response<ApiResponseSingleData<T>>): ApiResult<T> {
        println(response)
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
                    ApiResult.NullDataSuccess()
                } else {
                    ApiResult.Error("Voucher not found!")
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