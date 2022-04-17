package com.nt118.joliecafe.data

import com.nt118.joliecafe.data.network.JolieCafeApi
import com.nt118.joliecafe.models.SuspendUserMoneyResponse
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val jolieCafeApi: JolieCafeApi
) {
    suspend fun momoRequestPayment(data: HashMap<String, Any>): Response<SuspendUserMoneyResponse> {
        return jolieCafeApi.momoRequestPayment(body = data)
    }
}