package share.fair.fairshare.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.ArrayList;
import java.util.List;

import share.fair.fairshare.Action;
import share.fair.fairshare.Alert;
import share.fair.fairshare.FairShareGroup;
import share.fair.fairshare.FairShareReceiver;
import share.fair.fairshare.R;
import share.fair.fairshare.User;
import share.fair.fairshare.UserCheckBoxAdapter;
import share.fair.fairshare.dialogs.AlertsDialog;
import share.fair.fairshare.dialogs.GroupKeyDialog;
import share.fair.fairshare.dialogs.GroupOptionsMenuDialog;
import share.fair.fairshare.dialogs.UserContextMenuDialog;
import share.fair.fairshare.dialogs.UserNameDialog;

public class GroupActivity extends FragmentActivity {

    public static final int NOTIFY_USER_CHANGE = 1;
    public  static final int CHECKED_AVAILABLE = 2;
    public   static final int CHECKED_UNAVAILABLE = 3;
    public static final int BALANCE_CHANGED = 4;
    public static final int GO_OUT_REQUEST = 1;  // The request code
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
    private ArrayList<Alert.AlertObject> alertObjects = new ArrayList<>();

    ShowcaseView showcaseView;
    Target targetNewBill;
    Target targetAddPerson;
    Target targetOptionsMenu;
    Target targetAlertsIcon;

    int counter=0;


    @Override
    protected void onPause() {
        super.onPause();
        ((App) (getApplication())).registerGroupActivity(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
        boolean isLegalVersion = settings.getBoolean("isLegalVersion", true);
        if(!isLegalVersion){
            Intent intent = new Intent(getApplicationContext(), OldVersionScreenActivity.class);
            startActivity(intent);
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
         isFirstRun();
        String groupId = getIntent().getStringExtra("groupId");
        if (groupId.isEmpty()) {
            //todo: handle problem;
        }
        this.group = FairShareGroup.loadGroupFromStorage(groupId);

        groupNameTextView = (TextView) findViewById(R.id.tv_grp_name);
        groupNameTextView.setText(group.getGroupName());
        this.users = new ArrayList<>(group.getUsers());
        userListView = (ListView) findViewById(R.id.users_list_view);
        userCheckBoxAdapter = new UserCheckBoxAdapter(getApplicationContext(),this, R.layout.user_check_row, this.users);

        userListView.setAdapter(userCheckBoxAdapter);
        // registerForContextMenu(userListView);

        addUserButton = (Button) findViewById(R.id.add_user_button);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserNameDialog dialog = new UserNameDialog();
                dialog.show(getSupportFragmentManager(), "add_new_user");

            }
        });

