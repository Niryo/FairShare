package share.fair.fairshare;




import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;



public class MainActivity extends FragmentActivity implements GroupNameDialog.GroupCreatedListener {


    ListView groupList;
    GroupsAdapter groupAdapter;
    ArrayList<NameAndKey> groupNames;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button createNewGroupButton = (Button) findViewById(R.id.create_new_group_button);
        createNewGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupNameDialog dialog = new GroupNameDialog();
                dialog.show(getSupportFragmentManager(), "add_new_group");
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
        if(getIntent()!=null){
            Intent intent= getIntent();
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uri = intent.getData();
                String groupName = uri.getQueryParameter("groupName");
                String groupCloudKey = uri.getQueryParameter("groupCloudKey");
                Group newGroup = new Group(groupName);
                newGroup.setCloudGroupKey(groupCloudKey);
                newGroup.saveGroupToStorage(getApplicationContext());
                notifyGroupCreated(groupName, newGroup.getLocalGroupKey());
                Log.w("custom", "group name: " + groupName);
//                Toast toast = Toast.makeText(getApplicationContext(), "group name: " + groupName, Toast.LENGTH_LONG);
//                toast.show();
            }
        }

    }

    @Override
    public void notifyGroupCreated(String name, String localGroupKey) {
        groupNames.add(new NameAndKey(name,localGroupKey));
        groupAdapter.notifyDataSetChanged();
    }
}
