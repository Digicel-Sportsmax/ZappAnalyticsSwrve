package com.cme.zappanalyticsswrve

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.applicaster.activities.base.AppIntroActivity
import com.applicaster.analytics.BaseAnalyticsAgent
import com.applicaster.app.CustomApplication
import com.applicaster.util.StringUtil
import com.google.firebase.FirebaseApp
import com.swrve.sdk.SwrveInitMode
import com.swrve.sdk.SwrveNotificationConfig
import com.swrve.sdk.SwrveSDK
import com.swrve.sdk.config.SwrveConfig
import com.swrve.sdk.config.SwrveStack
import java.util.*

class SwrveAnalyticsAgent : BaseAnalyticsAgent() {

    private val TAG: String = SwrveAnalyticsAgent::class.java.simpleName

    private val MAX_SCREEN_NAME_LONG = 35
    private val MAX_PARAM_NAME_LONG = 40
    private val MAX_PARAM_VALUE_LONG = 100

    /**
     * This variables are created for Google Analytics purposes.
     * You can delete all this variables when you doing your plugin.
     */
    // region vars
    @Transient
    private val MOBILE_APP_ACCOUNT_ID_IDENTIFIER = "mobile_app_account_id"
    private val ANONYMIZE_USER_IP_IDENTIFIER = "anonymize_user_ip"
    private val SCREEN_VIEWS_IDENTIFIER = "screen_views"
    private val DO_NOT_SET_CLIENT_ID = "do_not_set_client_id"
    //private var mobileAppAccountId: String? = null
    //private var anonymizeUserIp = false
    //private var screenViews = false
    //private var shouldSetClientId = false

    // custom events
    private val PLAY_EVENT = "Play video"
    private val PAUSE_EVENT = "Pause video"
    private val STOP_EVENT = "Stop video"

    /**
     * Initialization of your Analytics provider.
     * @param context
     */
    override fun initializeAnalyticsAgent(context: Context?) {
        super.initializeAnalyticsAgent(context)
        init(context!!)
    }

