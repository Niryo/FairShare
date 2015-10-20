package share.fair.fairshare;

import com.orm.SugarApp;
import com.parse.Parse;


/**
 * Created by Nir on 09/10/2015.
 */
public class App extends SugarApp {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(getApplicationContext(), "nLLyqbfak5UsJbwJ086zWMCr5Ux6RvzXOM1kBpX3", "sauupds6DzHf2EroSxBjbnORMgMLbY87UKbFW0u9");
    }
}
