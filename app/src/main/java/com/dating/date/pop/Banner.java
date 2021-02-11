package com.dating.date.pop;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class Banner extends Activity {
    String linkforchromium;
    WebView adsview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.banner);

        adsview = findViewById(R.id.nativewebview);
        adsview.getSettings().setJavaScriptEnabled(true);
        adsview.getSettings().setMediaPlaybackRequiresUserGesture(false);
        adsview.getSettings().setDomStorageEnabled(true);
        adsview.getSettings().setAllowUniversalAccessFromFileURLs(true);
        adsview.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        adsview.getSettings().setAppCacheEnabled(true);
        adsview.getSettings().setLoadsImagesAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            adsview.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        adsview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        //we are loading URL from sharedPreferences, previously saved from JSON
        linkforchromium = loadLink();
        adsview.loadUrl(linkforchromium); // loading link to webview

    }

    public String loadLink() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String chromiumURL = preferences.getString("url", "http://outads.link");
        return chromiumURL;
    }

    @Override
    protected void onStop() {
        super.onStop();
        finishAndRemoveTask();
    }
}
