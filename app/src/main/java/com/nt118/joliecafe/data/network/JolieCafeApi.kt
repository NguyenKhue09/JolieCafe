package com.nt118.joliecafe.data.network

import com.nt118.joliecafe.models.*
import com.nt118.joliecafe.util.Constants.Companion.API_GATEWAY
import retrofit2.Response
import retrofit2.http.*

interface JolieCafeApi {

    @Headers("Content-Type: application/json")
    @POST("$API_GATEWAY/payment/payment-request")
    suspend fun momoRequestPayment(
        @Body body: HashMap<String, Any>,
        @Header("Authorization") token: String
    ): Response<SuspendUserMoneyResponse>


    @Headers("Content-Type: application/json")
    @POST("$API_GATEWAY/user/create-new-user")
    suspend fun createUser(
        @Body body: MutableMap<String, String>,
    ): Response<ApiResponseSingleData<User>>

    @GET("$API_GATEWAY/product/get-products")
    suspend fun getProducts(
        @QueryMap productQuery: Map<String, String>,
        @Header("Authorization") token: String
    ): ApiResponseMultiData<Product>

    @GET("$API_GATEWAY/user/get-user-info")
    suspend fun getUserInfos(
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<User>>

    @Headers("Content-Type: application/json")
    @POST("$API_GATEWAY/address/add")
    suspend fun addNewAddress(
        @Body body: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Address>>

    @Headers("Content-Type: application/json")
    @POST("$API_GATEWAY/address/add/default")
    suspend fun addNewDefaultAddress(
        @Body body: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<User>>

    @Headers("Content-Type: application/json")
    @GET("$API_GATEWAY/address/get")
    suspend fun getAddresses(
        @Header("Authorization") token: String,
        @QueryMap addressQuery: Map<String, String>,
    ): ApiResponseMultiData<Address>

   // https://stackoverflow.com/questions/41078866/retrofit2-authorization-global-interceptor-for-access-token

}