package com.nt118.joliecafe.data.paging_source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nt118.joliecafe.data.network.JolieCafeApi
import com.nt118.joliecafe.models.FavoriteProduct
import com.nt118.joliecafe.models.Product
import com.nt118.joliecafe.util.Constants.Companion.PAGE_SIZE
import retrofit2.HttpException
import java.io.IOException

class FavoriteProductPagingSource(
    private val jolieCafeApi: JolieCafeApi,
    private val token: String,
    private val productQuery: Map<String, String>
) : PagingSource<Int, FavoriteProduct>() {

    override fun getRefreshKey(state: PagingState<Int, FavoriteProduct>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FavoriteProduct> {
        try {
            val nextPageNumber = params.key ?: 1
            val query = mapOf(
                "currentPage" to nextPageNumber.toString(),
                "productPerPage" to PAGE_SIZE.toString(),
                "type" to productQuery["type"]!!
            )

            val response = jolieCafeApi.getUserFavoriteProduct(productQuery = query, token = token)

            return if (response.success) {
                LoadResult.Page(
                    data = if (response.data.isNullOrEmpty()) emptyList() else response.data,
                    prevKey = if (response.data.isNullOrEmpty()) null else response.prevPage,
                    nextKey = if (response.data.isNullOrEmpty()) null else response.nextPage
                )
            } else {
                LoadResult.Error(throwable = Throwable(response.message))
            }

        } catch (e: IOException) {
            // IOException for network failures.
            return LoadResult.Error(e)
        } catch (e: HttpException) {
            // HttpException for any non-2xx HTTP status codes.
            return LoadResult.Error(e)
        } catch (e: Exception) {
            e.printStackTrace()
            return LoadResult.Error(e)
        }
    }
}