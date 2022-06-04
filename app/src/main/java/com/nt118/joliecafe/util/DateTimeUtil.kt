package com.nt118.joliecafe.util

import android.os.Build
import java.time.format.DateTimeFormatter
import java.util.*

class DateTimeUtil {
    companion object {
        fun getCurrentDate(): String {
            val currentDate = Date()
            val calendar = GregorianCalendar()
            calendar.time = currentDate
            return "${calendar.get(Calendar.YEAR)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DAY_OF_MONTH)}"
        }
    }
}