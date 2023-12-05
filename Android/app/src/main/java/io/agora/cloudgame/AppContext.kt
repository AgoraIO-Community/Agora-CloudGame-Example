package io.agora.cloudgame

import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication

class AppContext : MultiDexApplication() {

    companion object {

        private var globalSettings: GlobalSettings? = null
        var instance: AppContext? = null

        @Synchronized
        fun get(): AppContext? {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        instance = this
    }

    fun getGlobalSettings(): GlobalSettings? {
        if (globalSettings == null) {
            globalSettings = GlobalSettings()
        }
        return globalSettings
    }
}