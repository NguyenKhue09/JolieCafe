package com.nt118.joliecafe.data.paging_source

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nt118.joliecafe.data.network.JolieCafeApi
import com.nt118.joliecafe.models.CartItem
import com.nt118.joliecafe.util.Constants
import retrofit2.HttpException
import java.io.IOException

class CartItemPagingSource(
    private val jolieCafeApi: JolieCafeApi,
    private val token: String
) : PagingSource<Int, CartItem>() {

    override fun getRefreshKey(state: PagingState<Int, CartItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CartItem> {
        try {
            val nextPageNumber = params.key ?: 1
            val query = mapOf(
                "currentPage" to nextPageNumber.toString(),
                "itemsPerPage" to Constants.PAGE_SIZE.toString(),
            )
            val response = jolieCafeApi.getCartItems(token, query)

            return if (response.success) {
                Log.d("Cart API", "load: thanh cong")
                LoadResult.Page(
                    data = if (response.data.isNullOrEmpty()) emptyList() else response.data,
                    prevKey = if (response.data.isNullOrEmpty()) null else response.prevPage,
                    nextKey = if (response.data.isNullOrEmpty()) null else response.nextPage
                )
            } else {
                LoadResult.Error(throwable = Throwable(response.message))
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return LoadResult.Error(throwable = e)
        } catch (e: HttpException) {
            e.printStackTrace()
            return LoadResult.Error(throwable = e)
        }
    }
}