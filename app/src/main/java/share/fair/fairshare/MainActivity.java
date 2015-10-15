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
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity implements GroupNameDialog.GroupCreatedListener {


    ListView groupList;
    GroupsAdapter groupAdapter;
    ArrayList<NameAndKey> groupNames;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        ParseObject testObject = new ParseObject("TestObject");
//        testObject.put("foo","bar");
//        try {
//            testObject.save();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        ParseObject testObject2 = new ParseObject("TestObject");
//        testObject2.put("foo", "bar2");
//        try {
//            testObject2.save();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

//        ParseQuery<ParseObject> testObject = ParseQuery.getQuery("TestObject");
//
//           testObject.findInBackground(new FindCallback<ParseObject>() {
//                @Override
//                public void done(List<ParseObject> list, ParseException e) {
//                    Log.w("custom", "test");
//                }
//            });


//        ParseObject banana = new ParseObject("banana");
//        banana.put("banana", "nir10");

//        testObject.getRelation("likes").add(new ParseObject("banana"));

//        ParseObject testRelation = new ParseObject("TestObject");
//       ParseObject wtf= testRelation.getParseObject("likes");
//        String test= wtf.getString("banana");
//
//        testObject.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                if (e != null) {
//                    e.printStackTrace();
//                    Toast.makeText(getApplicationContext(), "can't save to parse", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "saved!", Toast.LENGTH_LONG).show();
//                }
//            }
//        });

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
                String cloudLogKey = uri.getQueryParameter("cloudLogKey");
                Group newGroup =Group.joinGroupWithKey(getApplicationContext(), groupName,groupCloudKey,cloudLogKey);
                notifyGroupCreated(groupName, newGroup.getLocalGroupKey());
            }
        }

    }

    @Override
    public void notifyGroupCreated(String name, String localGroupKey) {
        groupNames.add(new NameAndKey(name,localGroupKey));
        groupAdapter.notifyDataSetChanged();
    }
}
