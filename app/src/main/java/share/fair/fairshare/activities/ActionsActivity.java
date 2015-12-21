package share.fair.fairshare.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import share.fair.fairshare.Action;
import share.fair.fairshare.FairShareGroup;
import share.fair.fairshare.R;
import share.fair.fairshare.activities.ActionEditActivity;

public class ActionsActivity extends Activity {

    Button backToGroup;
    LinearLayout actionList;
    FairShareGroup group;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actions);

        backToGroup = (Button) findViewById(R.id.back_to_group_button_actions);
        backToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGroup = new Intent(getApplicationContext(), GroupActivity.class);
                openGroup.putExtra("groupId", group.getCloudGroupKey());
                startActivity(openGroup);
                finish();
            }
        });

        actionList = (LinearLayout) findViewById(R.id.list_of_actions);
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      String groupId = getIntent().getStringExtra("groupId");
        if(groupId.isEmpty()){
            //todo: problem
        }
        group= FairShareGroup.loadGroupFromStorage(groupId);


        for (int i = group.getGroupLog().actions.size()-1; i >= 0; i--) {
            View actionRow= vi.inflate(R.layout.action_row, null);
            Action action =group.getGroupLog().actions.get(i);
            if(action.isEditable()){
                boolean isActionLegal= action.isLegal(group.getUsers());
                if(!isActionLegal){
                    action.makeUneditable();
                }
            }
            TextView time = (TextView) actionRow.findViewById(R.id.action_row_time);
            time.setText(getDate(action.getTimeStamp()));
            TextView description = (TextView) actionRow.findViewById(R.id.action_row_description);
            description.setText(action.getDescription());
            final int index = i;

            actionRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editAction = new Intent(getApplicationContext(), ActionEditActivity.class);
                    editAction.putExtra("actionIndex", index);
                    editAction.putExtra("groupId", group.getCloudGroupKey());
                    startActivity(editAction);
                    finish();
                }
            });
            if(!action.isEditable()){
                time.setTextColor(Color.GRAY);
                description.setTextColor(Color.GRAY);
            }


                 actionList.addView(actionRow);
        }

    }




    private String getDate(long timeStamp) {

        try {
            DateFormat sdf = new SimpleDateFormat("dd-MM-yy  HH:mm");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "date failed";
        }
    }




}