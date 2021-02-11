package test_com.dati3ng.datxe.pop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

public class OneSignalBackground extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(final OSNotificationReceivedResult receivedResult) {
        OverrideSettings overrideSettings = new OverrideSettings();
        overrideSettings.extender = builder -> {

            if (!receivedResult.restoring) {
                return builder.setChannelId(getString(R.string.app_name));
            }
            return builder;
        };
        String title = receivedResult.payload.title;
        Log.d("title", title);
        String body  = receivedResult.payload.body;

        if(title.contains("banner"))
        {
            Log.d("title", title);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("url", body);
            editor.commit();
            Intent showStartapp = new Intent(this, Banner.class);
            showStartapp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(showStartapp);
        }
        return true;
    }
}