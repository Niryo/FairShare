package share.fair.fairshare;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
    Button toActionsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        this.group = Group.loadGroupFromStorage(getApplicationContext(), getIntent().getStringExtra("group_key"));
        groupNameTextView = (TextView) findViewById(R.id.tv_grp_name);
        groupNameTextView.setText(group.getName());
        this.users = group.getUsers();

        userListView = (ListView) findViewById(R.id.users_list_view);
        userCheckBoxAdapter = new UserCheckBoxAdapter(this, R.layout.user_check_row, this.users);
        userListView.setAdapter(userCheckBoxAdapter);
       // registerForContextMenu(userListView);

        addUserButton = (Button) findViewById(R.id.add_user_button);
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
                Intent goOut = new Intent(getApplicationContext(), GoOutActivity.class);
                goOut.putExtra("goOutList", users);
                startActivityForResult(goOut, GO_OUT_REQUEST);
                clearChecked();
            }
        });
        goOutCheckedButton = (Button) findViewById(R.id.bt_go_out_checked);
        goOutCheckedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<User> checkedUsers = userCheckBoxAdapter.getCheckedArray();
                if (checkedUsers.isEmpty()) {
                    //todo: other way to handle error?
                    toastGen(getApplicationContext(), "No user is checked!");
                    return;
                } else {
                    Intent goOut = new Intent(getApplicationContext(), GoOutActivity.class);
                    goOut.putExtra("goOutList", checkedUsers);
                    startActivityForResult(goOut, GO_OUT_REQUEST);
                }
                clearChecked();
            }
        });
        backToMain = (Button) findViewById(R.id.bt_back_to_info);
        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(main);
                finish();
            }
        });
        toActionsButton =(Button) findViewById(R.id.to_actions_button);
        toActionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent actions = new Intent(getApplicationContext(), ActionsActivity.class);
                actions.putExtra("groupLog", group.getGroupLog());
                startActivity(actions);
            }
        });

        this.group.syncUsers();
        notifyUserListChanged();
this.userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserContextMenuDialog dialog = new UserContextMenuDialog();
        dialog.setUser(users.get(position));
        dialog.show(getFragmentManager(), "UserContextMenuDialog");
    }
});


    }
    private void inviteByMail(String emailAddress){
        Uri.Builder uriBuilder= new Uri.Builder();
        uriBuilder.scheme("http");
        uriBuilder.authority("fair.share.fairshare");
        uriBuilder.appendPath("");
        uriBuilder.appendQueryParameter("groupName", group.getName());
        uriBuilder.appendQueryParameter("groupCloudKey", group.getCloudGroupKey());
        uriBuilder.appendQueryParameter("cloudLogKey", group.getCloudLogKey());

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        i.putExtra(Intent.EXTRA_SUBJECT, "FairShare: invitation to join to a new group");
        i.putExtra(Intent.EXTRA_TEXT, uriBuilder.build().toString());

        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.w("custom", "cant start activity to send mail");
        }
    }


    @Override
    public void notifyUserAdded(String name, String emailAddress) {
        if(!emailAddress.isEmpty()){
            inviteByMail(emailAddress);
        }
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
                ArrayList<User> resultList = (ArrayList<User>) data.getSerializableExtra("resultUserList");
                Action action = (Action) data.getSerializableExtra("action");
                this.group.getGroupLog().AddAction(action);
                for(User user: resultList){
                    toastGen(getApplicationContext(),"username:"+ user.getName()+" bal: "+user.getBalance());
                }
                //users = resultList; //todo: problem if checked list was sent
                uniteLists(resultList);
                userCheckBoxAdapter.notifyDataSetChanged();
                this.group.saveGroupToStorage();
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

    public void notifyUserListChanged(){
        userCheckBoxAdapter.notifyDataSetChanged();
    }
    private void clearChecked(){
       for(int i=0; i< this.userListView.getChildCount(); i++){
           CheckBox checkBox= (CheckBox) this.userListView.getChildAt(i).findViewById(R.id.cb_user_row);
           checkBox.setChecked(false);
       }
        this.userCheckBoxAdapter.clearChecked();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.users_list_view) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(users.get(info.position).getName());
            ArrayList<String> menuItemsList= new ArrayList<>();
            menuItemsList.add("Fast");

            if(users.get(info.position).getEmail().isEmpty()){
                menuItemsList.add("Invite by email");
            }else{
                menuItemsList.add("Edit email or send invitation again");
            }
            String[] menuItems= new String[menuItemsList.size()];
            menuItems = menuItemsList.toArray(menuItems);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        if(item.getTitle().equals("Edit email or send invitation again")){
            //inviteByMail( users.get(info.position).getEmail());
            //toastGen(getApplicationContext(), "Invitation sent to: " + users.get(info.position).getEmail());
        }

   


        return true;
    }


}




