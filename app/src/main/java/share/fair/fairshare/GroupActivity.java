package share.fair.fairshare;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class GroupActivity extends FragmentActivity {

    static final int GO_OUT_REQUEST = 1;  // The request code
    TextView groupNameTextView;
    Button addUserButton;
    ArrayList<User> users;
    ListView userListView;
    FairShareGroup group;
    UserCheckBoxAdapter userCheckBoxAdapter;
    Button goOutAllButton;
    Button goOutCheckedButton;
    Button backToMain;
    Button toActionsButton;
    private Handler notifyUserChangedHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        long groupId = getIntent().getLongExtra("groupId", -1);
        if (groupId == -1) {
            //todo: handle problem;
        }
        this.group = FairShareGroup.loadGroupFromStorage(groupId);
        groupNameTextView = (TextView) findViewById(R.id.tv_grp_name);
        groupNameTextView.setText(group.getName());
        this.users = new ArrayList<>(group.getUsers());

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
        toActionsButton = (Button) findViewById(R.id.to_actions_button);
        toActionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent actions = new Intent(getApplicationContext(), ActionsActivity.class);
                actions.putExtra("group", group);
                actions.putExtra("groupLog", group.getGroupLog());
                startActivity(actions);
            }
        });
        Button syncButton = (Button) findViewById(R.id.sync_button);
        notifyUserChangedHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                notifyUserListChanged();
                super.handleMessage(msg);
            }
        };
        group.setParentActivityMessageHandler(notifyUserChangedHandler);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                group.syncUsers();
                group.getGroupLog().syncActions(getApplicationContext());
            }
        });
        this.group.syncUsers();
        group.getGroupLog().syncActions(getApplicationContext());
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

    private void inviteByMail(String emailAddress) {
        Uri.Builder uriBuilder = new Uri.Builder();
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


    public void notifyUserAdded(String name, String emailAddress) {
        if (!emailAddress.isEmpty()) {
            inviteByMail(emailAddress);
        }
        User newUser = new User(name, 0);
        newUser.setEmail(emailAddress);
        this.group.addUser(getApplicationContext(), newUser);
        users.clear();
        users.addAll(group.getUsers());
        userCheckBoxAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        clearChecked();
        if (requestCode == GO_OUT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Action action = (Action) data.getSerializableExtra("action");
                this.group.getGroupLog().addAction(getApplicationContext(), action);//todo: find a way to remove the context
                this.group.consumeAction(action);
                //users = resultList; //todo: problem if checked list was sent
                userCheckBoxAdapter.notifyDataSetChanged();
            }
        }
    }

    private void toastGen(Context context, String msg) {
        Log.w("user", "in toastGen: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }


    public void notifyUserListChanged() {
        userCheckBoxAdapter.notifyDataSetChanged();
    }

    private void clearChecked() {
        for (int i = 0; i < this.userListView.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) this.userListView.getChildAt(i).findViewById(R.id.cb_user_row);
            checkBox.setChecked(false);
        }
        this.userCheckBoxAdapter.clearChecked();
    }

    public void fastCheckoutCalculation(User user, double paid, double share) {

    }
    protected void onResume()
    {
        super.onResume();
        toastGen(this, "on resume called");
        notifyUserListChanged();
    }




}