    private fun init(context: Context){
        val customApp = context as? CustomApplication
        FirebaseApp.initializeApp(context)
        try {
            val config = SwrveConfig()
            config.initMode = SwrveInitMode.AUTO
            config.selectedStack = SwrveStack.EU

            var channel: NotificationChannel? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel = NotificationChannel(
                    "123",
                    "SportsMax channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                if (customApp?.getSystemService(Context.NOTIFICATION_SERVICE) != null) {
                    val notificationManager =
                        customApp?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(channel)
                }
            }

            val notificationConfig = SwrveNotificationConfig.Builder(R.drawable.appoxee_custom_icon, R.drawable.appoxee_custom_icon, channel)
            notificationConfig.accentColorHex("#FF0000")
            notificationConfig.largeIconDrawableId(R.drawable.notification_large_icon)
            config.notificationConfig = notificationConfig.build()

            val appId:Int
            val apiKey: String
            val isRelease = BuildConfig.BUILD_TYPE == "release"
            if (!isRelease){
                appId = PluginConfigurationHelper.getConfigurationValue(SWRVE_ACCOUNT_ID_SANDBOX)?.toInt() ?: 0
                apiKey = PluginConfigurationHelper.getConfigurationValue(SWRVE_SANDBOX_KEY) ?: ""
            }else{
                appId = PluginConfigurationHelper.getConfigurationValue(SWRVE_ACCOUNT_ID_PRODUCTION)?.toInt() ?: 0
                apiKey = PluginConfigurationHelper.getConfigurationValue(SWRVE_PRODUCTION_KEY) ?: ""
            }

            config.setNotificationListener { pushJson ->
                Log.wtf("Received push", "of body: " + pushJson.toString(1))
            }
            if (appId != 0 && apiKey.isNotEmpty())
                SwrveSDK.createInstance(customApp, appId, apiKey, config)
        } catch (ex: IllegalArgumentException) {
            Log.e("SwrveDemo", "Could not initialize the Swrve SDK", ex)
        }
    }

    override fun setParams(params: Map<*, *>) {
        super.setParams(params)
        val stringMap = params as Map<String, String>
        PluginConfigurationHelper.setConfigurationMap(stringMap)
    }

    /**
     * Get the value of the key present in plugin_configurations.json
     * @param params parameters
     * @param key key of the parameter
     * @return correspondent value of the parameter with key `key`
     */
    private fun getValue(
        params: Map<*, *>,
        key: String
    ): String? {
        var returnVal = ""
        if (params[key] != null) {
            returnVal = params[key].toString()
        }
        return returnVal
    }

    /**
     * It is a good practice to make the parameters of the plugin available with this method
     * @return a hash map of the configuration of the plugin
     */
    override fun getConfiguration(): Map<String, String>? {
        return super.getConfiguration()
    }

    override fun logEvent(eventName: String?) {
        super.logEvent(eventName)
        eventName?.let { it ->
            SwrveSDK.event(it.alphaNumericOnly().cutToMaxLength(MAX_PARAM_NAME_LONG), null)
        }
    }

    /**
     * Log event with extra data
     * @param eventName name of the event logged
     * @param params extra data
     */
    override fun logEvent(eventName: String?, params: TreeMap<String, String>?) {
        super.logEvent(eventName, params)
        Log.wtf("** eventName", "is " + eventName)
        Log.wtf("** params", "is " + params)
        params?.let { it ->
            val newTree = TreeMap<String, String>()
            for ((key, value) in it.entries) {
                newTree[key.alphaNumericOnly().cutToMaxLength(MAX_PARAM_NAME_LONG)] = value.alphaNumericOnly().cutToMaxLength(MAX_PARAM_VALUE_LONG)
            }
            eventName?.let { event ->
                var articleViewedEvent: String? = null
                for((key, value) in newTree.entries){
                    if(key.equals("Screen_Name", ignoreCase = true) && (value.startsWith("Article") || value.startsWith(" Article"))){
                        articleViewedEvent = "Article_Viewed"
                    }
                }
                when {
                    articleViewedEvent != null -> {
                        SwrveSDK.event(articleViewedEvent.alphaNumericOnly().cutToMaxLength(MAX_PARAM_NAME_LONG),newTree)
                    }
                    else -> {
                        SwrveSDK.event(event.alphaNumericOnly().cutToMaxLength(MAX_PARAM_NAME_LONG),newTree)
                    }
                }

            }
        }
    }


    override fun startTimedEvent(eventName: String?) {
        super.startTimedEvent(eventName)
        logEvent(eventName)
    }

    override fun startTimedEvent(eventName: String?, params: TreeMap<String, String>) {
        super.startTimedEvent(eventName, params)
        logEvent(eventName, params)
    }

    override fun endTimedEvent(eventName: String?) {
        super.endTimedEvent(eventName)
        logEvent(eventName)
    }

    override fun endTimedEvent(eventName: String?, params: TreeMap<String, String>) {
        super.endTimedEvent(eventName, params)
        logEvent(eventName, params)
    }

    override fun logPlayEvent(currentPosition: Long) {
        super.logPlayEvent(currentPosition)
        logEvent(PLAY_EVENT)
    }

    /**
     * Set the User Id (UUID) on the Analytics Agent
     *
     * @param userId
     */
    override fun sendUserID(userId: String?) {
        super.sendUserID(userId)
    }

    override fun logVideoEvent(eventName: String?, params: TreeMap<String, String>) {
        super.logVideoEvent(eventName, params)
        logEvent(eventName, params)
    }

    /**
     * Track a when player paused.
     *
     * @param currentPosition
     */
    override fun logPauseEvent(currentPosition: Long) {
        super.logPauseEvent(currentPosition)
        logEvent(PAUSE_EVENT)
    }

    /**
     * Track when player stop.
     *
     * @param currentPosition
     */
    override fun logStopEvent(currentPosition: Long) {
        super.logStopEvent(currentPosition)
        logEvent(STOP_EVENT)
    }

    override fun setScreenView(activity: Activity?, screenView: String) {
        super.setScreenView(activity, screenView)
        val map = TreeMap<String, String>()
        val screenName = if (screenView.contains("ATOM Article", ignoreCase = true)){
            val title = screenView.replace("ATOM Article", "Article").trim()
              title
        }else{
             screenView
        }
        map["Screen name"] = screenName.cutToMaxLength(MAX_SCREEN_NAME_LONG)
        logEvent("screen_visit",map)
    }
}

fun String.cutToMaxLength(maxLength: Int): String{
    return if (this.length > maxLength){
        this.substring(0, maxLength)
    }else{
        this
    }
}

fun String.alphaNumericOnly(): String{
    val regex = Regex("[^A-Za-z0-9_ ]")
    return regex.replace(this.trim(), "").replace(" ", "_")
}
