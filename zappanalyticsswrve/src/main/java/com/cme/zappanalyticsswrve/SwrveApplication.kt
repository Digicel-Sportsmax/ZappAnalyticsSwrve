package com.cme.zappanalyticsswrve

import android.content.pm.ApplicationInfo
import android.util.Log
import com.applicaster.app.CustomApplication 
import com.swrve.sdk.SwrveInitMode
import com.swrve.sdk.SwrveSDK
import com.swrve.sdk.config.SwrveConfig
import com.swrve.sdk.config.SwrveStack
import java.lang.IllegalArgumentException

class SwrveApplication : CustomApplication() {
    override fun onCreate() {
        super.onCreate()
        try {
            val config = SwrveConfig()
            config.initMode = SwrveInitMode.AUTO
            config.selectedStack = SwrveStack.EU

            val appId = PluginConfigurationHelper.getConfigurationValue(SWRVE_ACCOUNT_ID)?.toInt() ?: 0
            val isDebuggable = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE

            val apiKey = if (isDebuggable){
                PluginConfigurationHelper.getConfigurationValue(SWRVE_SANDBOX_KEY)
            }else{
                PluginConfigurationHelper.getConfigurationValue(SWRVE_PRODUCTION_KEY)
            }

            config.setNotificationListener { pushJson ->
                Log.wtf("Received push", "of body: " + pushJson.toString(1))
            }
            SwrveSDK.createInstance(this, appId, apiKey, config)
        } catch (ex: IllegalArgumentException) {
            Log.e("SwrveDemo", "Could not initialize the Swrve SDK", ex)
        }
    }
}