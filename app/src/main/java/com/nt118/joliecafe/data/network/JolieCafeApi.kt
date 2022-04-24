package com.nt118.joliecafe.data.network

import com.nt118.joliecafe.models.SuspendUserMoneyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface JolieCafeApi {

    @Headers("Content-Type: application/json")
    @POST("/api/v1/jolie-cafe/payment/payment-request")
    suspend fun momoRequestPayment(
        @Body body: HashMap<String, Any>
    ): Response<SuspendUserMoneyResponse>


}