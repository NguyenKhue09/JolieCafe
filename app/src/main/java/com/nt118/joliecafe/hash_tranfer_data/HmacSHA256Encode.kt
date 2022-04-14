package com.nt118.joliecafe.hash_tranfer_data


import android.os.Build
import androidx.annotation.RequiresApi
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HmacSHA256Encode {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun createSignature(data: String, key: String): String {
            val sha256Hmac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
            sha256Hmac.init(secretKey)

             //For base64
             return Base64.getEncoder().encodeToString(sha256Hmac.doFinal(data.toByteArray()))
        }
    }
}