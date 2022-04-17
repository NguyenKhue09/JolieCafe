package com.nt118.joliecafe.payments.momo_payment

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import org.json.JSONException
import org.json.JSONObject
import vn.momo.momo_partner.utils.MoMoUtils


class AppMoMoLibKotlinVersion {

    private var REQUEST_CODE_MOMO = 1000
    var action = ""
    var environment = 0
    private var actionType = ""
    var dataRequest: JSONObject? = null

    companion object {
        private lateinit var instance: AppMoMoLibKotlinVersion

        fun getMoMoKotlinInstance(): AppMoMoLibKotlinVersion {
            if (!::instance.isInitialized) {
                instance = AppMoMoLibKotlinVersion()
            }
            return instance
        }


        enum class ACTION_TYPE {
            GET_TOKEN, LINK
        }

        enum class ENVIRONMENT {
            DEBUG, DEVELOPMENT, PRODUCTION
        }

        enum class ACTION {
            MAP, PAYMENT
        }
    }

    fun setAction(_action: ACTION): String {
        action = when (_action) {
            ACTION.MAP -> {
                "com.android.momo.SDK"
            }
            else -> {
                "com.android.momo.PAYMENT"
            }
        }
        return action
    }

    fun setActionType(_actionType: ACTION_TYPE): String {
        actionType = if (_actionType == ACTION_TYPE.GET_TOKEN) {
            "gettoken"
        } else {
            "link"
        }
        return actionType
    }

    fun setEnvironment(_environment: ENVIRONMENT): Int {
        environment = when (_environment) {
            ENVIRONMENT.DEBUG -> {
                0
            }
            ENVIRONMENT.DEVELOPMENT -> {
                1
            }
            ENVIRONMENT.PRODUCTION -> {
                2
            }
            else -> {
                0
            }
        }
        return environment
    }

    fun requestMoMoCallBack(activity: Activity, momoRequest: ActivityResultLauncher<Intent>, hashMap: MutableMap<String, Any>) {
        val jsonData = JSONObject()
        val iterator = hashMap.keys.iterator()
        var packageClass: String
        var value: Any?

        try {
            while (iterator.hasNext()) {
                packageClass = iterator.next()
                value = hashMap[packageClass]
                if (packageClass == "extraData" && value != null) {
                    value = MoMoUtils.encodeString(value.toString())
                }
                if (packageClass == "extra" && value != null) {
                    value = MoMoUtils.encodeString(value.toString())
                }
                jsonData.put(packageClass, value)
            }
        } catch (var11: JSONException) {
            var11.printStackTrace()
        }
        dataRequest = jsonData
        if (action == "") {
            Toast.makeText(activity, "Please init AppMoMoLibKotlinVersion.getMoMoKotlinInstance().setAction", Toast.LENGTH_LONG).show()
        } else if (actionType == "") {
            Toast.makeText(activity, "Please init AppMoMoLibKotlinVersion.getMoMoKotlinInstance().setActionType", Toast.LENGTH_LONG).show()
        } else if (action == "com.android.momo.SDK" && actionType != "link" || action == "com.android.momo.PAYMENT" && actionType != "gettoken") {
            Toast.makeText(activity, "Please set action type and action", Toast.LENGTH_LONG).show()
        } else {
            try {
                packageClass = when (environment) {
                    1 -> "vn.momo.platform.test"
                    2 -> "com.mservice.momotransfer"
                    else -> "com.mservice.debug"
                }
                val intent = Intent()
                val applicationInfo = activity.applicationContext.applicationInfo
                val stringId = applicationInfo.labelRes
                val appName =
                    if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else activity.applicationContext.getString(
                        stringId
                    )
                jsonData.put("sdkversion", "3.0.1")
                jsonData.put("clientIp", MoMoUtils.getIPAddress(true))
                jsonData.put("appname", appName)
                jsonData.put("packagename", activity.packageName)
                jsonData.put("action", actionType)
                jsonData.put(
                    "clientos",
                    "Android_" + MoMoUtils.getDeviceName() + "_" + MoMoUtils.getDeviceSoftwareVersion()
                )
                if (appInstalledOrNot(activity, packageClass)) {
                    intent.action = action
                    intent.putExtra("JSON_PARAM", jsonData.toString())
                    momoRequest.launch(intent)
                } else {
                    handleCallGooglePlay(activity, "com.mservice.momotransfer")
                }
            } catch (var10: Exception) {
                var10.printStackTrace()
            }
        }
    }

    private fun appInstalledOrNot(mActivity: Activity, uri: String): Boolean {
        val pm = mActivity.packageManager
        var app_installed = false
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            app_installed = true
        } catch (var6: PackageManager.NameNotFoundException) {
        }
        return app_installed
    }

    private fun handleCallGooglePlay(mActivity: Activity, packageClass: String) {
        try {
            mActivity.startActivity(
                Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("market://details?id=$packageClass")
                )
            )
        } catch (var4: java.lang.Exception) {
            mActivity.startActivity(
                Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("http://play.google.com/store/apps/details?id=com.mservice.momotransfer")
                )
            )
        }
    }
}