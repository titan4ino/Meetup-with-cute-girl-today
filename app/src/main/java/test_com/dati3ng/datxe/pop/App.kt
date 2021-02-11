package test_com.dati3ng.datxe.pop

import android.app.Application
import com.onesignal.OneSignal
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig

val IS_PASSED = "is_passed"

val API_KEY = "5f2ee307-d9ea-48b3-88e3-41dee28adcbb"


class App :Application() {

    override fun onCreate() {
        super.onCreate()

        initYandexMetrica()
        initOneConfig()
    }

    private fun initYandexMetrica() {
        val config =
            YandexMetricaConfig.newConfigBuilder(API_KEY).build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(this)
    }

    private fun initOneConfig() {
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init();
    }



}