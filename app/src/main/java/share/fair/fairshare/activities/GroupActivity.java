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


/**
 * Group activity
 */
public class GroupActivity extends FragmentActivity {

    public static final int NOTIFY_USER_CHANGE = 1;
    public static final int CHECKED_AVAILABLE = 2;
    public static final int CHECKED_UNAVAILABLE = 3;
    public static final int BALANCE_CHANGED = 4;
    public static final int GO_OUT_REQUEST = 1;  // The request code

    TextView tvGroupName;
    Button btnAddUser;
    ArrayList<User> users;
    ListView userListView;
    FairShareGroup group;
    UserCheckBoxAdapter userCheckBoxAdapter;
    Button btnNewBillWithAll;
    Button btnNewBillWithChecked;
    Button btnBackToMain;
    Button btnOptionsMenu;
    Button btnAlert;
    ShowcaseView showcaseView;
    Target targetNewBill;
    Target targetAddPerson;
    Target targetOptionsMenu;
    Target targetAlertsIcon;
    int showCaseCounter = 0;
    private ArrayList<Alert.AlertObject> alertObjects = new ArrayList<>();

    @Override
    protected void onPause() {
        super.onPause();
        ((App) (getApplication())).registerGroupActivity(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //version check:
        final SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
        boolean isLegalVersion = settings.getBoolean("isLegalVersion", true);
        if (!isLegalVersion) {
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

        tvGroupName = (TextView) findViewById(R.id.tv_group_group_name);
        tvGroupName.setText(group.getGroupName());
        this.users = new ArrayList<>(group.getUsers());
        userListView = (ListView) findViewById(R.id.users_list_view);
        userCheckBoxAdapter = new UserCheckBoxAdapter(getApplicationContext(), this, R.layout.row_user_checkbox_adapter, this.users);
        userListView.setAdapter(userCheckBoxAdapter);
        btnAddUser = (Button) findViewById(R.id.group_btn_add_user);
        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserNameDialog dialog = new UserNameDialog();
                dialog.show(getSupportFragmentManager(), "add_new_user");
            }
        });

