package com.cme.zappanalyticsswrve

object PluginConfigurationHelper {

    private var pluginConfiguration: HashMap<String, String> = HashMap()

    fun setConfigurationMap(pluginConfiguration: Map<String, String>) {
        this.pluginConfiguration.putAll(pluginConfiguration)
    }

    fun getConfigurationValue(key: String): String? {
        return this.pluginConfiguration[key]
    }
}