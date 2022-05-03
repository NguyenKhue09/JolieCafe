package com.nt118.joliecafe.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.nt118.joliecafe.data.network.JolieCafeApi
import com.nt118.joliecafe.data.paging_source.AddressPagingSource
import com.nt118.joliecafe.data.paging_source.CartItemPagingSource
import com.nt118.joliecafe.data.paging_source.ProductPagingSource
import com.nt118.joliecafe.models.*
import com.nt118.joliecafe.util.Constants.Companion.PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val jolieCafeApi: JolieCafeApi
) {
    suspend fun momoRequestPayment(
        data: HashMap<String, Any>,
        token: String
    ): Response<SuspendUserMoneyResponse> {
        return jolieCafeApi.momoRequestPayment(body = data, token = token)
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

    fun getProducts(productQuery: Map<String, String>, token: String): Flow<PagingData<Product>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                ProductPagingSource(jolieCafeApi, token, productQuery)
            }
        ).flow
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

    fun getCartItems(token: String): Flow<PagingData<CartItem>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = { CartItemPagingSource(jolieCafeApi, token) }
        ).flow
    }

    suspend fun updateUserInfos(
        newUserData: Map<String, String>,
        token: String
    ): Response<ApiResponseSingleData<User>> {
        return jolieCafeApi.updateUserInfos(token = "Bearer $token", body = newUserData)
    }
}