package share.fair.fairshare.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import share.fair.fairshare.Action;
import share.fair.fairshare.FairShareGroup;
import share.fair.fairshare.Operation;
import share.fair.fairshare.R;

public class ActionEditActivity extends Activity {


    Button saveButton;
    Button backToActionsButton;
    Button deleteAction;
    Button editButton;
    TextView createdBy;
    FairShareGroup group;
    int actionIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_edit);
        SharedPreferences settings = getApplicationContext().getSharedPreferences("MAIN_PREFERENCES", 0);
        final String creatorName = settings.getString("name", "");
        final String creatorId= settings.getString("id", "");

        actionIndex = (int) getIntent().getIntExtra("actionIndex", -1);
        if (actionIndex < 0) {
            return;
        }

        String groupId = getIntent().getStringExtra("groupId");
        if(groupId.isEmpty()){
            //todo: problem
        }
        group  = FairShareGroup.loadGroupFromStorage(groupId);
        //todo: put the contents of the operations in the boxes

        final Action action = group.getGroupLog().actions.get(actionIndex);
        final ArrayList<Operation> operationList = (ArrayList<Operation>) action.getOperations();
        String actionDescription = action.getDescription();
        String actionCreatedBy = action.getCreatorName();
        ArrayList<GoOutFragment.GoOutObject> goOutObjectsList= new ArrayList<>();

        for (Operation oper : operationList) {
            double share= oper.getHasShare()? oper.share: Double.NaN;
            goOutObjectsList.add(new GoOutFragment.GoOutObject(oper.getUserId(),oper.username,oper.paid, share));
        }

        final GoOutFragment goOutFragment= new GoOutFragment();
        goOutFragment.goOutObjectList = goOutObjectsList;
        goOutFragment.editMode=true;
        goOutFragment.billTitle = actionDescription;
        final FragmentManager fm = getFragmentManager();
        FragmentTransaction ft= fm.beginTransaction();
        ft.add(R.id.action_edit_fragment_container, goOutFragment, "goOutFragment");
        ft.commit();

         createdBy= (TextView) findViewById(R.id.action_edit_created_by);
        createdBy.setText("Created by: " + actionCreatedBy);
        createdBy.setTextColor(Color.BLACK);

        saveButton = (Button) findViewById(R.id.save_changes_action_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo: check if change is needed
                group.getGroupLog().makeActionUneditable(getApplicationContext(),action);
                action.save();
                //1. create opposite action:
                ArrayList<Operation> oppositeOperationList = new ArrayList<Operation>();
                for (Operation oper : operationList) {
                    String oppositeId = oper.userId;
                    String oppositeUsername = oper.username;

                    double oppositePaid = -1 * oper.paid;
                    double oppositeShare = -1 * oper.share;
                    oppositeOperationList.add(new Operation(oppositeId, oppositeUsername, oppositePaid, oppositeShare, oper.getHasShare()));
                }

                Action oppositeAction = new Action(creatorName, creatorId, action.getDescription() + " (CANCELED)");
                group.getGroupLog().makeActionUneditable(getApplicationContext(), oppositeAction);
                oppositeAction.save();
                oppositeAction.setGroupLogId(group.getGroupLog().getGroupLogId());
                oppositeAction.operations = oppositeOperationList;
                oppositeAction.setTimeStamp(action.getTimeStamp());


                group.consumeAction(oppositeAction);
                group.getGroupLog().addAction(getApplicationContext(), oppositeAction);

                //2. create the new action

                Action editAction = goOutFragment.calculate();
                if (editAction == null) {
                    //todo: problem
                }
                group.consumeAction(editAction);
                group.getGroupLog().addAction(getApplicationContext(), editAction);
                openActionActivity();

            }
        });

        editButton = (Button) findViewById(R.id.edit_action_activity_edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goOutFragment.enableEdit();
                saveButton.setVisibility(View.VISIBLE);
                editButton.setVisibility(View.GONE);
            }
        });

        backToActionsButton = (Button) findViewById(R.id.back_to_actions);
        backToActionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActionActivity();
            }
        });
        deleteAction = (Button) findViewById(R.id.delete_action_button);
        deleteAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 group.getGroupLog().makeActionUneditable(getApplicationContext(), action);
                action.save();
                //1. create opposite action:
                ArrayList<Operation> oppositeOperationList = new ArrayList<Operation>();
                for (Operation oper : operationList) {
                    String oppositeId = oper.userId;
                    String oppositeUsername = oper.username;
                    double oppositePaid = -1 * oper.paid;
                    double oppositeShare = -1 * oper.share;
                    oppositeOperationList.add(new Operation(oppositeId, oppositeUsername, oppositePaid, oppositeShare, oper.getHasShare()));
                }
                Action oppositeAction = new Action(creatorName, creatorId, action.getDescription() + " (CANCELED)");
                oppositeAction.makeUneditable();
                group.getGroupLog().makeActionUneditable(getApplicationContext(), oppositeAction);
                oppositeAction.setGroupLogId(group.getGroupLog().getGroupLogId());
                oppositeAction.operations = oppositeOperationList;
                oppositeAction.setTimeStamp(action.getTimeStamp());
                group.consumeAction(oppositeAction);
                group.getGroupLog().addAction(getApplicationContext(), oppositeAction);
                Toast.makeText(getApplicationContext(), "the action: " + action.getDescription() + "was successfully deleted.", Toast.LENGTH_LONG).show();
                openActionActivity();
            }
        });

        if(!action.isEditable()){
            editButton.setVisibility(View.INVISIBLE);
            deleteAction.setVisibility(View.INVISIBLE);
        }
    }




    private void openActionActivity(){
        Intent actions = new Intent(getApplicationContext(), ActionsActivity.class);
        actions.putExtra("groupId", group.getCloudGroupKey());
        startActivity(actions);
        finish();
    }
}

