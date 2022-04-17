package com.nt118.joliecafe.util


class RandomString {
    companion object {
        fun generateRandomString(length: Int = 6) : String {
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            return "MM" + (1..length)
                .map { allowedChars.random() }
                .joinToString("") + System.currentTimeMillis()
        }
    }
}
