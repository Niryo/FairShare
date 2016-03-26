package share.fair.fairshare;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

public class RegistrationIntentService extends IntentService {

    // abbreviated tag name
    private static final String TAG = "RegIntentService";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String GCM_TOKEN = "gcmToken";
    private static final String PROJECT_ID= "890576213693";


    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
        InstanceID instanceID = InstanceID.getInstance(this);
        String senderId = PROJECT_ID;
        try {
            // request token that will be used by the server to send push notifications
            String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
            Log.d("custom", "GCM Registration Token: " + token);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GCM_TOKEN, token);
            editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("custom", "Failed to complete token refresh", e);
        }
    }
}