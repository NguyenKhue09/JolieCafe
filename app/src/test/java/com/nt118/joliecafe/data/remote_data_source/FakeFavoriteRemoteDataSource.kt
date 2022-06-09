package com.nt118.joliecafe.data.remote_data_source

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.nt118.joliecafe.data.paging_source.FavoriteProductPagingSource
import com.nt118.joliecafe.data.remote.FakeJolieApi
import com.nt118.joliecafe.models.FavoriteProduct
import com.nt118.joliecafe.util.Constants
import kotlinx.coroutines.flow.Flow

class FakeFavoriteRemoteDataSource(
    private val fakeJolieApi: FakeJolieApi
) {

    fun getUserFavoriteProducts(token: String, productQuery: Map<String, String>) : Flow<PagingData<FavoriteProduct>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.PAGE_SIZE),
            pagingSourceFactory = {
                FavoriteProductPagingSource(fakeJolieApi, "Bearer $token", productQuery = productQuery)
            }
        ).flow
    }
}