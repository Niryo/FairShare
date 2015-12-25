package share.fair.fairshare.activities;

import android.content.SharedPreferences;

import com.orm.SugarApp;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.util.List;

import share.fair.fairshare.R;


@ReportsCrashes(mailTo = "niryosef89@gmail.com",
        mode = ReportingInteractionMode.DIALOG,
        resDialogText = R.string.crash_dialog_text,
        resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
        resDialogTitle = R.string.crash_dialog_title // optional. default is your application name
)
public class App extends SugarApp {
    public GroupActivity activity = null; //holds the current groupActivity. It will indicate if the groupActivity is running or not.
    private String currentVersion = "betaV1.3"; //the current version

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this); //init the crash report library
        Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(this, "nLLyqbfak5UsJbwJ086zWMCr5Ux6RvzXOM1kBpX3", "sauupds6DzHf2EroSxBjbnORMgMLbY87UKbFW0u9");
        ParseInstallation.getCurrentInstallation().saveInBackground();
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
                }
            }

        });

    }


}
