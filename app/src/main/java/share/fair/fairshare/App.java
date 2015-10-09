package share.fair.fairshare;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Nir on 09/10/2015.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "nLLyqbfak5UsJbwJ086zWMCr5Ux6RvzXOM1kBpX3", "sauupds6DzHf2EroSxBjbnORMgMLbY87UKbFW0u9");
    }
}
