package share.fair.fairshare;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class GroupActivity extends FragmentActivity {

    TextView groupNameTextView;
    Button addUserButton;
    ArrayList<User> users;
    ListView userList;
    Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        this.group = Group.loadGroupFromStorage(getApplicationContext(),getIntent().getStringExtra("group_key"));
        this.users = group.getUsers();
        addUserButton = (Button)findViewById(R.id.bt_add_user);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterNameDialog dialog = new EnterNameDialog();
                dialog.setTitle("Choose user name:");
                dialog.setHint("User's name");
                dialog.show(getSupportFragmentManager(), "add_new_user");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group, menu);
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
