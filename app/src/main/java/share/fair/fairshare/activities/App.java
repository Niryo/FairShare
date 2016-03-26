package share.fair.fairshare.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.orm.SugarApp;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.util.List;

import share.fair.fairshare.CloudCommunication;
import share.fair.fairshare.PushService;
import share.fair.fairshare.R;


//@ReportsCrashes(mailTo = "niryosef89@gmail.com",
//        mode = ReportingInteractionMode.DIALOG,
//        resDialogText = R.string.crash_dialog_text,
//        resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
//        resDialogTitle = R.string.crash_dialog_title // optional. default is your application name
//)
public class App extends SugarApp {
    public GroupActivity activity = null; //holds the current groupActivity. It will indicate if the groupActivity is running or not.
    private String currentVersion = "betaV1.6"; //the current version


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this); //init the crash report library
        PushService.init(this);
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        verifyVersion();
    }

    /**
     * Register the current instance of groupActivity
     * @param activity Group activity
     */
    public void registerGroupActivity(GroupActivity activity) {
        this.activity = activity;
    }

    /**
     * Verifies that we are using the latest compativle version of fairShare
     */
    private void verifyVersion() {
        //we will check in parse The VERSION table to verify that we have the latest compatible
        //version and if not, we will set in the shared preferences a value that indicates
        // illegal version:

        final SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);

        CloudCommunication.queryVersion(new CloudCommunication.CloudCallback() {
            @Override
            public void done(FirebaseError firebaseError, DataSnapshot dataSnapshot) {
                if(firebaseError==null){
                    String lastVersion = dataSnapshot.getValue().toString();
                    SharedPreferences.Editor editor = settings.edit();
                    if (!currentVersion.equals(lastVersion)) {
                        editor.putBoolean("isLegalVersion", false);
                        editor.commit();
                    } else {
                        editor.putBoolean("isLegalVersion", true);
                        editor.commit();
                    }
                }
            }
        });

    }
}
