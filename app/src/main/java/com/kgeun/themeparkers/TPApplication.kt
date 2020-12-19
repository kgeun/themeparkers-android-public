package com.kgeun.themeparkers

import android.app.Application
import android.graphics.Typeface
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.kgeun.themeparkers.util.diModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TPApplication : Application() {

    private var isMainRunning = false
    private var isVerifyPasswordRunning = false

    private var mTypeface : Typeface? = null
    private var mTypefaceBold : Typeface? = null

    companion object {
//        private var mTypeface : Typeface? = null
//        private var mTypefaceBold : Typeface? = null
        lateinit var instance: TPApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@TPApplication)
            modules(diModule)
        }
        instance = this@TPApplication

        initRemoteConfig()
    }

    private fun setNanumGothicTypeface() {
        try {
//            mTypeface = Typeface.createFromAsset(assets, getString(R.string.nanum_gothic_font))
//            mTypefaceBold = Typeface.createFromAsset(assets, getString(R.string.nanum_gothic_bold_font))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun getTypeface(): Typeface? {
        if (mTypeface == null) {
            setNanumGothicTypeface()
        }
        return mTypeface
    }

    fun getTypefaceBold(): Typeface? {
        if (mTypefaceBold == null) {
            setNanumGothicTypeface()
        }
        return mTypefaceBold
    }

    fun initRemoteConfig() {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 60
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
    }
}