        btnAlert = (Button) findViewById(R.id.group_btn_alert);
        btnAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alertObjects.size() > 0) { //check if there are new alerts
                    v.setBackgroundResource(R.drawable.img_alert_button_off); //set the icon back to normal
                    ((Button) v).setText("");
                    AlertsDialog alertsDialog = new AlertsDialog();
                    int[] location = new int[2];
                    v.getLocationOnScreen(location);
                    alertsDialog.setX(location[0]);
                    alertsDialog.setY(location[1]);
                    alertsDialog.setAlerts(new ArrayList<>(alertObjects));
                    alertsDialog.show(getSupportFragmentManager(), "add_new_user");
                    alertObjects.clear();

                }
            }
        });


        btnOptionsMenu = (Button) findViewById(R.id.group_btn_options_menu);
        btnOptionsMenu.setOnClickListener(new View.OnClickListener() {
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

        btnNewBillWithAll = (Button) findViewById(R.id.group_btn_new_bill_all);
        btnNewBillWithAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<BillFragment.BillLine> billLines = new ArrayList<>();
                for (User user : users) {
                    billLines.add(new BillFragment.BillLine(user.getUserId(), user.getUserName(), 0, 0));
                }
                Intent goOut = new Intent(getApplicationContext(), NewBillActivity.class);
                goOut.putExtra("goOutList", billLines);
                goOut.putExtra("installationId", group.getInstallationId());
                startActivityForResult(goOut, GO_OUT_REQUEST);

            }
        });

        btnNewBillWithChecked = (Button) findViewById(R.id.group_btn_new_bill_checked);
        btnNewBillWithChecked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<BillFragment.BillLine> checkedUsers = new ArrayList<BillFragment.BillLine>();
                for (User user : userCheckBoxAdapter.getCheckedArray()) {
                    checkedUsers.add(new BillFragment.BillLine(user.getUserId(), user.getUserName(), 0, 0));
                }

                if (checkedUsers.size() == 1) {
                    toastGen(getApplicationContext(), "Please choose at least one more person");
                    return;
                }

                Intent goOut = new Intent(getApplicationContext(), NewBillActivity.class);
                goOut.putExtra("goOutList", checkedUsers);
                goOut.putExtra("installationId", group.getInstallationId());
                startActivityForResult(goOut, GO_OUT_REQUEST);


            }
        });

        btnBackToMain = (Button) findViewById(R.id.group_btn_back);
        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        group.setGroupActivity(this);
        sync();
        // notifyUserListChanged();
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

    /**
     * Handle messages from background threads
     *
     * @param messageNum message number
     * @param object     an optional objects
     */
    public void messageHandler(int messageNum, Object object) {
        if (messageNum == NOTIFY_USER_CHANGE) {
            notifyUserListChanged();
        }
        if (messageNum == CHECKED_AVAILABLE) {
            btnNewBillWithChecked.setVisibility(View.VISIBLE);
            btnNewBillWithAll.setVisibility(View.GONE);
        }
        if (messageNum == CHECKED_UNAVAILABLE) {
            btnNewBillWithChecked.setVisibility(View.GONE);
            btnNewBillWithAll.setVisibility(View.VISIBLE);
        }
        if (messageNum == BALANCE_CHANGED) {
            Alert.AlertObject alert = (Alert.AlertObject) object;
            alertObjects.add(alert);
            btnAlert.setBackgroundResource(R.drawable.img_alert_button_on);
            btnAlert.setText(Integer.toString(alertObjects.size()));
            notifyUserListChanged();

        }
    }

    /**
     * Send group invitation by mail
     *
     * @param emailAddress
     */
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

    /**
     * Go to action activity
     */
    public void goToActionActivity() {
        Intent actions = new Intent(getApplicationContext(), PaymentsHistoryActivity.class);
        actions.putExtra("groupId", group.getCloudGroupKey());
        startActivity(actions);
        finish();
    }

    /**
     * Adds new user to the group
     *
     * @param name user name
     */
    public void addNewUser(String name) {
        User newUser = new User(name, null, 0, false);
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
                userCheckBoxAdapter.notifyDataSetChanged();
            }
        }
    }

    private void toastGen(Context context, String msg) {
        Log.w("user", "in toastGen: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Updates user list
     */
    public void notifyUserListChanged() {
        users.clear();
        users.addAll(group.getUsers());
        userCheckBoxAdapter.notifyDataSetChanged();
    }

    /**
     * Clear all checked users
     */
    private void clearChecked() {
        for (int i = 0; i < this.userListView.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) this.userListView.getChildAt(i).findViewById(R.id.user_checkbox_adapter_cb_user_name);
            checkBox.setChecked(false);
        }
        this.userCheckBoxAdapter.clearChecked();
        btnNewBillWithChecked.setVisibility(View.GONE);
        btnNewBillWithAll.setVisibility(View.VISIBLE);
    }

    /**
     * Clear the debts of the given user
     *
     * @param user user
     * @return true if we were able to clear the user's debts
     */
    public boolean clearUserDebts(User user) {
        SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
        String creatorName = settings.getString("name", "");
        String creatorId = group.getInstallationId();
        String description = user.getUserName() + "'s debts has been settled up";
        ArrayList<BillFragment.BillLine> billLines = new ArrayList<>();
        double balance = user.getBalance();

        if (balance >= 0) {
            for (User currentUser : users) {
                double othersPaid = balance / (users.size() - 1); //user's positive balance is evenly being split between all the other users.
                if (currentUser.getUserId().equals(user.getUserId())) {
                    billLines.add(new BillFragment.BillLine(currentUser.getUserId(), currentUser.getUserName(), 0, balance));
                } else {
                    billLines.add(new BillFragment.BillLine(currentUser.getUserId(), currentUser.getUserName(), othersPaid, 0));
                }
            }
        } else {
            for (User currentUser : users) {
                if (currentUser.getUserId().equals(user.getUserId())) {
                    billLines.add(new BillFragment.BillLine(currentUser.getUserId(), currentUser.getUserName(), Math.abs(balance), 0));
                } else {
                    billLines.add(new BillFragment.BillLine(currentUser.getUserId(), currentUser.getUserName(), 0, Double.NaN));
                }
            }
        }

        Action action = BillFragment.calculateAndCreateAction(creatorName, creatorId, description, billLines);
        if (action == null) {
            Toast.makeText(getApplicationContext(), "Error: can't settle up user's debts", Toast.LENGTH_LONG).show();
            return false;
        }
        this.group.addAction(action);
        this.group.consumeAction(action);
        userCheckBoxAdapter.notifyDataSetChanged();
        return true;

    }

    /**
     * This method is using for creating a fast bill, where one user pays for all the group
     *
     * @param user  the user that paid for all
     * @param paid  amount paid
     * @param share user's share
     */
    public void payForAll(User user, double paid, double share) {
        SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
        String creatorName = settings.getString("name", "");
        String creatorId = group.getInstallationId();
        String description = user.getUserName() + " paid for all";
        ArrayList<BillFragment.BillLine> goOutObjectsList = new ArrayList<>();
        for (User currentUser : users) {
            if (currentUser.getUserId().equals(user.getUserId())) {
                goOutObjectsList.add(new BillFragment.BillLine(currentUser.getUserId(), currentUser.getUserName(), paid, share));
            } else {
                goOutObjectsList.add(new BillFragment.BillLine(currentUser.getUserId(), currentUser.getUserName(), 0, Double.NaN));
            }
        }

        Action action = BillFragment.calculateAndCreateAction(creatorName, creatorId, description, goOutObjectsList);
        if (action == null) {
            Toast.makeText(getApplicationContext(), "Error: sum paid must be greater than sum share.\nPlease try again.", Toast.LENGTH_LONG).show();
            return;
        }
        this.group.addAction(action);
        this.group.consumeAction(action);
        userCheckBoxAdapter.notifyDataSetChanged();

    }

    /**
     * Remove any notification from the status bar
     */
    private void removeNotificationFromStatusBar() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancel(FairShareReceiver.NOTIFICATION_ID);
    }

    /**
     * Remove user from the group
     *
     * @param user the user to remove
     */
    public void removeUser(final User user) {
        //start by presenting a warning dialog:
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Wait!");
        alert.setMessage("Are you sure you want to remove " + user.getUserName() + " from the group?\n (user's debts within the group would automatically be settled up )");
        alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                if (clearUserDebts(user)) {
                    group.removeUser(getApplicationContext(), user);
                    //remove the user from the notified IDs, if exist:
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

    /**
     * Settles up all group debts
     */
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

                ArrayList<BillFragment.BillLine> billLines = new ArrayList<>();
                //adds for every user a line in the bill, that will settle up is debts
                for (User user : users) {
                    double balance = user.getBalance();
                    double sumPaid;
                    double sumShare;
                    if (balance >= 0) {
                        sumPaid = 0;
                        sumShare = balance;
                    } else {
                        sumPaid = Math.abs(balance);
                        sumShare = 0;
                    }
                    billLines.add(new BillFragment.BillLine(user.getUserId(), user.getUserName(), sumPaid, sumShare));
                }

                Action action = BillFragment.calculateAndCreateAction(creatorName, creatorId, description, billLines);
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

    /**
     * Open group key dialog
     */
    public void showGroupKeyDialog() {
        GroupKeyDialog dialog = new GroupKeyDialog();
        dialog.setGroupKey(group.getCloudGroupKey());
        dialog.setGroupName(group.getGroupName());
        dialog.show(getSupportFragmentManager(), "group_key");
    }

    protected void onResume() {
        super.onResume();
        //register the activity in the App activity:
        ((App) (getApplication())).registerGroupActivity(this);
        removeNotificationFromStatusBar();
        notifyUserListChanged();
    }

    /**
     * Check if this is the first run of the activity
     */
    private void isFirstRun() {
        final SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
        boolean isFirstRun = settings.getBoolean("isFirstRunGroupActivity", true);
        if (isFirstRun) {
            showTutorial();
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("isFirstRunGroupActivity", false);
            editor.commit();
        }
    }

    /**
     * Show tutorial
     */
    private void showTutorial() {
        targetNewBill = new ViewTarget(R.id.group_btn_new_bill_all, this);
        targetAddPerson = new ViewTarget(R.id.group_btn_add_user, this);
        targetOptionsMenu = new ViewTarget(R.id.group_btn_options_menu, this);
        ;
        ;
        targetAlertsIcon = new ViewTarget(R.id.group_btn_alert, this);
        ;

        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(Target.NONE).setContentTitle("Group's page").setContentText("This is the group's page. Here you can see all the users in the group and their balance.").setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (showCaseCounter == 0) {
                            showcaseView.setShowcase(targetAddPerson, true);
                            showcaseView.setContentTitle("Add new person");
                            showcaseView.setContentText("Click here if you want to add new person to the group");
                        }

                        if (showCaseCounter == 1) {
                            showcaseView.setShowcase(targetNewBill, true);
                            showcaseView.setContentTitle("New bill button");
                            showcaseView.setContentText("Click here if you want to add a new bill with all the persons in the group involved.\n If you want to make a bill with specific persons, just choose " +
                                    "all the persons you want before clicking the 'New Bill' button");
                        }

                        if (showCaseCounter == 2) {
                            showcaseView.setShowcase(targetAlertsIcon, true);
                            showcaseView.setContentTitle("Alert Icon");
                            showcaseView.setContentText("You can choose to be notified on changes to the balance of a specific person in the group.\n In order to do so, click and hold a person's name and " +
                                    "choose 'Notify on balance change'.");
                        }

                        if (showCaseCounter == 3) {
                            showcaseView.setShowcase(targetAlertsIcon, true);
                            showcaseView.setContentTitle("Alert Icon");
                            showcaseView.setContentText("If someone makes a payment that effect this user's balance, this icon will turn red and clicking on it will show you the changes that has been made.");
                        }
                        if (showCaseCounter == 4) {
                            showcaseView.setShowcase(targetOptionsMenu, true);
                            showcaseView.setContentTitle("Options menu");
                            showcaseView.setContentText("From here you can access the payment history, settle up all group debts, invite someone to the group and more..");
                        }

                        if (showCaseCounter == 5) {
                            showcaseView.setTarget(Target.NONE);
                            showcaseView.setContentTitle("Few more things..");
                            showcaseView.setContentText("User options menu: click and hold a persons name to show the user menu.\n" +
                                    "Ghosts: in rare occasions there can be an out-of-sync conflicts. For example, if someone remove a person from the list and at the same time you include this user in a bill. " +
                                    "In that case, the user will reappear as a ghost, indicating for you that there has been a conflict, and you will need to cancel that bill and re-delete the user.");
                        }

                        if (showCaseCounter == 6) {
                            showcaseView.hide();
                        }
                        showCaseCounter++;

                    }
                }).build();

        showcaseView.setStyle(R.style.ShowCaseCustomStyle);
        showcaseView.setButtonText("Next");
    }

    public void sync() {
        this.group.sync(getApplicationContext());
    }


}