        alertButton = (Button) findViewById(R.id.group_activity_alert_button);
        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alertObjects.size() > 0) {
                    v.setBackgroundResource(R.drawable.popup_reminder);
                    ((Button) v).setText("");
                    AlertsDialog alertsDialog = new AlertsDialog();
                    int[] location = new int[2];
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
                GroupOptionsMenuDialog optionsMenuDialog = new GroupOptionsMenuDialog();
                int[] location = new int[2];
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
                ArrayList<GoOutFragment.GoOutObject> goOutObjectsList = new ArrayList<GoOutFragment.GoOutObject>();
                for (User user : users) {
                    goOutObjectsList.add(new GoOutFragment.GoOutObject(user.getUserId(), user.getName(), 0, 0));
                }
                Intent goOut = new Intent(getApplicationContext(), GoOutActivity.class);
                goOut.putExtra("goOutList", goOutObjectsList);
                goOut.putExtra("installationId",group.getInstallationId());
                startActivityForResult(goOut, GO_OUT_REQUEST);

            }
        });
        goOutCheckedButton = (Button) findViewById(R.id.bt_go_out_checked);
        goOutCheckedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<GoOutFragment.GoOutObject> checkedUsers = new ArrayList<GoOutFragment.GoOutObject>();
                for (User user : userCheckBoxAdapter.getCheckedArray()) {
                    checkedUsers.add(new GoOutFragment.GoOutObject(user.getUserId(), user.getName(), 0, 0));
                }

                if (checkedUsers.isEmpty()) {
                    //todo: other way to handle error?
                    toastGen(getApplicationContext(), "No user is checked!");
                    return;
                } else {
                    if (checkedUsers.size() == 1) {
                        toastGen(getApplicationContext(), "Only one person in the bill(Pointless). Have fun though");
                        return;
                    }

                    Intent goOut = new Intent(getApplicationContext(), GoOutActivity.class);
                    goOut.putExtra("goOutList", checkedUsers);
                    goOut.putExtra("installationId", group.getInstallationId());
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
        group.setGroupActivity(this);
        sync();
        notifyUserListChanged();
        this.userListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                UserContextMenuDialog dialog = new UserContextMenuDialog();
                dialog.setUser(users.get(position));
                dialog.show(getFragmentManager(), "UserContextMenuDialog");
                return true;
            }
        });
        
    }

   public void messageHandler(int messageNum, Object object){
       if (messageNum == NOTIFY_USER_CHANGE) {
           notifyUserListChanged();
       }
       if (messageNum == CHECKED_AVAILABLE) {
           goOutCheckedButton.setVisibility(View.VISIBLE);
           goOutAllButton.setVisibility(View.GONE);
       }
       if (messageNum == CHECKED_UNAVAILABLE) {
           goOutCheckedButton.setVisibility(View.GONE);
           goOutAllButton.setVisibility(View.VISIBLE);
       }
       if (messageNum == BALANCE_CHANGED) {
           Alert.AlertObject alert = (Alert.AlertObject) object;
           alertObjects.add(alert);
           alertButton.setBackgroundResource(R.drawable.popup_reminder_active);
           alertButton.setText(Integer.toString(alertObjects.size()));
           notifyUserListChanged();

       }
   }

    public void inviteByMail(String emailAddress) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("http");
        uriBuilder.authority("fair.share.fairshare");
        uriBuilder.appendPath("");
        uriBuilder.appendQueryParameter("groupName", group.getGroupName());
        uriBuilder.appendQueryParameter("groupCloudKey", group.getCloudGroupKey());


        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        i.putExtra(Intent.EXTRA_SUBJECT, "FairShare: invitation to join to a new group");
        i.putExtra(Intent.EXTRA_TEXT, "To join the group, click on the link below and choose to open it using FairShare app:\n\n" + uriBuilder.build().toString());

        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.w("custom", "cant start activity to send mail");
        }
    }

    public void goToActionActivity() {
        Intent actions = new Intent(getApplicationContext(), ActionsActivity.class);
        actions.putExtra("groupId", group.getCloudGroupKey());
        startActivity(actions);
        finish();
    }

    public void notifyUserAdded(String name) {
        User newUser = new User(name, 0);
        this.group.addUser(getApplicationContext(), newUser);
        notifyUserListChanged();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        clearChecked();
        if (requestCode == GO_OUT_REQUEST) {
            if (resultCode == RESULT_OK) {

                Action action = (Action) data.getSerializableExtra("action");
                if (action == null) {
                    Toast.makeText(getApplicationContext(), "Error: sum paid must be greater than sum share.\nPlease try again.", Toast.LENGTH_LONG).show();
                    return;
                }
                this.group.addAction(action);//todo: find a way to remove the context
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

    public boolean clearUserDebts(User user) {
        SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
        String creatorName = settings.getString("name", "");
        String creatorId = settings.getString("id", "");
        String description = user.getName() + "'s debts has been settled up";
        ArrayList<GoOutFragment.GoOutObject> goOutObjectsList = new ArrayList<>();
        double balance = user.getBalance();
        if (balance >= 0) {
            for (User currentUser : users) {
                double othersPaid = balance / (users.size() - 1); //user's positive balance is evenly split between all the other users.
                if (currentUser.getUserId().equals(user.getUserId())) {
                    goOutObjectsList.add(new GoOutFragment.GoOutObject(currentUser.getUserId(), currentUser.getName(), 0, balance));
                } else {
                    goOutObjectsList.add(new GoOutFragment.GoOutObject(currentUser.getUserId(), currentUser.getName(), othersPaid, 0));
                }
            }
        } else {
            for (User currentUser : users) {
                if (currentUser.getUserId().equals(user.getUserId())) {
                    goOutObjectsList.add(new GoOutFragment.GoOutObject(currentUser.getUserId(), currentUser.getName(), Math.abs(balance), 0));
                } else {
                    goOutObjectsList.add(new GoOutFragment.GoOutObject(currentUser.getUserId(), currentUser.getName(), 0, Double.NaN));
                }
            }
        }

        Action action = GoOutFragment.createAction(creatorName, creatorId, description, goOutObjectsList);
        if (action == null) {
            Toast.makeText(getApplicationContext(), "Error: can't settle up user's debts", Toast.LENGTH_LONG).show();
            return false;
        }
        this.group.addAction(action);
        this.group.consumeAction(action);
        userCheckBoxAdapter.notifyDataSetChanged();
        return true;

    }

    public void fastCheckoutCalculation(User user, double paid, double share) {
        SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
        String creatorName = settings.getString("name", "");
        String creatorId = settings.getString("id", "");
        String description = user.getName() + " paid for all";
        ArrayList<GoOutFragment.GoOutObject> goOutObjectsList = new ArrayList<>();
        for (User currentUser : users) {
            if (currentUser.getUserId().equals(user.getUserId())) {
                goOutObjectsList.add(new GoOutFragment.GoOutObject(currentUser.getUserId(), currentUser.getName(), paid, share));
            } else {
                goOutObjectsList.add(new GoOutFragment.GoOutObject(currentUser.getUserId(), currentUser.getName(), 0, Double.NaN));
            }
        }

        Action action = GoOutFragment.createAction(creatorName, creatorId, description, goOutObjectsList);
        if (action == null) {
            Toast.makeText(getApplicationContext(), "Error: sum paid must be greater than sum share.\nPlease try again.", Toast.LENGTH_LONG).show();
            return;
        }
        this.group.addAction( action);
        this.group.consumeAction(action);
        userCheckBoxAdapter.notifyDataSetChanged();

    }

    private void removeNotificationFromStatusBar() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancel(FairShareReceiver.NOTIFICATION_ID);
    }

    public void removeUser(final User user) {
        //todo: what to do when user removed from one group but not from other group and action has been made
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Wait!");
        alert.setMessage("Are you sure you want to remove " + user.getName() + " from the group?\n (user's debts within the group would automatically be settled up )");
        alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                if (clearUserDebts(user)) {
                    group.removeUser(getApplicationContext(), user);
                    List<Alert.NotifiedId> notifiedIds = (List<Alert.NotifiedId>) Alert.NotifiedId.listAll(Alert.NotifiedId.class);
                    for (Alert.NotifiedId notifiedId : notifiedIds) {
                        if (notifiedId.userId.equals(user.getUserId())) {
                            notifiedId.delete();
                            break;
                        }
                    }
                    notifyUserListChanged();
                }

            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.create().show();
    }

    public void settleUp() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Wait!");
        alert.setMessage("Are you sure you want to clear all debts?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
                String creatorName = settings.getString("name", "");
                String creatorId = settings.getString("id", "");
                String description = "Settle up";

                ArrayList<GoOutFragment.GoOutObject> goOutObjects = new ArrayList<>();
                for (User user : users) {
                    double balance = user.getBalance();
                    double sumPaid;
                    double sumShare;
                    if (balance >= 0) {
                        sumPaid = 0;
                        sumShare = balance;
                    } else {
                        sumPaid = Math.abs(balance);
                        ;
                        sumShare = 0;
                    }
                    goOutObjects.add(new GoOutFragment.GoOutObject(user.getUserId(), user.getName(), sumPaid, sumShare));
                }

                Action action = GoOutFragment.createAction(creatorName, creatorId, description, goOutObjects);
                if (action == null) {
                    Toast.makeText(getApplicationContext(), "Error: can't settle up", Toast.LENGTH_LONG).show();
                    return;
                }
                group.addAction(action);
                group.consumeAction(action);
                userCheckBoxAdapter.notifyDataSetChanged();

            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.create().show();
    }

    public void showGroupKeyDialog() {
        GroupKeyDialog dialog = new GroupKeyDialog();
        dialog.setGroupKey(group.getCloudGroupKey());
        dialog.setGroupName(group.getGroupName());
        dialog.show(getSupportFragmentManager(), "group_key");
    }

    protected void onResume() {
        super.onResume();
        ((App) (getApplication())).registerGroupActivity(this);
        removeNotificationFromStatusBar();
        notifyUserListChanged();
    }

    private void isFirstRun(){
        final SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
        boolean isFirstRun = settings.getBoolean("isFirstRunGroupActivity", true);
        if(isFirstRun){
            showTutorial();
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("isFirstRunGroupActivity", false);
            editor.commit();
        }
    }

    private void showTutorial(){
         targetNewBill= new ViewTarget(R.id.bt_go_out_all,this);
         targetAddPerson= new ViewTarget(R.id.add_user_button,this);
         targetOptionsMenu= new ViewTarget(R.id.group_activity_options_button,this);;;
         targetAlertsIcon = new ViewTarget(R.id.group_activity_alert_button,this);;

        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(Target.NONE).setContentTitle("Group's page").setContentText("This is the group's page. Here you can see all the users in the group and their balance.").setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (counter == 0) {
                            showcaseView.setShowcase(targetAddPerson, true);
                            showcaseView.setContentTitle("Add new person");
                            showcaseView.setContentText("Click here if you want to add new person to the group");
                        }

                        if (counter == 1) {
                            showcaseView.setShowcase(targetNewBill, true);
                            showcaseView.setContentTitle("New bill button");
                            showcaseView.setContentText("Click here if you want to add a new bill with all the persons in the group involved.\n If you want to make a bill with specific persons, just choose " +
                                    "all the persons you want before clicking the 'New Bill' button");
                        }

                        if (counter == 2) {
                            showcaseView.setShowcase(targetAlertsIcon, true);
                            showcaseView.setContentTitle("Alert Icon");
                            showcaseView.setContentText("You can choose to be notified on changes to the balance of a specific person in the group.\n In order to do so, click and hold a person's name and " +
                                    "choose 'Notify on balance change'.");
                        }

                        if (counter == 3) {
                            showcaseView.setShowcase(targetAlertsIcon, true);
                            showcaseView.setContentTitle("Alert Icon");
                            showcaseView.setContentText("If someone makes a payment that effect this user's balance, this icon will turn red and clicking on it will show you the changes that has been made.");
                        }
                        if (counter == 4) {
                            showcaseView.setShowcase(targetOptionsMenu, true);
                            showcaseView.setContentTitle("Options menu");
                            showcaseView.setContentText("From here you can access the payment history, settle up all group debts, invite someone to the group and more..");
                        }

                        if (counter == 5) {
                            showcaseView.setTarget(Target.NONE);
                            showcaseView.setContentTitle("Few more things..");
                            showcaseView.setContentText("User options menu: click and hold a persons name to show the user menu.\n" +
                                    "Ghosts: in rare occasions there can be an out-of-sync conflicts. For example, if someone remove a person from the list and at the same time you include this user in a bill. " +
                                    "In that case, the user will reappear as a ghost, indicating for you that there has been a conflict, and you will need to cancel that bill and re-delete the user.");
                        }

                        if (counter == 6) {
                            showcaseView.hide();
                        }
                        counter++;

                    }
                }).build();

        showcaseView.setStyle(R.style.ShowCaseCustomStyle);
        showcaseView.setButtonText("Next");
    }
    public void sync() {
        this.group.sync(getApplicationContext());
    }


}




