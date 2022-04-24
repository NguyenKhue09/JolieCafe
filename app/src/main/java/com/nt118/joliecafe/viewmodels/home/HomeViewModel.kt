package com.nt118.joliecafe.viewmodels.home

import android.app.Application
import androidx.lifecycle.*
import com.google.protobuf.Api
import com.nt118.joliecafe.data.Repository
import com.nt118.joliecafe.models.SuspendUserMoneyResponse
import com.nt118.joliecafe.util.ApiResult.Success
import com.nt118.joliecafe.util.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val repository: Repository
) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment cmm"
    }
    val text: LiveData<String> = _text

    private val _momo = MutableStateFlow<ApiResult<SuspendUserMoneyResponse>>(ApiResult.Loading())
    val momo: StateFlow<ApiResult<SuspendUserMoneyResponse>> = _momo

    fun getMomo(data: HashMap<String, Any>, token: String) {
        viewModelScope.launch {
            val response = repository.remote.momoRequestPayment(data, token)
            if(response.isSuccessful) {
                _momo.value = Success(data = response.body()!!)
            } else {
                _momo.value = ApiResult.Error(message = response.message(), data = null)
            }
        }
    }
}