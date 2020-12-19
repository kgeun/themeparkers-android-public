package com.kgeun.themeparkers

import android.content.Context
import android.content.SharedPreferences
import com.kgeun.themeparkers.TPApplication

object TPPrefCtl {

    private const val PREF_NAME = "THEMEPARKERS_PREF"

    private const val PREF_LOGIN_ID = "Pref_LoginID"
    private const val PREF_MASTER_PASSWORD = "Pref_MasterPassword"
    private const val PREF_FINGERPRINT_ALLOW = "Pref_FingerPrint_Allow"
    private const val PREF_COMMON_KEY = "Pref_CommonKey"
    private const val PREF_ENC_KEY = "Pref_EncKey"
    private const val PREF_ACCESS_TOKEN = "Pref_AccessToken"
    private const val PREF_ACCESS_TOKEN_EXPIRE_DATE = "Pref_AccessTokenExpireDate"
    private const val PREF_REFRESH_TOKEN = "Pref_RefreshToken"
    private const val PREF_PUSH_AGREE = "Pref_PushAgree"
    private const val PREF_LOCATION_AGREE = "Pref_LocationAgree"
    private const val PREF_NEWSLETTER_AGREE = "Pref_NewsletterAgree"
    private const val PREF_PUSH_GET_OFF_WORK_AGREE = "Pref_PushGetOffWorkAgree"
    private const val PREF_REGISTER = "Pref_Register"
    private const val PREF_SECOND_AUTH = "Pref_SecondAuth"
    private const val NOTIFICATION_CENTER_LAST_PUSHID = "notificationCenterLastPushID"
    private const val PUSH_ALERT_ID = "pushAlertID"
    private const val CMP_CODE = "cmpCode"
    private const val RESTAURANT_CAPTION = "restaurant_caption"
    private const val PREF_DAY_OF_YEAR = "Pref_day_of_year"


    private const val PREF_SELECTED_THEMEPARK = "Pref_selected_themepark"
    private const val PREF_GOOGLE_API_KEY = "Pref_google_api_key"
    private const val PREF_WAITTIME_ALLOWED = "Pref_Waittime_allowed"
    private const val PREF_SERVICE_RUNNING = "Pref_service_running"


