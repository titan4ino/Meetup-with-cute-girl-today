package com.dating.date.pop

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.*
import com.yandex.metrica.YandexMetrica
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    enum class UIState { FIRST, SECOND, FOURTH, LOADING }

    private val currentState = MutableLiveData<UIState>()


    var isNext = false
    private var count = 0
    private var addmob_app_id = ""
    private var addmob_banner_id = ""
    private var addmob_interstitial_id = ""
    private lateinit var adView_banner: AdView
    private lateinit var mInterstitialAd: InterstitialAd
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        currentState.postValue(UIState.FIRST)
        currentState.observe(this) { newState ->
            when (newState) {
                UIState.FIRST -> {
                    first.setVisible(true)
                    second.setVisible(false)
                    fourth.setVisible(false)
                    progress.setVisible(false)
                }
                UIState.SECOND -> {
                    first.setVisible(false)
                    second.setVisible(true)
                    fourth.setVisible(false)
                    progress.setVisible(false)
                }
                UIState.FOURTH -> {
                    first.setVisible(false)
                    second.setVisible(false)
                    fourth.setVisible(true)
                    progress.setVisible(false)
                }
                else -> {
                    first.setVisible(false)
                    second.setVisible(false)
                    fourth.setVisible(false)
                    progress.setVisible(true)
                }
            }
        }


        addmob_app_id =
            PreferenceManager.getDefaultSharedPreferences(this).getString("app_id", "ca-app-pub-3940256099942544/3419835294").toString()
        addmob_banner_id =
            PreferenceManager.getDefaultSharedPreferences(this).getString("banner_id", "ca-app-pub-3940256099942544/6300978111")
                .toString()
        addmob_interstitial_id =
            PreferenceManager.getDefaultSharedPreferences(this).getString("interstitial_id", "ca-app-pub-3940256099942544/1033173712")
                .toString()

        if (addmob_app_id.isEmpty()) addmob_app_id = "ca-app-pub-3940256099942544/3419835294"
        if (addmob_banner_id.isEmpty()) addmob_banner_id = "ca-app-pub-3940256099942544/6300978111"
        if (addmob_interstitial_id.isEmpty()) addmob_interstitial_id = "ca-app-pub-3940256099942544/1033173712"

        MobileAds.initialize(this, addmob_app_id)

        adView_banner = AdView(this)
        createLayout()
        val adRequest = AdRequest.Builder().build()
        adView_banner.loadAd(adRequest)

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = addmob_interstitial_id
//        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712" //test

        mInterstitialAd.loadAd(AdRequest.Builder().build())

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }

        login.setOnClickListener {
            currentState.postValue(UIState.LOADING)
            Handler().postDelayed({
                currentState.postValue(UIState.FIRST)
                showAlert("Server is unavailable")
            }, 1000)
        }
        signUp.setOnClickListener {
            currentState.postValue(UIState.SECOND)
        }
        no.setOnClickListener {
            showAlert("You should be 18+ to proceed.")
        }
        yes.setOnClickListener {
            currentState.postValue(UIState.FOURTH)
            if (mInterstitialAd.isLoaded) {
                mInterstitialAd.show()
            } else {
                Log.e("TAG", "The interstitial wasn't loaded yet.")
            }
        }
        terms.paintFlags = terms.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        terms.setOnClickListener {
            startActivity(Intent(this, PolicyActivity::class.java))
        }
        signUpButton.setOnClickListener {
            val emailString = email.text.toString().trim()
            val pwString = password.text.toString().trim()
            if (!isValidEmail(emailString)) {
                showAlert("Please type in correct email")
            } else if (pwString.isEmpty()) {
                showAlert("Password should not be empty")
            } else {
                currentState.postValue(UIState.LOADING)
                Handler().postDelayed({
                    currentState.postValue(UIState.FOURTH)
                    showAlert("Server is unavailable")
                }, 1000)
            }
        }
    }


    fun createLayout() {

        // Get the layout from xml
        val layout: ConstraintLayout = main
        val cs = ConstraintSet()

        // Create layout params for content size
        var params: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        // Create AdView
        var adView = AdView(this)
        // Set Ad Size
        adView.setAdSize(AdSize.BANNER)
        // Set Ad Unit Id
//        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111")//test
        adView.setAdUnitId(addmob_banner_id)
        // Generate a View Id
        adView.id = View.generateViewId()
        // Set the size params
        adView.layoutParams = params
        // Add it to the layout
        layout.addView(adView)

        // Create ConstraintSet
        cs.clone(layout)

        // Bottom of the banner to bottom of parent, 16dp
        cs.connect(adView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        // Center it horizontally with the parent.
        cs.centerHorizontally(adView.id, ConstraintSet.PARENT_ID)
        // Set horizontal bias

        // Since the height is already set, these constraints would be enough to
        // keep the banner to the bottom of the page. Apply the constraints to the layout
        cs.applyTo(layout)

        adView_banner = adView

    }

    private fun yandexEvent(event: String) =
        YandexMetrica.getReporter(applicationContext, API_KEY)
            .reportEvent(event)

    override fun onBackPressed() {
        when (currentState.value) {
            UIState.FIRST -> {
                super.onBackPressed()
            }
            UIState.SECOND -> {
                currentState.postValue(UIState.FIRST)
            }
            UIState.FOURTH -> {
                currentState.postValue(UIState.SECOND)
            }
            else -> {
                //nothing
            }
        }
    }

    private fun isValidEmail(target: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(target) || target == null) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message) // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton(android.R.string.yes, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun View.setVisible(visible : Boolean) {
        if (visible) {
            this.visibility = View.VISIBLE
        } else {
            this.visibility = View.GONE
        }
    }
}