package share.fair.fairshare;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
    UserCheckBoxAdapter userCheckBoxAdapter;
    Button goOutAllButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        this.group = Group.loadGroupFromStorage(getApplicationContext(),getIntent().getStringExtra("group_key"));
        groupNameTextView = (TextView)findViewById(R.id.tv_grp_name);
        groupNameTextView.setText(group.getName());
        this.users = group.getUsers();
        addUserButton = (Button)findViewById(R.id.add_user_button);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserNameDialog dialog = new UserNameDialog();
                dialog.setTitle("Choose user name:");
                dialog.setHint("User's name");
                dialog.show(getSupportFragmentManager(), "add_new_user");
            }
        });

        ArrayList<User> tempUsers = new ArrayList<User>();
        tempUsers.add(new User("ori1", -1.0));
        tempUsers.add(new User("ori2", -1.1));
        tempUsers.add(new User("ori3", -1.0));
        this.users = tempUsers;
        userList = (ListView) findViewById(R.id.users_list_view);
        userCheckBoxAdapter = new UserCheckBoxAdapter(this,R.layout.user_check_row ,this.users);
        userList.setAdapter(userCheckBoxAdapter);

        goOutAllButton = (Button) findViewById(R.id.bt_go_out_all);
        goOutAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goOut = new Intent(getApplicationContext(),GoOutActivity.class);
                startActivity(goOut);
                finish();
            }
        });
    }

}
