package com.nt118.joliecafe.data

import com.nt118.joliecafe.data.network.JolieCafeApi
import com.nt118.joliecafe.models.*
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val jolieCafeApi: JolieCafeApi
) {
    suspend fun momoRequestPayment(data: HashMap<String, Any>, token: String): Response<SuspendUserMoneyResponse> {
        return jolieCafeApi.momoRequestPayment(body = data, token = token)
    }

    suspend fun createUser(data: HashMap<String, Any>): Response<ApiResponseSingleData<User>> {
        return jolieCafeApi.createUser(body = data)
    }

    suspend fun getProduct(productQuery: Map<String, String>, token: String): Response<ApiResponseMultiData<Product>> {
        return jolieCafeApi.getProducts(productQuery = productQuery, token = token)
    }
}