    private val pref: SharedPreferences =
        TPApplication.instance.applicationContext.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    private fun set(key: String, value: Any?) {
        when (value) {
            is String? -> pref.edit { it.putString(key, value) }
            is Int -> pref.edit { it.putInt(key, value) }
            is Boolean -> pref.edit { it.putBoolean(key, value) }
            is Float -> pref.edit { it.putFloat(key, value) }
            is Long -> pref.edit { it.putLong(key, value) }
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    private inline fun <reified T : Any> get(key: String, defaultValue: T? = null): T {
        return when (T::class) {
            String::class -> pref.getString(key, defaultValue as? String) as T
            Int::class -> pref.getInt(key, defaultValue as? Int ?: -1) as T
            Boolean::class -> pref.getBoolean(key, defaultValue as? Boolean ?: false) as T
            Float::class -> pref.getFloat(key, defaultValue as? Float ?: -1f) as T
            Long::class -> pref.getLong(key, defaultValue as? Long ?: -1) as T
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    fun setCommonKey(value: String) {
        set(PREF_COMMON_KEY, value)
    }

    fun getCommonKey(): String? {
        return get(PREF_COMMON_KEY, "")
    }

//    fun setEncKey(value: String) {
//        if (value == "") {
//            set(PREF_ENC_KEY, value)
//        } else {
//            set(PREF_ENC_KEY, NSGCrypto.prefEncrypt(value))
//        }
//    }

//    fun getEncKey(): String? {
//        val retValue = get(PREF_ENC_KEY, "")
//
//        return if (retValue.equals("")) {
//            retValue
//        } else {
//            NSGCrypto.prefDecrypt(retValue!!)
//        }
//    }
//
//    fun setAccessToken(value: String) {
//        if (value == "") {
//            set(PREF_ACCESS_TOKEN, value)
//        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                set(PREF_ACCESS_TOKEN, NSGCrypto.prefEncrypt(value))
//            } else {
//                set(PREF_ACCESS_TOKEN, value)
//            }
//        }
//    }

//    fun getAccessToken(): String? {
//        val retValue = get(PREF_ACCESS_TOKEN, "")
//
//        return if (retValue.equals("")) {
//            retValue
//        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                NSGCrypto.prefDecrypt(retValue!!)
//            } else {
//                retValue
//            }
//        }
//    }
//
//    fun setAccessTokenExpireDate(value: Long) {
//        set(PREF_ACCESS_TOKEN_EXPIRE_DATE, value)
//    }
//
//    fun getAccessTokenExpireDate(): Long? {
//        return get(PREF_ACCESS_TOKEN_EXPIRE_DATE, 0)
//    }
//
//    fun setRefreshToken(value: String) {
//        if (value == "") {
//            set(PREF_REFRESH_TOKEN, value)
//        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                set(PREF_REFRESH_TOKEN, NSGCrypto.prefEncrypt(value))
//            } else {
//                set(PREF_REFRESH_TOKEN, value)
//            }
//        }
//    }
//
//    fun getRefreshToken(): String? {
//        val retValue = get(PREF_REFRESH_TOKEN, "")
//
//        return if (retValue.equals("")) {
//            retValue
//        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                NSGCrypto.prefDecrypt(retValue!!)
//            } else {
//                retValue
//            }
//        }
//    }
//
//    fun setLoginID(value: String) {
//        set(PREF_LOGIN_ID, value)
//    }
//
//    fun getLoginID(): String? {
//        return get(PREF_LOGIN_ID, "")
//    }
//
//    fun setMasterPassword(value: String) {
//        if (value == "") {
//            set(PREF_MASTER_PASSWORD, value)
//        } else {
//            set(PREF_MASTER_PASSWORD, NSGCrypto.prefEncrypt(value))
//        }
//    }
//
//    fun getMasterPassword(): String? {
//        val retValue = get(PREF_MASTER_PASSWORD, "")
//
//        return if (retValue.equals("")) {
//            retValue
//        } else {
//            NSGCrypto.prefDecrypt(retValue!!)
//        }
//    }

    fun setFingerPrintAllow(value: Boolean) {
        set(PREF_FINGERPRINT_ALLOW, value)
    }

    fun getFingerPrintAllow(): Boolean? {
        return get(PREF_FINGERPRINT_ALLOW, false)
    }

    fun setPushAllow(value: Boolean) {
        set(PREF_PUSH_AGREE, value)
    }

    fun getPushAllow(): Boolean? {
        return get(PREF_PUSH_AGREE, false)
    }

    fun setLocationAllow(value: Boolean) {
        set(PREF_LOCATION_AGREE, value)
    }

    fun getLocationAllow(): Boolean? {
        return get(PREF_LOCATION_AGREE, false)
    }

    fun setNewsletterAllow(value: Boolean) {
        set(PREF_NEWSLETTER_AGREE, value)
    }

    fun getNewsletterAllow(): Boolean? {
        return get(PREF_NEWSLETTER_AGREE, false)
    }


    fun setPushGetOffWorkAllow(value: Boolean) {
        set(PREF_PUSH_GET_OFF_WORK_AGREE, value)
    }

    fun getPushGetOffWorkAllow(): Boolean? {
        return get(PREF_PUSH_GET_OFF_WORK_AGREE, false)
    }

    fun setRegister(value: Boolean) {
        set(PREF_REGISTER, value)
    }

    fun getRegister(): Boolean? {
        return get(PREF_REGISTER, false)
    }

    fun setPushAlertID(value: Long) {
        set(PUSH_ALERT_ID, value)
    }

    fun getPushAlertID(): Long? {
        return get(PUSH_ALERT_ID, 0)
    }

    fun setNotificationCenterLastPushID(value: Long) {
        set(NOTIFICATION_CENTER_LAST_PUSHID, value)
    }

    fun getNotificationCenterLastPushID(): Long? {
        return get(NOTIFICATION_CENTER_LAST_PUSHID, 0)
    }


    fun setCmpCode(value: String) {
        set(CMP_CODE, value)
    }

    fun getCmpCode(): String? {
        return get(CMP_CODE)
    }

    fun setRestaurantCaption(value: String) {
        set(RESTAURANT_CAPTION, value)
    }

    fun getRestaurantCaption(): String? {
        return get(RESTAURANT_CAPTION)
    }

    fun setSecondAuth(value: Boolean) {
        set(PREF_SECOND_AUTH, value)
    }

    fun getSecondAuth(): Boolean? {
        return get(PREF_SECOND_AUTH, false)
    }

    fun setDayOfYear(value: Int) {
        set(PREF_DAY_OF_YEAR, value)
    }

    fun getDayOfYear(): Int? {
        return get(PREF_DAY_OF_YEAR, 0)
    }

    fun clear() {
        pref.edit().clear().apply()
    }

    fun logout() {
//        setLoginID("")
//        setEncKey("")
//        setAccessToken("")
//        setRefreshToken("")
//        setAccessTokenExpireDate(0)
//        setMasterPassword("")
//        setRestaurantCaption("")
//        setCmpCode("")
//
//        setFingerPrintAllow(false)
//        setRegister(false)
//        setSecondAuth(false)
    }


    fun setSelectedThemepark(value: String) {
        set(PREF_SELECTED_THEMEPARK, value)
    }

    fun getSelectedThemepark(): String {
        return get(PREF_SELECTED_THEMEPARK, "")
    }

    fun setGoogleApiKey(value: String) {
        set(PREF_GOOGLE_API_KEY, value)
    }

    fun getGoogleApiKey(): String{
        return get(PREF_GOOGLE_API_KEY, "")
    }

    fun setServiceRunning(value: Boolean) {
        set(PREF_SERVICE_RUNNING, value)
    }

    fun getServiceRunning(): Boolean{
        return get(PREF_SERVICE_RUNNING, false)
    }

}