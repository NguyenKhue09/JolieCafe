package com.nt118.joliecafe.data.network

import com.nt118.joliecafe.models.*
import com.nt118.joliecafe.util.Constants.Companion.API_GATEWAY
import retrofit2.Response
import retrofit2.http.*


interface JolieCafeApi {

    @Headers("Content-Type: application/json")
    @POST("$API_GATEWAY/payment/payment-request")
    suspend fun momoRequestPayment(
        @Body body: MomoPaymentRequestBody,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Unit>>


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

    @Headers("Content-Type: application/json")
    @GET("$API_GATEWAY/address/{addressId}")
    suspend fun getAddressById(
        @Header("Authorization") token: String,
        @Path("addressId") addressId: String
    ): Response<ApiResponseSingleData<Address>>

    // Cart API

    @Headers("Content-Type: application/json")
    @GET("$API_GATEWAY/cart")
    suspend fun getCartItems(
        @Header("Authorization") token: String,
        @QueryMap cartQuery: Map<String, String>,
    ): ApiResponseMultiData<CartItem>

    @Headers("Content-Type: application/json")
    @PUT("$API_GATEWAY/cart/update")
    suspend fun updateCartItem(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>,
    ): Response<ApiResponseSingleData<CartItem>>

    @Headers("Content-Type: application/json")
    @DELETE("$API_GATEWAY/cart/remove/{productId}")
    suspend fun deleteCartItem(
        @Header("Authorization") token: String,
        @Path("productId") productId: String,
    ): Response<ApiResponseSingleData<Unit>>

    @Headers("Content-Type: application/json")
    @PUT("$API_GATEWAY/cart/remove-many")
    @JvmSuppressWildcards
    suspend fun deleteCartItems(
        @Body body: Map<String, List<String>>,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Unit>>

    @Headers("Content-Type: application/json")
    @POST("$API_GATEWAY/cart/add")
    suspend fun addCart(
        @Body body: Map<String, String>,
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<Unit>>

    @Headers("Content-Type: application/json")
    @GET("$API_GATEWAY/cart/all")
    suspend fun getAllCartItems(
        @Header("Authorization") token: String
    ): Response<ApiResponseSingleData<List<CartItem>>>

    @Headers("Content-Type: application/json")
    @GET("$API_GATEWAY/cart/get-cart-v2")
    suspend fun getCartItemsV2(
        @Header("Authorization") token: String
    ): Response<ApiResponseMultiData<CartItemByCategory>>

    // End of Cart API


    //favorite
    @DELETE("$API_GATEWAY/favorite/remove-by-productId")
    suspend fun removeUserFavoriteProductByProductId(
        @Header("Authorization") token: String,
        @Query("productId") productId: String,
    ): Response<ApiResponseSingleData<Unit>>

    @POST("$API_GATEWAY/favorite/add")
    suspend fun addUserFavoriteProduct(
        @Header("Authorization") token: String,
        @Query("productId") productId: String,
    ): Response<ApiResponseSingleData<Unit>>

    @GET("$API_GATEWAY/favorite/list-id")
    suspend fun getUserFavoriteProductsId(
        @Header("Authorization") token: String
    ):  Response<ApiResponseMultiData<FavProductId>>

    // Start bill api

    @Headers("Content-Type: application/json")
    @GET("$API_GATEWAY/bill/get-bill-user")
    suspend fun getUserBills(
        @Header("Authorization") token: String,
        @QueryMap orderQuery: Map<String, String>,
    ): ApiResponseMultiData<OrderHistory>

    @Headers("Content-Type: application/json")
    @POST("$API_GATEWAY/bill/create")
    suspend fun createBill(
        @Header("Authorization") token: String,
        @Body bill: Bill
    ): Response<ApiResponseSingleData<Unit>>

    // End bill api

    // start detail product

    @GET("$API_GATEWAY/product/get-product-detail")
    suspend fun getDetailProductsId(
        @Header("Authorization") token: String,
        @Query("productId") productId: String,
    ):  Response<ApiResponseSingleData<Product>>

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @PUT("$API_GATEWAY/user/update-user-notice-token")
    @FormUrlEncoded
    suspend fun updateUserNoticeToken(
        @Header("Authorization") token: String,
        @Field("notificationToken") notificationToken: String,
    ): Response<ApiResponseSingleData<User>>


    @PUT("$API_GATEWAY/user/remove-user-notice-token")
    suspend fun removeUserNoticeToken(
        @Header("Authorization") token: String,
    ): Response<ApiResponseSingleData<Unit>>

    //get comments
    @GET("$API_GATEWAY/comment/get")
    suspend fun getCommentProduct(
        @Header("Authorization") token: String,
        @Query("productId") productId: String,
    ):  Response<ApiResponseMultiData<Comment>>


    @GET("$API_GATEWAY/notification/all-admin-notice-user")
    suspend fun getAdminNotificationForUser(
        @QueryMap notificationQuery: Map<String, String>,
        @Header("Authorization") token: String
    ): ApiResponseMultiData<Notification>

    @GET("$API_GATEWAY/notification/all-user-notice")
    suspend fun getAllUserNotification(
        @QueryMap notificationQuery: Map<String, String>,
        @Header("Authorization") token: String
    ): ApiResponseMultiData<Notification>

    // Voucher API

    @GET("$API_GATEWAY/voucher/get-all")
    suspend fun getVouchers(
        @Header("Authorization") token: String,
    ): Response<ApiResponseMultiData<Voucher>>

    // End of Voucher API

    //favorite
    @POST("$API_GATEWAY/comment/add")
    suspend fun reviewBill(
        @Header("Authorization") token: String,
        @Body body: BillReviewBody,
    ): Response<ApiResponseSingleData<Unit>>

   // https://stackoverflow.com/questions/41078866/retrofit2-authorization-global-interceptor-for-access-token

}