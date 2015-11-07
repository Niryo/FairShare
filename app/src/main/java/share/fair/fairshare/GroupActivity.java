package share.fair.fairshare;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends FragmentActivity {

    static final int NOTIFY_USER_CHANGE=1;
    static final int CHECKED_AVAILABLE=2;
    static final int CHECKED_UNAVAILABLE=3;
    static final int BALANCE_CHANGED=4;
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
    Button optionsButton;
    Button alertButton;
    private Handler messageHandler;
    private ArrayList<Alert.AlertObject> alertObjects=new ArrayList<>();


    @Override
    protected void onPause() {
        super.onPause();
        ((App) (getApplication())).registerGroupActivity(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        String groupId = getIntent().getStringExtra("groupId");
        if (groupId.isEmpty()) {
            //todo: handle problem;
        }

        this.group = FairShareGroup.loadGroupFromStorage(groupId);

        groupNameTextView = (TextView) findViewById(R.id.tv_grp_name);
        groupNameTextView.setText(group.getName());
        this.users = new ArrayList<>(group.getUsers());
        messageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what==NOTIFY_USER_CHANGE){
                notifyUserListChanged();}
                if(msg.what==CHECKED_AVAILABLE){
                    goOutCheckedButton.setVisibility(View.VISIBLE);
                    goOutAllButton.setVisibility(View.GONE);
                }
                if(msg.what==CHECKED_UNAVAILABLE){
                    goOutCheckedButton.setVisibility(View.GONE);
                    goOutAllButton.setVisibility(View.VISIBLE);
                }
                if(msg.what==BALANCE_CHANGED){
                    Alert.AlertObject alert =(Alert.AlertObject) msg.obj;
                    alertObjects.add(alert);
                    alertButton.setBackgroundResource(R.drawable.popup_reminder_active);
                    alertButton.setText(Integer.toString(alertObjects.size()));
                    notifyUserListChanged();

                }
            }
        };
        userListView = (ListView) findViewById(R.id.users_list_view);
        userCheckBoxAdapter = new UserCheckBoxAdapter(this, R.layout.user_check_row, this.users, messageHandler);
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

        alertButton = (Button) findViewById(R.id.group_activity_alert_button);
        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(alertObjects.size()>0) {
                    v.setBackgroundResource(R.drawable.popup_reminder);
                    ((Button)v).setText("");
                    AlertsDialog alertsDialog = new AlertsDialog();
                    int[] location= new int[2];
                    v.getLocationOnScreen(location);
                    alertsDialog.setX(location[0]);
                    alertsDialog.setY(location[1]);
                    alertsDialog.setAlerts(new ArrayList<Alert.AlertObject>(alertObjects));
                    alertsDialog.show(getSupportFragmentManager(), "add_new_user");
                    alertObjects.clear();

                }
            }
        });


        optionsButton = (Button) findViewById(R.id.group_activity_options_button);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupOptionsMenuDialog optionsMenuDialog =new GroupOptionsMenuDialog();
                int[] location= new int[2];
                v.getLocationOnScreen(location);
                optionsMenuDialog.setX(location[0]);
                optionsMenuDialog.setY(location[1] - v.getHeight());
                optionsMenuDialog.show(getFragmentManager(), "optionsMenueDialog");

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
                    if(checkedUsers.size() == 1){
                        toastGen(getApplicationContext(), "Only one person in the bill(Pointless). Have fun though");
                        return;
                    }
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
                finish();
            }
        });



        group.setParentActivityMessageHandler(messageHandler);
       syncActions();
        syncUsers();
        notifyUserListChanged();
        this.userListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                UserContextMenuDialog dialog = new UserContextMenuDialog();
                dialog.setUser(users.get(position));
                dialog.show(getFragmentManager(), "UserContextMenuDialog");
                return false;
            }
        });

        initLayoutPreferences();


    }

    private void setAlertButton(){

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

public void goToActionActivity(){
    Intent actions = new Intent(getApplicationContext(), ActionsActivity.class);
    actions.putExtra("groupId", group.getCloudGroupKey());
    startActivity(actions);
}
    public void notifyUserAdded(String name, String emailAddress) {
        if (!emailAddress.isEmpty()) {
            inviteByMail(emailAddress);
        }
        User newUser = new User(name, 0);
        newUser.setEmail(emailAddress);
        this.group.addUser(getApplicationContext(), newUser);
        notifyUserListChanged();

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
        users.clear();
        users.addAll(group.getUsers());
        userCheckBoxAdapter.notifyDataSetChanged();
    }

    private void clearChecked() {
        for (int i = 0; i < this.userListView.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) this.userListView.getChildAt(i).findViewById(R.id.cb_user_row);
            checkBox.setChecked(false);
        }
        this.userCheckBoxAdapter.clearChecked();
        goOutCheckedButton.setVisibility(View.GONE);
        goOutAllButton.setVisibility(View.VISIBLE);
    }

    public void fastCheckoutCalculation(User user, double paid, double share) {

    }

    private void removeNotificationFromStatusBar(){
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancel(FairShareReceiver.NOTIFICATION_ID);
    }
    public void removeUser(final User user){
        //todo: what to do when user removed from one group but not from other group and action has been made
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Wait!");
        alert.setMessage("Are you sure that you want to remove " +user.getName() + " from the group?");
        alert.setPositiveButton("Remove", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int whichButton)
            {
            group.removeUser(getApplicationContext(), user);
                List<Alert.NotifiedId> notifiedIds = (List<Alert.NotifiedId>) Alert.NotifiedId.listAll(Alert.NotifiedId.class);
                for(Alert.NotifiedId notifiedId : notifiedIds){
                    if(notifiedId.userId.equals(user.getUserId())){
                        notifiedId.delete();
                        break;
                    }
                }
            notifyUserListChanged();
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.create().show();
    }

    public void showGroupKeyDialog(){
        GroupKeyDialog dialog = new GroupKeyDialog();
        dialog.setGroupKey(group.getCloudGroupKey());
        dialog.show(getSupportFragmentManager(), "group_key");
    }
    protected void onResume()
    {
        super.onResume();
        ((App) (getApplication())).registerGroupActivity(this);
        removeNotificationFromStatusBar();
        notifyUserListChanged();
    }
    public void syncActions(){
        group.getGroupLog().syncActions(getApplicationContext());
    }
    public void syncUsers(){
        this.group.syncUsers(null);
    }

    private void initLayoutPreferences() {
        double syncButtonFactor;
        double groupNameFactor;
        double backButtonFactor;
        double regularButtonSizeFactor;
        double goOutCheckedFactor;
        double goOutAllFactor;
        double optionManuFactor;
        double alertButtonFactor;
        double alertButtonTextSizeFactor;


        int screenSize;
        int configuration = getResources().getConfiguration().orientation;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            syncButtonFactor=18;
            groupNameFactor=10;
            backButtonFactor=15;
            regularButtonSizeFactor=40;
            optionManuFactor=25;
            alertButtonFactor=10;
            alertButtonTextSizeFactor=50;
        } else {
             syncButtonFactor=18;
             groupNameFactor=10;
             backButtonFactor=15;
             regularButtonSizeFactor=40;
            optionManuFactor=10;
            alertButtonFactor=12;
            alertButtonTextSizeFactor=50;
        }
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;


        TextView groupName = (TextView) findViewById(R.id.tv_grp_name);
        groupName.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height/groupNameFactor));

        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) optionsButton.getLayoutParams();
        params2.width = (int)(height / optionManuFactor);
        params2.height = (int) (height / optionManuFactor);
        optionsButton.setLayoutParams(params2);


        RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams) backToMain.getLayoutParams();
        params3.width = (int)(height / backButtonFactor);
        params3.height = (int) (height / backButtonFactor);
        backToMain.setLayoutParams(params3);

        addUserButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / regularButtonSizeFactor));
        goOutAllButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height/regularButtonSizeFactor));
        goOutCheckedButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / regularButtonSizeFactor));

        RelativeLayout.LayoutParams params4 = (RelativeLayout.LayoutParams) alertButton.getLayoutParams();
        params4.width = (int)(height / alertButtonFactor);
        params4.height = (int) (height / alertButtonFactor);
        alertButton.setLayoutParams(params4);
        alertButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / alertButtonTextSizeFactor));
    }

}




