package com.cme.zappanalyticsswrve

import android.content.Context
import com.applicaster.analytics.BaseAnalyticsAgent
import com.applicaster.util.StringUtil
import com.swrve.sdk.SwrveSDK
import java.util.*

class SwrveAnalyticsAgent : BaseAnalyticsAgent() {

    private val TAG: String = SwrveAnalyticsAgent::class.java.simpleName

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
    private var mobileAppAccountId: String? = null
    private var anonymizeUserIp = false
    private var screenViews = false
    private var shouldSetClientId = false

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

        //SwrveSDK.g

    }

    override fun setParams(params: Map<*, *>) {
        super.setParams(params)
        mobileAppAccountId = getValue(params, MOBILE_APP_ACCOUNT_ID_IDENTIFIER)
        anonymizeUserIp = getValue(params, ANONYMIZE_USER_IP_IDENTIFIER) == "1"
        screenViews = getValue(params, SCREEN_VIEWS_IDENTIFIER) == "1"
        shouldSetClientId = getValue(params, DO_NOT_SET_CLIENT_ID) != "1"
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
        val configuration =
            super.getConfiguration()
        if (mobileAppAccountId != null) {
            configuration[MOBILE_APP_ACCOUNT_ID_IDENTIFIER] = mobileAppAccountId!!
            configuration[ANONYMIZE_USER_IP_IDENTIFIER] = anonymizeUserIp.toString()
            configuration[SCREEN_VIEWS_IDENTIFIER] = screenViews.toString()
        }
        return configuration
    }

    override fun logEvent(eventName: String?) {
        super.logEvent(eventName)
        SwrveSDK.event(eventName)
    }

    /**
     * Log event with extra data
     * @param eventName name of the event logged
     * @param params extra data
     */
    override fun logEvent(
        eventName: String?,
        params: TreeMap<String?, String?>?
    ) {
        super.logEvent(eventName, params)
        val label: String? = params?.let {
            getLabel(it)
        }
    }

    private fun getLabel(map: TreeMap<String?, String?>): String? {
        val notAvailableString = "N/A"
        // Build the labels param.
        var labelsString: String? = null
        if (map != null) {
            val labels = StringBuilder()
            for (key in map.keys) {
                var value = map[key]
                if (StringUtil.isEmpty(value)) {
                    value = notAvailableString
                }
                val label = String.format("%s=%s;", key, value)
                labels.append(label)
            }
            if (labels.length > 0) {
                // If it's not empty, we need to remove the last ';'
                labels.setLength(labels.length - 1)
            }
            labelsString = labels.toString()
        }
        return labelsString
    }


    /**
     * Set the User Id (UUID) on the Analytics Agent
     *
     * @param userId
     */
    override fun sendUserID(userId: String?) {
        super.sendUserID(userId)
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
}