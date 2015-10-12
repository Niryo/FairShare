package share.fair.fairshare;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class GroupActivity extends FragmentActivity implements UserNameDialog.UserAddedListener {

    static final int GO_OUT_REQUEST = 1;  // The request code
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
                dialog.setGroup(group);
                dialog.show(getSupportFragmentManager(), "add_new_user");
            }
        });

        userList = (ListView) findViewById(R.id.users_list_view);
        userCheckBoxAdapter = new UserCheckBoxAdapter(this,R.layout.user_check_row ,this.users);
        userList.setAdapter(userCheckBoxAdapter);



        goOutAllButton = (Button) findViewById(R.id.bt_go_out_all);
        goOutAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goOut = new Intent(getApplicationContext(),GoOutActivity.class);
                goOut.putExtra("goOutList", users );
                startActivityForResult(goOut, GO_OUT_REQUEST);
                finish();
            }
        });
    }


    @Override
//<<<<<<< HEAD
//    public void notifyUserAdded(String name) {
//        this.group.addUser(getApplicationContext(), name);
//=======
    public void notifyUserAdded(String name, String emailAddress) {
       User newUser= new User(name,0);
        newUser.setEmail(emailAddress);
        this.group.addUser(getApplicationContext(),newUser);
        users= group.getUsers();
        userCheckBoxAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == GO_OUT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
                ArrayList<User> resultList = (ArrayList<User>) data.getSerializableExtra("result");
                for(User user: resultList){
                    toastGen(getApplicationContext(),"username:"+ user.getName()+" bal: "+user.getBalance());
                }

            }
        }
    }
    private void toastGen(Context context,String msg){
        Log.w("user", "in toastGen: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}










