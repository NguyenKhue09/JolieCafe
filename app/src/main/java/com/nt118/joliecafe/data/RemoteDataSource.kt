package com.nt118.joliecafe.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nt118.joliecafe.data.network.JolieCafeApi
import com.nt118.joliecafe.data.paging_source.ProductPagingSource
import com.nt118.joliecafe.models.*
import com.nt118.joliecafe.util.Constants.Companion.PAGE_SIZE
import kotlinx.coroutines.flow.Flow
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

    fun getProducts(productQuery: Map<String, String>, token: String): Flow<PagingData<Product>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                ProductPagingSource(jolieCafeApi, token, productQuery)
            }
        ).flow
    }
}