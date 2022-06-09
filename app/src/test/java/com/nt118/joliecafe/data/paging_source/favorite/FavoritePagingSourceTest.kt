package com.nt118.joliecafe.data.paging_source.favorite

import com.nt118.joliecafe.data.remote.FakeJolieApi
import com.nt118.joliecafe.models.FavoriteProduct
import com.nt118.joliecafe.models.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FavoritePagingSourceTest {

    private lateinit var jolieApi: FakeJolieApi
    private lateinit var favProducts: List<FavoriteProduct>

    @Before
    fun setup() {
        jolieApi = FakeJolieApi()
        favProducts = listOf(
            FavoriteProduct(
                id = "0",
                product = Product(
                    discountPercent = 0,
                    startDateDiscount = "2022-06-09T03:43:49.731Z",
                    endDateDiscount = "2022-06-09T03:43:49.731Z",
                    id = "6267f76e02095fbefdd3cbae",
                    name = "Molasses",
                    status = "Available",
                    description = "Tea, sugar cane",
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/joliecafe-799f7.appspot.com/o/molasses.jpg?alt=media&token=1d2f1aee-31bb-4307-8e49-66b0134b628e",
                    comments = emptyList(),
                    originPrice = 46000.0,
                    avgRating = 3,
                    isDeleted = false,
                    type = "Tea",
                    updatedAt = null,
                    createdAt = null
                )
            ),
            FavoriteProduct(
                id = "1",
                product = Product(
                    discountPercent = 0,
                    startDateDiscount = "2022-06-09T03:43:49.731Z",
                    endDateDiscount = "2022-06-09T03:43:49.731Z",
                    id = "6267f9ab02095fbefdd3cbb3",
                    name = "Gold berry",
                    status = "Available",
                    description = "Tea, berry, peach",
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/joliecafe-799f7.appspot.com/o/gold-berry.jpg?alt=media&token=a9bb8bac-cecc-4ea2-8be0-455f6a2c1603",
                    comments = emptyList(),
                    originPrice = 49000.0,
                    avgRating = 3,
                    isDeleted = false,
                    type = "Tea",
                    updatedAt = null,
                    createdAt = null
                )
            ),
            FavoriteProduct(
                id = "2",
                product = Product(
                    discountPercent = 0,
                    startDateDiscount = "2022-06-09T03:43:49.731Z",
                    endDateDiscount = "2022-06-09T03:43:49.731Z",
                    id = "6267f5fd02095fbefdd3cbab",
                    name = "Pineapple juice",
                    status = "Available",
                    description = "pineapple",
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/joliecafe-799f7.appspot.com/o/pineapple-juice.jpg?alt=media&token=18e4a9df-3848-41e4-9d96-48efafd8ff89",
                    comments = emptyList(),
                    originPrice = 56000.0,
                    avgRating = 4,
                    isDeleted = false,
                    type = "Juice",
                    updatedAt = null,
                    createdAt = null
                )
            ),
            FavoriteProduct(
                id = "3",
                product = Product(
                    discountPercent = 0,
                    startDateDiscount = "2022-06-09T03:43:49.731Z",
                    endDateDiscount = "2022-06-09T03:43:49.731Z",
                    id = "6267fa7e02095fbefdd3cbb5",
                    name = "Oolong",
                    status = "Available",
                    description = "Oolong, milk",
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/joliecafe-799f7.appspot.com/o/oolong.jpg?alt=media&token=ab2137f8-be40-47e3-ad90-fac34784dc74",
                    comments = emptyList(),
                    originPrice = 46000.0,
                    avgRating = 5,
                    isDeleted = false,
                    type = "Milk shake",
                    updatedAt = null,
                    createdAt = null
                )
            ),
            FavoriteProduct(
                id = "4",
                product = Product(
                    discountPercent = 0,
                    startDateDiscount = "2022-06-09T03:43:49.731Z",
                    endDateDiscount = "2022-06-09T03:43:49.731Z",
                    id = "6266aa09de6570302a415601",
                    name = "Cappuccino",
                    status = "Available",
                    description = "Coffee, fresh milk",
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/joliecafe-799f7.appspot.com/o/cappuccino.jpg?alt=media&token=4c7f7c99-3d64-4329-94e5-cdb2752e684e",
                    comments = emptyList(),
                    originPrice = 52000.0,
                    avgRating = 5,
                    isDeleted = false,
                    type = "Coffee",
                    updatedAt = null,
                    createdAt = null
                )
            ),
            FavoriteProduct(
                id = "5",
                product = Product(
                    discountPercent = 0,
                    startDateDiscount = "2022-06-09T03:43:49.731Z",
                    endDateDiscount = "2022-06-09T03:43:49.731Z",
                    id = "6267f7e002095fbefdd3cbaf",
                    name = "Jasmine lychee",
                    status = "Available",
                    description = "Tea, lychee",
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/joliecafe-799f7.appspot.com/o/jasmine-lychee.jpg?alt=media&token=644eb60a-b57c-4a1d-b5c3-6b9b56a10ea8",
                    comments = emptyList(),
                    originPrice = 46000.0,
                    avgRating = 4,
                    isDeleted = false,
                    type = "Tea",
                    updatedAt = null,
                    createdAt = null
                )
            ),
            FavoriteProduct(
                id = "6",
                product = Product(
                    discountPercent = 0,
                    startDateDiscount = "2022-06-09T03:43:49.731Z",
                    endDateDiscount = "2022-06-09T03:43:49.731Z",
                    id = "6267f4c802095fbefdd3cba9",
                    name = "Cacao classic",
                    status = "Available",
                    description = "Cacao, fresh milk",
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/joliecafe-799f7.appspot.com/o/cacao-classic.jpg?alt=media&token=cab284f1-f599-447d-b28a-d023a2352fb1",
                    comments = emptyList(),
                    originPrice = 52000.0,
                    avgRating = 4,
                    isDeleted = false,
                    type = "Coffee",
                    updatedAt = null,
                    createdAt = null
                )
            ),
            FavoriteProduct(
                id = "7",
                product = Product(
                    discountPercent = 0,
                    startDateDiscount = "2022-06-09T03:43:49.731Z",
                    endDateDiscount = "2022-06-09T03:43:49.731Z",
                    id = "6267f86502095fbefdd3cbb0",
                    name = "Passion peach",
                    status = "Available",
                    description = "Tea, peach, passion fruit",
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/joliecafe-799f7.appspot.com/o/passion-peach.jpg?alt=media&token=e7d022a3-3db8-41d9-adc5-3c24c46d9ec6",
                    comments = emptyList(),
                    originPrice = 46000.0,
                    avgRating = 4,
                    isDeleted = false,
                    type = "Tea",
                    updatedAt = null,
                    createdAt = null
                )
            ),
            FavoriteProduct(
                id = "8",
                product = Product(
                    discountPercent = 0,
                    startDateDiscount = "2022-06-09T03:43:49.731Z",
                    endDateDiscount = "2022-06-09T03:43:49.731Z",
                    id = "6267fa2902095fbefdd3cbb4",
                    name = "Full leaf",
                    status = "Available",
                    description = "Red tea, milk",
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/joliecafe-799f7.appspot.com/o/full-leaf.jpg?alt=media&token=fe0856a3-56e6-4e6f-88c7-9e39f512f118",
                    comments = emptyList(),
                    originPrice = 42000.0,
                    avgRating = 5,
                    isDeleted = false,
                    type = "Milk shake",
                    updatedAt = null,
                    createdAt = null
                ),
            ),
            FavoriteProduct(
                id = "9",
                product = Product(
                    discountPercent = 0,
                    startDateDiscount = "2022-06-09T03:43:49.731Z",
                    endDateDiscount = "2022-06-09T03:43:49.731Z",
                    id = "6267fa7e02095fbefdd3cbb5",
                    name = "Oolong",
                    status = "Available",
                    description = "Oolong, milk",
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/joliecafe-799f7.appspot.com/o/oolong.jpg?alt=media&token=ab2137f8-be40-47e3-ad90-fac34784dc74",
                    comments = emptyList(),
                    originPrice = 46000.0,
                    avgRating = 5,
                    isDeleted = false,
                    type = "Milk shake",
                    updatedAt = null,
                    createdAt = null
                ),
            ),
            FavoriteProduct(
                id = "10",
                product = Product(
                    discountPercent = 0,
                    startDateDiscount = "2022-06-09T03:43:49.731Z",
                    endDateDiscount = "2022-06-09T03:43:49.731Z",
                    id = "6267fb0902095fbefdd3cbb6",
                    name = "Brown sugar bean",
                    status = "Available",
                    description = "Red tea, milk, brown sugar",
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/joliecafe-799f7.appspot.com/o/brown-sugar-bean.jpg?alt=media&token=d6b7245a-ff75-4f7a-92a1-626b4e2cbda1",
                    comments = emptyList(),
                    originPrice = 48000.0,
                    avgRating = 3,
                    isDeleted = false,
                    type = "Milk shake",
                    updatedAt = null,
                    createdAt = null
                )
            ),
            FavoriteProduct(
                id = "11",
                product = Product(
                    discountPercent = 0,
                    startDateDiscount = "2022-06-09T03:43:49.731Z",
                    endDateDiscount = "2022-06-09T03:43:49.731Z",
                    id = "6267fb7a02095fbefdd3cbb7",
                    name = "Classic",
                    status = "Available",
                    description = "milk, tea",
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/joliecafe-799f7.appspot.com/o/classic-milk-tea.jpg?alt=media&token=bab6e568-e8b4-4653-9052-87b859d38718",
                    comments = emptyList(),
                    originPrice = 25000.0,
                    avgRating = 5,
                    isDeleted = false,
                    type = "Milk tea",
                    updatedAt = null,
                    createdAt = null
                )
            ),
            FavoriteProduct(
                id = "12",
                product = Product(
                    discountPercent = 0,
                    startDateDiscount = "2022-06-09T03:43:49.731Z",
                    endDateDiscount = "2022-06-09T03:43:49.731Z",
                    id = "6267fbd402095fbefdd3cbb8",
                    name = "Matcha",
                    status = "Available",
                    description = "matcha, milk, tea",
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/joliecafe-799f7.appspot.com/o/matcha.jpg?alt=media&token=67901427-3428-4c3a-a868-b5709c2eac22",
                    comments = emptyList(),
                    originPrice = 30000.0,
                    avgRating = 5,
                    isDeleted = false,
                    type = "Milk tea",
                    updatedAt = null,
                    createdAt = null
                )
            ),
            FavoriteProduct(
                id = "13",
                product = Product(
                    discountPercent = 0,
                    startDateDiscount = "2022-06-09T03:43:49.731Z",
                    endDateDiscount = "2022-06-09T03:43:49.731Z",
                    id = "6267fc2002095fbefdd3cbb9",
                    name = "Taro",
                    status = "Available",
                    description = "taro, milk, tea",
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/joliecafe-799f7.appspot.com/o/taro.jpg?alt=media&token=348f82f7-4b5e-4d97-b43e-6f41c6a6f620",
                    comments = emptyList(),
                    originPrice = 30000.0,
                    avgRating = 3,
                    isDeleted = false,
                    type = "Milk tea",
                    updatedAt = null,
                    createdAt = null
                )
            ),
            FavoriteProduct(
                id = "14",
                product = Product(
                    discountPercent = 0,
                    startDateDiscount = "2022-06-09T03:43:49.731Z",
                    endDateDiscount = "2022-06-09T03:43:49.731Z",
                    id = "6267fc8402095fbefdd3cbba",
                    name = "Pearl",
                    status = "Available",
                    description = "pearl, milk, tea",
                    thumbnail = "https://firebasestorage.googleapis.com/v0/b/joliecafe-799f7.appspot.com/o/pearl-milk-tea.jpg?alt=media&token=c39ddcb9-af50-436d-af5d-1f5b51b29d76",
                    comments = emptyList(),
                    originPrice = 30000.0,
                    avgRating = 4,
                    isDeleted = false,
                    type = "Milk tea",
                    updatedAt = null,
                    createdAt = null
                )
            )
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getFavoriteProducts should return list of favorite products`() =
        runTest {

        }

}