package share.fair.fairshare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class GoOutActivity extends AppCompatActivity {

    TextView title;
    Button backToGroup;
    ListView goOutList;
    ArrayAdapter goOutAdpater;
    ArrayList<User> nameList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_out);
        title = (TextView)findViewById(R.id.go_out_title);
        backToGroup = (Button)findViewById(R.id.back_to_group_button);
        backToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo: go back to the last screen(group screen)
            }
        });

        nameList = new ArrayList<User>();
        nameList.add(new User("kipi1", -1.23));
        nameList.add(new User("kipi2", -1.23));
        nameList.add(new User("kipi3", -1.23));
        nameList.add(new User("kipi4", -1.23));
        nameList.add(new User("kipi5", -1.23));
        nameList.add(new User("kipi6", -1.23));
        nameList.add(new User("kipi7", -1.23));
        nameList.add(new User("kipi8", -1.23));
        nameList.add(new User("kipi9", -1.23));

        goOutList = (ListView) findViewById(R.id.go_out_list_view);
        goOutAdpater = new GoOutAdapter(this, R.layout.user_go_out_row ,nameList);
        goOutList.setAdapter(goOutAdpater);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_go_out, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
