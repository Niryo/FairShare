package share.fair.fairshare.activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.List;

import share.fair.fairshare.FairShareGroup;
import share.fair.fairshare.GroupsAdapter;
import share.fair.fairshare.R;
import share.fair.fairshare.dialogs.GroupContextMenuDialog;
import share.fair.fairshare.dialogs.GroupNameDialog;
import share.fair.fairshare.dialogs.MainOptionsMenuDialog;
import share.fair.fairshare.dialogs.SaveNameDialog;

/**
 * Main activity
 */
public class MainActivity extends FragmentActivity implements GroupNameDialog.GroupCreatedListener {

    ShowcaseView showcaseView;
    Target targetCreateButton;
    Target targetOptionsMenu;
    Button btnOptionsMenu;
    int showCaseCounter = 0;


    ListView groupList;
    GroupsAdapter groupAdapter;
    List<FairShareGroup.GroupNameRecord> groupNames; //all groups' names


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isFirstRun();
        //version check:
        SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
        boolean isLegalVersion = settings.getBoolean("isLegalVersion", true);
        if (!isLegalVersion) {
            Intent intent = new Intent(getApplicationContext(), OldVersionScreenActivity.class);
            startActivity(intent);
            finish();
        }


        Button btnCreateNewGroup = (Button) findViewById(R.id.create_new_group_button);
        btnCreateNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GroupNameDialog().show(getSupportFragmentManager(), "add_new_group");
            }
        });

        btnOptionsMenu = (Button) findViewById(R.id.activity_main_options_menu);
        btnOptionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainOptionsMenuDialog optionsMenuDialog = new MainOptionsMenuDialog();
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                optionsMenuDialog.setX(location[0]);
                optionsMenuDialog.setY(location[1] - v.getHeight());
                optionsMenuDialog.show(getSupportFragmentManager(), "mainOptionsMenueDialog");
            }
        });

        groupNames = FairShareGroup.getSavedGroupNames();
        groupList = (ListView) findViewById(R.id.groups_list);
        groupAdapter = new GroupsAdapter(this, R.id.group_row_container, groupNames);
        groupList.setAdapter(groupAdapter);
        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent openGroup = new Intent(getApplicationContext(), GroupActivity.class);
                openGroup.putExtra("groupId", groupNames.get(position).getGroupId());
                startActivity(openGroup);
            }
        });

        groupList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                GroupContextMenuDialog dialog = new GroupContextMenuDialog();
                dialog.setGroupNameRecord(groupNames.get(position));
                dialog.show(getFragmentManager(), "groupContextMenuDialog");
                return true;
            }
        });

        //try to add group from an email link:
        if (getIntent() != null) {
            Intent intent = getIntent();
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uri = intent.getData();
                String groupName = uri.getQueryParameter("groupName");
                String groupCloudKey = uri.getQueryParameter("groupCloudKey");
                FairShareGroup.joinGroupWithKey(getApplicationContext(), groupName, groupCloudKey);
                notifyGroupListChanged();
            }
        }
    }


    /**
     * Notify the group adapter about a change int the group list
     */
    @Override
    public void notifyGroupListChanged() {
        groupNames.clear();
        groupNames.addAll(FairShareGroup.getSavedGroupNames());
        groupAdapter.notifyDataSetChanged();
    }

    /**
     * Remove group
     *
     * @param groupNameRecord the group record that represents the group
     */
    public void removeGroup(final FairShareGroup.GroupNameRecord groupNameRecord) {
        //todo: what to do when user removed from one group but not from other group and action has been made
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Wait!");
        alert.setMessage("Are you sure you want to remove " + groupNameRecord.getGroupName() + " from your groups?");
        alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                groupNameRecord.delete();
                FairShareGroup.loadGroupFromStorage(groupNameRecord.getGroupId()).delete();
                notifyGroupListChanged();
                Toast.makeText(getApplicationContext(), groupNameRecord.getGroupName() + " has been removed", Toast.LENGTH_SHORT).show();
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.create().show();
    }

    /**
     * Check if this is the first run of the activity
     */
    private void isFirstRun() {
        final SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
        boolean isFirstRun = settings.getBoolean("isFirstRunMainActivity", true);
        if (isFirstRun) {
            showTutorial();
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("isFirstRunMainActivity", false);
            editor.commit();
        }
    }

    /**
     * Shows the tutorial
     */
    private void showTutorial() {
        targetCreateButton = new ViewTarget(R.id.create_new_group_button, this);
        targetOptionsMenu = new ViewTarget(R.id.activity_main_options_menu, this);
        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(Target.NONE).setContentTitle("Welcome to FaireShare!").setContentText("The best app for keeping track of group expenses!").setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (showCaseCounter == 0) {
                            showcaseView.setShowcase(targetCreateButton, true);
                            showcaseView.setContentTitle("Create new group button");
                            showcaseView.setContentText("Click here if you want to create a new group");
                        }

                        if (showCaseCounter == 1) {
                            showcaseView.setShowcase(targetOptionsMenu, true);
                            showcaseView.setContentTitle("Options menu");
                            showcaseView.setContentText("This is the options menu button.\n from here you can join to an existing group if you have its key.");
                        }


                        if (showCaseCounter == 2) {
                            showcaseView.hide();
                            SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
                            String name = settings.getString("name", "");
                            if (name.isEmpty()) {
                                new SaveNameDialog().show(getSupportFragmentManager(), "save_name_dialog");

                            }
                        }

                        showCaseCounter++;

                    }
                }).build();

        showcaseView.setStyle(R.style.ShowCaseCustomStyle);
        showcaseView.setButtonText("Next");
    }

}
