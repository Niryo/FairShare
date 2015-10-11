package share.fair.fairshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;


public class MainActivity extends FragmentActivity implements AdapterView.OnItemClickListener {


    ListView groupList;

    GroupsAdapter groupAdapter;

    ArrayList<NameAndKey> groupNames;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button createNewGroupButton =(Button) findViewById(R.id.create_new_group_button);
        createNewGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterNameDialog dialog = new EnterNameDialog();
                dialog.setTitle("Choose group name:");
                dialog.setHint("Group's name");
                dialog.show(getSupportFragmentManager(), "add_new_group");

            }
        });
        groupNames = Group.getSavedGroupNames(getApplicationContext());
        groupNames.add(new NameAndKey("GROUPYAIR","123"));
        groupList = (ListView) findViewById(R.id.groups_list);
        groupAdapter = new GroupsAdapter(this,R.id.info, groupNames);
        groupList.setAdapter(groupAdapter);
        groupList.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent openGroup = new Intent(this, GroupActivity.class);
        startActivity(openGroup);
        finish();
    }



}
