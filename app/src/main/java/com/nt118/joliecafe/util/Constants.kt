package com.nt118.joliecafe.util

class Constants {

    companion object {
        val listTabContentFavorite = listOf("All", "Coffee", "Tea", "Juice", "Pasty", "Milk shake", "Milk tea")
        const val WEBCLIENT_ID = "761101144147-1ac26g0mibusafq3vfgd7dutvgad81f1.apps.googleusercontent.com"
        const val IS_EDIT = "isEdit"
        const val IS_CHANGE_PASSWORD = "isChangePassword"
        const val IS_SAVE_CHANGE_PASSWORD = "isSaveChangePassword"
        const val IS_ADD_NEW_ADDRESS = "isAddNewAddress"
        const val BASE_URL = "https://joliecafe.herokuapp.com"

        const val PREFERENCES_NAME = "jolie_preferences"
        const val PREFERENCES_BACK_ONLINE = "backOnline"
        const val PREFERENCES_USER_TOKEN = "userToken"
        const val PREFERENCES_USER_AUTH_TYPE = "isFaceOrGGLogin"
        const val PREFERENCES_IS_USER_DATA_CHANGE = "isUserDataChange"
        const val PREFERENCES_USER_DEFAULT_ADDRESS_ID = "defaultAddressId"
        const val PREFERENCES_USER_NOTICE_TOKEN = "userNoticeToken"
        const val PREFERENCES_COIN = "coin"
        const val API_GATEWAY = "/api/v1/jolie-cafe"
        const val PAGE_SIZE = 10

        const val SNACK_BAR_STATUS_SUCCESS = 1
        const val SNACK_BAR_STATUS_DISABLE = 0
        const val SNACK_BAR_STATUS_ERROR = -1

        const val MERCHANT_NAME = "JolieCafe"
        const val MERCHANT_CODE = "MOMOJKQ920220323"
        const val MERCHANT_NAME_LABEL = "JolieCafe"

        const val UTC_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        const val LOCAL_TIME_FORMAT = "dd/MM/yyyy"
        const val TOPIC = "JolieCafeNotificationMainTopic"

        val listNotificationType = listOf("COMMON", "PRODUCT", "VOUCHER", "BILL")
        val listNotificationTab = listOf("All", "For You")
    }
}       