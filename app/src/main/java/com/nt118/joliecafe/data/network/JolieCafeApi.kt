package com.nt118.joliecafe.data.network

import com.nt118.joliecafe.models.*
import com.nt118.joliecafe.util.Constants.Companion.API_GATEWAY
import okhttp3.ResponseBody
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
        @Body body: Map<String, String>,
    ): Response<ApiResponseSingleData<User>>

    @GET("$API_GATEWAY/user/login")
    suspend fun userLogin(
        @Query("userId") userId: String
    ): Response<ApiResponseSingleData<User>>

    @GET("$API_GATEWAY/user/get-user-info")
    suspend fun getUserInfos(
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<User>>

    @PUT("$API_GATEWAY/user/update-user-info")
    suspend fun updateUserInfos(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>,
    ): Response<ApiResponseSingleData<User>>


    @GET("$API_GATEWAY/product/get-products")
    suspend fun getProducts(
        @QueryMap productQuery: Map<String, String>,
        @Header("Authorization") token: String
    ): ApiResponseMultiData<Product>

    @Headers("Content-Type: application/json")
        @GET("$API_GATEWAY/favorite")
    suspend fun getUserFavoriteProduct(
        @QueryMap productQuery: Map<String, String>,
        @Header("Authorization") token: String
    ): ApiResponseMultiData<FavoriteProduct>

    @DELETE("$API_GATEWAY/favorite/remove")
    suspend fun removeUserFavoriteProduct(
        @Header("Authorization") token: String,
        @Query("favoriteProductId") favoriteProductId: String,
    ): Response<ApiResponseSingleData<Unit>>


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

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("$API_GATEWAY/address/delete")
    @FormUrlEncoded
    suspend fun deleteAddress(
        @Field("addressId") addressId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Address>>

    @Headers("Content-Type: application/json")
    @POST("$API_GATEWAY/address/update")
    suspend fun updateAddress(
        @Body body: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Address>>

    // Cart API

    @Headers("Content-Type: application/json")
    @GET("$API_GATEWAY/cart")
    suspend fun getCartItems(
        @Header("Authorization") token: String,
        @QueryMap cartQuery: Map<String, String>,
    ): ApiResponseMultiData<CartItem>

    // End of Cart API




   // https://stackoverflow.com/questions/41078866/retrofit2-authorization-global-interceptor-for-access-token

}