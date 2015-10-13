package share.fair.fairshare;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class GroupActivity extends FragmentActivity implements UserNameDialog.UserAddedListener {

    static final int GO_OUT_REQUEST = 1;  // The request code
    TextView groupNameTextView;
    Button addUserButton;
    ArrayList<User> users;
    ListView userListView;
    Group group;
    UserCheckBoxAdapter userCheckBoxAdapter;
    Button goOutAllButton;
    Button goOutCheckedButton;
    Button backToMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        this.group = Group.loadGroupFromStorage(getApplicationContext(),getIntent().getStringExtra("group_key"));
        groupNameTextView = (TextView)findViewById(R.id.tv_grp_name);
        groupNameTextView.setText(group.getName());
        this.users = group.getUsers();

        userListView = (ListView) findViewById(R.id.users_list_view);
        userCheckBoxAdapter = new UserCheckBoxAdapter(this,R.layout.user_check_row ,this.users);
        userListView.setAdapter(userCheckBoxAdapter);

        addUserButton = (Button)findViewById(R.id.add_user_button);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserNameDialog dialog = new UserNameDialog();
                dialog.setGroup(group);
                dialog.show(getSupportFragmentManager(), "add_new_user");

            }
        });



        goOutAllButton = (Button) findViewById(R.id.bt_go_out_all);
        goOutAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goOut = new Intent(getApplicationContext(),GoOutActivity.class);
                goOut.putExtra("goOutList", users);
                startActivityForResult(goOut, GO_OUT_REQUEST);
                clearCheked();
            }
        });
        goOutCheckedButton = (Button) findViewById(R.id.bt_go_out_checked);
        goOutCheckedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<User> checkedUsers = userCheckBoxAdapter.getCheckedArray();
                if(checkedUsers.isEmpty()) {
                    //todo: other way to handle error?
                    toastGen(getApplicationContext(),"No user is checked!");
                    return;
                }else{
                    Intent goOut = new Intent(getApplicationContext(),GoOutActivity.class);
                    goOut.putExtra("goOutList", checkedUsers);
                    startActivityForResult(goOut, GO_OUT_REQUEST);
                }
                clearCheked();
            }
        });
    }

    @Override
    public void notifyUserAdded(String name, String emailAddress) {
       User newUser= new User(name,0);
        newUser.setEmail(emailAddress);
        this.group.addUser(getApplicationContext(), newUser);
        users= group.getUsers();
        userCheckBoxAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GO_OUT_REQUEST) {
            if (resultCode == RESULT_OK) {
                ArrayList<User> resultList = (ArrayList<User>) data.getSerializableExtra("result");
                for(User user: resultList){
                    toastGen(getApplicationContext(),"username:"+ user.getName()+" bal: "+user.getBalance());
                }
                //users = resultList; //todo: problem if checked list was sent
                uniteLists(resultList);
                userCheckBoxAdapter.notifyDataSetChanged();
                this.group.saveGroupToStorage(getApplicationContext());


            }
        }
    }
    private void toastGen(Context context,String msg){
        Log.w("user", "in toastGen: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
    private void uniteLists(ArrayList<User> resultList){
        for(int i =0; i< users.size(); i++){
            for(int j =0; j<resultList.size(); j++){
                if ( users.get(i).getId().equals(resultList.get(j).getId()) ){
                    users.set(i, resultList.get(j));
                }
            }
        }
    }

    private void clearCheked(){
       for(int i=0; i< this.userListView.getChildCount(); i++){
           CheckBox checkBox= (CheckBox) this.userListView.getChildAt(i).findViewById(R.id.cb_user_row);
           checkBox.setChecked(false);
       }
        this.userCheckBoxAdapter.clearChecked();
    }
}










