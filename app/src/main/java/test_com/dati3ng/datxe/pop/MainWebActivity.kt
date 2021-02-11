package test_com.dati3ng.datxe.pop

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_mainweb.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener


class MainWebActivity : AppCompatActivity() {

    private val FILE_CHOOSER_RESULTCODE = 1
    private var mUploadMessage: ValueCallback<Array<Uri>>? = null
    var isNext=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainweb)

        val url = getLastURL()
        Log.e("urla", url!!)

        webview.loadUrl(url!!)

        webview.settings.apply {
            javaScriptEnabled = true
            setAppCacheEnabled(true)
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(true)
            builtInZoomControls = false
            blockNetworkImage = false
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            javaScriptCanOpenWindowsAutomatically = true
            allowFileAccess = true
            domStorageEnabled = true
            setSupportMultipleWindows(true)
            loadWithOverviewMode = true
            allowContentAccess = true
            setGeolocationEnabled(true)
            allowUniversalAccessFromFileURLs = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mediaPlaybackRequiresUserGesture = false
            }
        }

        setEventListener(
            this,
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

                }
            })

        //downloadlistener enabled
        webview.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            //filename of downloading file
            var filename = URLUtil.guessFileName(url, contentDisposition, mimetype)

            //alertdialog builder created
            val builder = AlertDialog.Builder(this@MainWebActivity)
            //alertdialog title set
            builder.setTitle("Download")
            //alertdialog message set
            builder.setMessage("Do you want to save $filename")
            //if yes clicks,following code will executed
            builder.setPositiveButton("Yes") { dialog, which ->
                //DownloadManager request created based on url
                val request = DownloadManager.Request(Uri.parse(url))
                //get cookie
                val cookie = CookieManager.getInstance().getCookie(url)
                //add cookie to request
                request.addRequestHeader("Cookie", cookie)
                //add User-agent to request
                request.addRequestHeader("User-Agent", userAgent)
                //Files are scanned before downloading
                request.allowScanningByMediaScanner()
                //download notification is visible while downloading and after download completion
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                //DownloadManager Service created
                val downloadmanager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                //Files are downloaded to Download folder
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
                //download starts
                downloadmanager.enqueue(request)
            }
            builder.setNegativeButton("Cancel")
            { dialog, which ->
                //dialog cancels
                dialog.cancel()
            }
            //alertdialog created
            val dialog: AlertDialog = builder.create()
            //shows alertdialog
            dialog.show()
        }

        webview.webChromeClient = object : WebChromeClient() {

            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.e(
                    "JSERROR", consoleMessage.message() + " -- From line "
                            + consoleMessage.lineNumber() + " of "
                            + consoleMessage.sourceId()
                );
                return super.onConsoleMessage(consoleMessage);
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: WebChromeClient.FileChooserParams?
            ): Boolean {

                mUploadMessage = filePathCallback
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val intent = fileChooserParams!!.createIntent()
                    try {
                        isNext=true
                        startActivityForResult(
                            intent,
                            FILE_CHOOSER_RESULTCODE
                        );
                    } catch (e: ActivityNotFoundException) {
                        mUploadMessage = null;
                        Toast.makeText(
                            this@MainWebActivity,
                            "Cannot open file chooser",
                            Toast.LENGTH_LONG
                        ).show();
                        return false;
                    }
                } else {

                }
                return true
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val result = view!!.hitTestResult
                val data = result.extra
                view.loadUrl(data!!)
                return false
            }

        }

        webview.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url!!.startsWith("http")) {
                    view?.loadUrl("$url")
                    Log.e("LOG_TAG", "shouldOverrideUrlLoading URL1: $url ")
                } else {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.data = Uri.parse(url)
                    isNext=true
                    startActivity(intent)
                }
                return true
            }

        }

    }


    override fun onBackPressed() {
        if (webview != null && webview.canGoBack())
            webview.goBack()
    }

    override fun onPause() {
        super.onPause()
        webview.onPause()
        webview.pauseTimers()
    }

    override fun onResume() {
        super.onResume()
        webview.onResume()
        webview.resumeTimers()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_RESULTCODE) {
            if (mUploadMessage == null) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mUploadMessage!!.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        data
                    )
                )
            }
            mUploadMessage = null
        }
    }

    override fun onStop() {
        setlastURL(webview.url!!)
        super.onStop()
        if (!isNext)
        finish()
    }

    private fun getParametr() =
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
            .getString("param", "")

    fun setlastURL(value: String) {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString("url", value)
        editor.apply()
    }

    private fun getLastURL() =
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getString("url", getParametr())
}
