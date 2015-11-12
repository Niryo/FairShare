package share.fair.fairshare;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends FragmentActivity implements GroupNameDialog.GroupCreatedListener {


    ListView groupList;
    GroupsAdapter groupAdapter;
    List<FairShareGroup.GroupNameRecord> groupNames;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initLayoutPreferences();
        SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
        String name = settings.getString("name", "");
        if (name.isEmpty()) {
            new SaveNameDialog().show(getSupportFragmentManager(), "save_name_dialog");
            ;
        }



        Button createNewGroupButton = (Button) findViewById(R.id.create_new_group_button);
        createNewGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GroupNameDialog().show(getSupportFragmentManager(), "add_new_group");
            }
        });

        Button optionsMenuButton= (Button) findViewById(R.id.activity_main_options_menu);
        optionsMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    MainOptionsMenuDialog optionsMenuDialog =new MainOptionsMenuDialog();
                    int[] location= new int[2];
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
        if (getIntent() != null) {
            Intent intent = getIntent();
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uri = intent.getData();
                String groupName = uri.getQueryParameter("groupName");
                String groupCloudKey = uri.getQueryParameter("groupCloudKey");
                String cloudLogKey = uri.getQueryParameter("cloudLogKey");
                FairShareGroup.joinGroupWithKey(getApplicationContext(), groupName, groupCloudKey, cloudLogKey);
                notifyGroupListChanged();
            }
        }
    }

    private void initLayoutPreferences() {
        double titleFactor;
        double buttonFactor;
        double optionManuFactor;
        int screenSize;
        int configuration = getResources().getConfiguration().orientation;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            titleFactor = 10;
            buttonFactor = 30;
            optionManuFactor=10;
        } else {
            titleFactor = 10;
            buttonFactor = 40;
            optionManuFactor=10;
        }
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        TextView textView = (TextView) findViewById(R.id.main_activity_title);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / titleFactor));

        Button newGroupButton = (Button) findViewById(R.id.create_new_group_button);
        newGroupButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / buttonFactor));

        Button optionsMenu = (Button) findViewById(R.id.activity_main_options_menu);
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) optionsMenu.getLayoutParams();
        params2.width = (int)(height / optionManuFactor);
        params2.height = (int) (height / optionManuFactor);
        optionsMenu.setLayoutParams(params2);

    }

    @Override
    public void notifyGroupListChanged() {
        groupNames.clear();
        groupNames.addAll(FairShareGroup.getSavedGroupNames());
        groupAdapter.notifyDataSetChanged();
    }

    public void removeGroup(final FairShareGroup.GroupNameRecord groupNameRecord){
        //todo: what to do when user removed from one group but not from other group and action has been made
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Wait!");
        alert.setMessage("Are you sure that you want to remove " + groupNameRecord.getGroupName()+ " from your groups?");
        alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                groupNameRecord.delete();
               FairShareGroup.loadGroupFromStorage(groupNameRecord.getGroupId()).delete();
              notifyGroupListChanged();
                Toast.makeText(getApplicationContext(), groupNameRecord.getGroupName()+" has been removed", Toast.LENGTH_SHORT).show();
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.create().show();
    }
}
