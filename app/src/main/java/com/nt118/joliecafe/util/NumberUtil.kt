package com.nt118.joliecafe.util

import kotlin.math.roundToInt

class NumberUtil {
    companion object {
        fun addSeparator(number: Double): String {
            val numberInString = number.roundToInt().toString()
            var result = ""
            for (i in numberInString.length - 1 downTo 0) {
                result = numberInString[i] + result
                if ((numberInString.length - i) % 3 == 0 && i != 0) {
                    result = ".$result"
                }
            }
            return result
        }
    }
}