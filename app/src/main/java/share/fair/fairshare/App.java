package share.fair.fairshare;

import org.acra.*;
import org.acra.annotation.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;

import com.orm.SugarApp;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import share.fair.fairshare.activities.GroupActivity;


/**
 * Created by Nir on 09/10/2015.
 */
@ReportsCrashes(mailTo = "niryosef89@gmail.com",
        mode = ReportingInteractionMode.DIALOG,
        resDialogText = R.string.crash_dialog_text,
        resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
        resDialogTitle = R.string.crash_dialog_title // optional. default is your application name
        )
public class App extends SugarApp {
    private String currentVersion="betaV1.3";
   public GroupActivity activity=null;

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
        Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(this, "nLLyqbfak5UsJbwJ086zWMCr5Ux6RvzXOM1kBpX3", "sauupds6DzHf2EroSxBjbnORMgMLbY87UKbFW0u9");
        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
        ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });
        verifyVersion();




    }

    public void registerGroupActivity(GroupActivity activity){
        this.activity=activity;
    }

    private void verifyVersion (){
        final SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);


        ParseQuery<ParseObject> query = ParseQuery.getQuery("VERSION");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects != null) {
                    ParseObject parseObject = objects.get(0);
                    String lastVersion = parseObject.getString("version");
                        SharedPreferences.Editor editor = settings.edit();
                    if (!currentVersion.equals(lastVersion)) {
                        editor.putBoolean("isLegalVersion", false);
                        editor.commit();
                    } else {
                        editor.putBoolean("isLegalVersion", true);
                        editor.commit();

                }
            } }


        });

    }


}
