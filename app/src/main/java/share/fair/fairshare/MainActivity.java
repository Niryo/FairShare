package share.fair.fairshare;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.parse.ParseObject;

import java.util.ArrayList;


public class MainActivity extends FragmentActivity implements GroupNameDialog.GroupCreatedListener {


    ListView groupList;
    GroupsAdapter groupAdapter;
    ArrayList<NameAndKey> groupNames;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
        String name = settings.getString("name", "");
        if(name.isEmpty()){
              new SaveNameDialog().show(getSupportFragmentManager(), "save_name_dialog");;
        }

        Button createNewGroupButton = (Button) findViewById(R.id.create_new_group_button);
        createNewGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GroupNameDialog().show(getSupportFragmentManager(), "add_new_group");
            }
        });
        groupNames = Group.getSavedGroupNames(getApplicationContext());
        groupList = (ListView) findViewById(R.id.groups_list);
        groupAdapter = new GroupsAdapter(this, R.id.info, groupNames);
        groupList.setAdapter(groupAdapter);
        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent openGroup = new Intent(getApplicationContext(), GroupActivity.class);
                openGroup.putExtra("group_key", groupNames.get(position).getKey());
                startActivity(openGroup);
                finish();
            }
        });
        if (getIntent() != null) {
            Intent intent = getIntent();
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uri = intent.getData();
                String groupName = uri.getQueryParameter("groupName");
                String groupCloudKey = uri.getQueryParameter("groupCloudKey");
                String cloudLogKey = uri.getQueryParameter("cloudLogKey");
                Group newGroup = Group.joinGroupWithKey(getApplicationContext(), groupName, groupCloudKey, cloudLogKey);
                notifyGroupCreated(groupName, newGroup.getLocalGroupKey());
            }
        }

    }

    @Override
    public void notifyGroupCreated(String name, String localGroupKey) {
        groupNames.add(new NameAndKey(name, localGroupKey));
        groupAdapter.notifyDataSetChanged();
    }
}
