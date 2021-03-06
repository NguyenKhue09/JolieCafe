package com.nt118.joliecafe.viewmodels.order_history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nt118.joliecafe.data.DataStoreRepository
import com.nt118.joliecafe.data.Repository
import com.nt118.joliecafe.models.ApiResponseSingleData
import com.nt118.joliecafe.models.BillReviewBody
import com.nt118.joliecafe.models.OrderHistory
import com.nt118.joliecafe.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository
) : AndroidViewModel(application){

    var readBackOnline = dataStoreRepository.readBackOnline
    var readUserToken = dataStoreRepository.readUserToken

    private val _orderHistory = MutableStateFlow<PagingData<OrderHistory>>(PagingData.empty())
    val orderHistory: StateFlow<PagingData<OrderHistory>> = _orderHistory

    private val _orderHistoryClickedList = MutableLiveData<MutableList<String>>(mutableListOf())
    val orderHistoryClickedList: LiveData<MutableList<String>> = _orderHistoryClickedList

    val reviewBillResponse: MutableLiveData<ApiResult<Unit>> = MutableLiveData()

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

    fun addNewOrderToClickedList(id: String) {
        println(id)
        if(!_orderHistoryClickedList.value!!.contains(id)) {
            _orderHistoryClickedList.value!!.add(id)
        } else {
            _orderHistoryClickedList.value!!.remove(id)
        }
        _orderHistoryClickedList.value = _orderHistoryClickedList.value
    }

    fun getUserBills() {
        try {
            viewModelScope.launch {
                repository.remote.getUserBills(
                    token = userToken
                ).cachedIn(viewModelScope).collectLatest {
                    _orderHistory.value = it
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _orderHistory.value = PagingData.empty()
        }
    }

    fun reviewBills(billReviewBody: BillReviewBody) =  viewModelScope.launch {
        reviewBillResponse.value = ApiResult.Loading()
        try {
            val response = repository.remote.reviewBill(
                token = userToken,
                body = billReviewBody
            )
            reviewBillResponse.value = handleApiResponse(response = response)
        } catch (e: Exception) {
            e.printStackTrace()
            reviewBillResponse.value = ApiResult.Error(e.message)
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
}