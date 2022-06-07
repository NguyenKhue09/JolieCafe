package com.nt118.joliecafe.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.nt118.joliecafe.data.network.JolieCafeApi
import com.nt118.joliecafe.data.paging_source.*
import com.nt118.joliecafe.models.*
import com.nt118.joliecafe.util.Constants.Companion.PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val jolieCafeApi: JolieCafeApi
) {
    suspend fun momoRequestPayment(
        data: MomoPaymentRequestBody,
        token: String
    ): Response<ApiResponseSingleData<Unit>> {
        return jolieCafeApi.momoRequestPayment(body = data, token = "Bearer $token")
    }

    suspend fun createUser(data: Map<String, String>): Response<ApiResponseSingleData<User>> {
        return jolieCafeApi.createUser(body = data)
    }

    suspend fun userLogin(userId: String): Response<ApiResponseSingleData<User>> {
        return jolieCafeApi.userLogin(userId = userId)
    }

    suspend fun getUserInfos(token: String): Response<ApiResponseSingleData<User>> {
        return jolieCafeApi.getUserInfos(token = token)
    }

    suspend fun updateUserInfos(
        newUserData: Map<String, String>,
        token: String
    ): Response<ApiResponseSingleData<User>> {
        return jolieCafeApi.updateUserInfos(token = "Bearer $token", body = newUserData)
    }

    fun getProducts(productQuery: Map<String, String>, token: String): Flow<PagingData<Product>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                ProductPagingSource(jolieCafeApi, token, productQuery)
            }
        ).flow
    }

    fun getUserFavoriteProducts(token: String, productQuery: Map<String, String>) : Flow<PagingData<FavoriteProduct>>{
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                FavoriteProductPagingSource(jolieCafeApi, "Bearer $token", productQuery = productQuery)
            }
        ).flow
    }

    suspend fun removeUserFavoriteProduct(token: String, favoriteProductId: String): Response<ApiResponseSingleData<Unit>> {
        return jolieCafeApi.removeUserFavoriteProduct(token = "Bearer $token", favoriteProductId = favoriteProductId)
    }

    suspend fun addNewAddress(
        data: Map<String, String>,
        token: String
    ): Response<ApiResponseSingleData<Address>> {
        return jolieCafeApi.addNewAddress(body = data, token = "Bearer $token")
    }


    suspend fun addNewDefaultAddress(
        data: Map<String, String>,
        token: String
    ): Response<ApiResponseSingleData<User>> {
        return jolieCafeApi.addNewDefaultAddress(body = data, token = "Bearer $token")
    }

    fun getAddresses(token: String): Flow<PagingData<Address>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                AddressPagingSource(jolieCafeApi, "Bearer $token")
            }
        ).flow
    }

    suspend fun deleteAddress(
        addressId: String,
        token: String
    ): Response<ApiResponseSingleData<Address>> {
        return jolieCafeApi.deleteAddress(addressId = addressId, token = "Bearer $token")
    }

    suspend fun updateAddress(
        newAddressData: Map<String, String>,
        token: String
    ): Response<ApiResponseSingleData<Address>> {
        return jolieCafeApi.updateAddress(body = newAddressData, token = "Bearer $token")
    }

    fun getCartItems(token: String, type: String): Flow<PagingData<CartItem>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = { CartItemPagingSource(jolieCafeApi, token = "Bearer $token", type) }
        ).flow
    }

    suspend fun getAddressById(token: String, addressId: String): Response<ApiResponseSingleData<Address>> {
        return jolieCafeApi.getAddressById(token = "Bearer $token", addressId = addressId)
    }

    suspend fun updateCartItem(
        newCartItemData: Map<String, String>,
        token: String
    ): Response<ApiResponseSingleData<CartItem>> {
        return jolieCafeApi.updateCartItem(body = newCartItemData, token = "Bearer $token")
    }

    suspend fun deleteCartItem(
        productId: String,
        token: String
    ): Response<ApiResponseSingleData<Unit>> {
        return jolieCafeApi.deleteCartItem(productId = productId, token = "Bearer $token")
    }

    suspend fun deleteCartItems(
        productIds: List<String>,
        token: String
    ): Response<ApiResponseSingleData<Unit>> {
        val body = mapOf(
            "productIds" to productIds
        )
        println(body["productIds"])
        return jolieCafeApi.deleteCartItems(body = body, token = "Bearer $token")
    }

    suspend fun addCart(
        data: Map<String, String>,
        token: String
    ): Response<ApiResponseSingleData<Unit>> {
        return jolieCafeApi.addCart(body = data, token = "Bearer $token")
    }

    suspend fun getAllCartItems(
        token: String
    ): Response<ApiResponseSingleData<List<CartItem>>> {
        return jolieCafeApi.getAllCartItems(token = "Bearer $token")
    }

    suspend fun getCartItemsV2(
        token: String
    ): Response<ApiResponseMultiData<CartItemByCategory>> {
        return jolieCafeApi.getCartItemsV2(token = "Bearer $token")
    }

    //favorite
    suspend fun getUserFavoriteProductsId(token: String): Response<ApiResponseMultiData<FavProductId>> {
        return jolieCafeApi.getUserFavoriteProductsId( token= "Bearer $token")
    }


    suspend fun removeUserFavoriteProductByProductId(token: String, productId: String): Response<ApiResponseSingleData<Unit>> {
        return jolieCafeApi.removeUserFavoriteProductByProductId(token = "Bearer $token", productId = productId)
    }

    suspend fun addUserFavoriteProduct(
        token: String,
        productId: String,
    ): Response<ApiResponseSingleData<Unit>> {
        return jolieCafeApi.addUserFavoriteProduct(token = "Bearer $token" , productId = productId)
    }

    fun getUserBills(token: String): Flow<PagingData<OrderHistory>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = { OrderHistoryPagingSource(token = "Bearer $token", jolieCafeApi = jolieCafeApi) }
        ).flow
    }

    // api detail product
    suspend fun getDetailFavoriteProductsId(token: String, productId: String): Response<ApiResponseSingleData<Product>> {
        return jolieCafeApi.getDetailProductsId( token= "Bearer $token", productId = productId)
    }

    suspend fun updateUserNoticeToken(
        token: String,
        notificationToken: String,
    ): Response<ApiResponseSingleData<User>> {
        return jolieCafeApi.updateUserNoticeToken(token = "Bearer $token", notificationToken = notificationToken)
    }

    suspend fun removeUserNoticeToken(
        token: String
    ): Response<ApiResponseSingleData<Unit>> {
        return jolieCafeApi.removeUserNoticeToken(token = "Bearer $token")
    }


    suspend fun getCommentProduct(token: String, productId: String): Response<ApiResponseMultiData<Comment>> {
        return jolieCafeApi.getCommentProduct( token= "Bearer $token", productId = productId)
    }

    fun getAdminNotificationsForUser(token: String): Flow<PagingData<Notification>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                NotificationPagingSource(jolieCafeApi, "Bearer $token")
            }
        ).flow
    }

}