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

import java.util.ArrayList;

import share.fair.fairshare.Action;
import share.fair.fairshare.FairShareGroup;
import share.fair.fairshare.Operation;
import share.fair.fairshare.R;

/**
 * The edit action page
 */
public class ActionEditActivity extends Activity {


    Button btnSave; //save the action
    Button btnBackToActionsActivity;
    Button btnDeleteAction; //delete the action
    Button btnEdit; //make the action editable
    TextView tvCreatedBy; //shows who created the action
    FairShareGroup group;
    int actionIndex; //an inded to the action the is now being edited


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_edit);
        SharedPreferences settings = getApplicationContext().getSharedPreferences("MAIN_PREFERENCES", 0);
        final String creatorName = settings.getString("name", "");

        actionIndex = (int) getIntent().getIntExtra("actionIndex", -1);
        if (actionIndex < 0) {
            return;
        }

        String groupId = getIntent().getStringExtra("groupId");
        if (groupId.isEmpty()) {
            //todo: problem
        }

        group = FairShareGroup.loadGroupFromStorage(groupId);

        final Action currentAction = group.actions.get(actionIndex);
        String actionDescription = currentAction.getDescription();
        String actionCreatedBy = currentAction.getCreatorName();
        ArrayList<BillFragment.BillLine> goOutObjectsList = new ArrayList<>();

        //Create the goOutObject from the current action, to fill the fragment with the correct operations:
        final ArrayList<Operation> operationList = (ArrayList<Operation>) currentAction.getOperations();
        for (Operation oper : operationList) {
            double share = oper.getHasShare() ? oper.share : Double.NaN;
            goOutObjectsList.add(new BillFragment.BillLine(oper.getUserId(), oper.username, oper.paid, share));
        }
        //prepare the fragment:
        final BillFragment goOutFragment = new BillFragment();
        goOutFragment.billLineInfoList = goOutObjectsList;
        goOutFragment.editMode = true;
        goOutFragment.billTitle = actionDescription;
        final FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.action_edit_fragment_container, goOutFragment, "goOutFragment");
        ft.commit();

        tvCreatedBy = (TextView) findViewById(R.id.action_edit_created_by);
        tvCreatedBy.setText("Created by: " + actionCreatedBy);
        tvCreatedBy.setTextColor(Color.BLACK);

        btnSave = (Button) findViewById(R.id.save_changes_action_button);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1.first make the original action uneditable:
                currentAction.makeUneditable(true);
                currentAction.save();
                //2. construct an opposite action that will cancel the current action:
                ArrayList<Operation> oppositeOperationList = new ArrayList<>();
                for (Operation operation : operationList) {
                    String oppositeId = operation.userId;
                    String oppositeUsername = operation.username;

                    double oppositePaid = -1 * operation.paid;
                    double oppositeShare = -1 * operation.share;
                    oppositeOperationList.add(new Operation(oppositeId, oppositeUsername, oppositePaid, oppositeShare, operation.getHasShare()));
                }

                Action oppositeAction = new Action(creatorName, group.getInstallationId(), currentAction.getDescription() + " (CANCELED)");
                oppositeAction.setGroup(group.getCloudGroupKey(), group.getGroupName(), group.getInstallationId());
                oppositeAction.makeUneditable(false);
                oppositeAction.operations = oppositeOperationList;
                oppositeAction.save();


                group.consumeAction(oppositeAction);
                group.addAction(oppositeAction);

                //3. create the new action
                Action newAction = goOutFragment.createNewBill(group.getInstallationId());
                if (newAction == null) {
                    //todo: problem
                }

                group.consumeAction(newAction);
                group.addAction(newAction);
                openActionActivity();

            }
        });

        btnEdit = (Button) findViewById(R.id.edit_action_activity_edit_button);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goOutFragment.enableEdit();
                btnSave.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.GONE);
            }
        });

        btnBackToActionsActivity = (Button) findViewById(R.id.back_to_actions);
        btnBackToActionsActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActionActivity();
            }
        });

        btnDeleteAction = (Button) findViewById(R.id.delete_action_button);
        btnDeleteAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1.first make the original action uneditable:
                currentAction.makeUneditable(true);
                currentAction.save();
                //2. construct an opposite action that will cancel the current action:
                ArrayList<Operation> oppositeOperationList = new ArrayList<>();
                for (Operation oper : operationList) {
                    String oppositeId = oper.userId;
                    String oppositeUsername = oper.username;
                    double oppositePaid = -1 * oper.paid;
                    double oppositeShare = -1 * oper.share;
                    oppositeOperationList.add(new Operation(oppositeId, oppositeUsername, oppositePaid, oppositeShare, oper.getHasShare()));
                }
                Action oppositeAction = new Action(creatorName, group.getInstallationId(), currentAction.getDescription() + " (CANCELED)");
                oppositeAction.setGroup(group.getCloudGroupKey(), group.getGroupName(), group.getInstallationId());
                oppositeAction.makeUneditable(false);
                oppositeAction.operations = oppositeOperationList;
                group.consumeAction(oppositeAction);
                group.addAction(oppositeAction);
                openActionActivity();
            }
        });

        //if the current action cannot be edited, we will make all the buttons invisible:
        if (!currentAction.isEditable()) {
            btnEdit.setVisibility(View.INVISIBLE);
            btnDeleteAction.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Opens the action activity and finish this activity
     */
    private void openActionActivity() {
        Intent actions = new Intent(getApplicationContext(), ActionsActivity.class);
        actions.putExtra("groupId", group.getCloudGroupKey());
        startActivity(actions);
        finish();
    }
}

