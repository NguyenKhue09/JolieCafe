package com.nt118.joliecafe.data.paging_source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nt118.joliecafe.data.network.JolieCafeApi
import com.nt118.joliecafe.models.Address
import com.nt118.joliecafe.util.Constants
import retrofit2.HttpException
import java.io.IOException

class AddressPagingSource(
    private val jolieCafeApi: JolieCafeApi,
    private val token: String,
): PagingSource<Int, Address>() {

    override fun getRefreshKey(state: PagingState<Int, Address>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Address> {
        try {
            val nextPageNumber = params.key ?: 1
            val query = mapOf(
                "currentPage" to nextPageNumber.toString(),
                "addressPerPage" to Constants.PAGE_SIZE.toString(),
            )
            val response = jolieCafeApi.getAddresses(token = token, addressQuery = query)

            return if(response.success) {
                LoadResult.Page(
                    data = if(response.data.isNullOrEmpty()) emptyList() else response.data,
                    prevKey = if(response.data.isNullOrEmpty()) null else response.prevPage,
                    nextKey = if(response.data.isNullOrEmpty()) null else response.nextPage
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
        }

    }

}