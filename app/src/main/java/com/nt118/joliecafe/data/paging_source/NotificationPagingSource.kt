package com.nt118.joliecafe.data.paging_source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nt118.joliecafe.data.network.JolieCafeApi
import com.nt118.joliecafe.models.Notification
import com.nt118.joliecafe.util.Constants.Companion.PAGE_SIZE
import retrofit2.HttpException
import java.io.IOException

class NotificationPagingSource(
    private val jolieAdminApi: JolieCafeApi,
    private val token: String
): PagingSource<Int, Notification>() {

    override fun getRefreshKey(state: PagingState<Int, Notification>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Notification> {
        try {
            val nextPageNumber = params.key ?: 1
            val query = mapOf(
                "currentPage" to nextPageNumber.toString(),
                "notificationPerPage" to PAGE_SIZE.toString(),
            )

            //println(query)

            try {
                val response = jolieAdminApi.getAdminNotificationForUser(notificationQuery = query, token = token)
                //println(response)
                return if(response.success) {
                    LoadResult.Page(
                        data = if(response.data.isNullOrEmpty()) emptyList() else response.data,
                        prevKey = if(response.data.isNullOrEmpty()) null else response.prevPage,
                        nextKey = if(response.data.isNullOrEmpty()) null else response.nextPage
                    )
                } else {
                    LoadResult.Error(throwable = Throwable(response.message))
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return LoadResult.Error(throwable = Throwable(e.message))
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