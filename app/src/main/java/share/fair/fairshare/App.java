package share.fair.fairshare;

import android.util.Log;

import com.orm.SugarApp;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.PushService;
import com.parse.SaveCallback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.acl.Group;


/**
 * Created by Nir on 09/10/2015.
 */
public class App extends SugarApp {
   public GroupActivity activity=null;

    @Override
    public void onCreate() {
        super.onCreate();
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




    }

    public void registerGroupActivity(GroupActivity activity){
        this.activity=activity;
    }
}
