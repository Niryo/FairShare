package share.fair.fairshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ActionEditActivity extends AppCompatActivity {


    Button editActionButton;
    Button backToActionsButton;
    Button deleteAction;
    FairShareGroup group;
    int actionIndex;
    private String creatorName;
    private String creatorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
        creatorName = settings.getString("name", "");
        creatorId = settings.getString("id", "");

        setContentView(R.layout.activity_action_edit);
        actionIndex = (int) getIntent().getIntExtra("actionIndex", -1);
        toastGen(this, "Action index is: " + actionIndex); //debug
        if (actionIndex < 0) {
            toastGen(this, "problem with action index"); //debug
            return;
        }

        String groupId = getIntent().getStringExtra("groupId");
        if(groupId.isEmpty()){
            //todo: problem
        }
        group= FairShareGroup.loadGroupFromStorage(groupId);
        //todo: put the contents of the operations in the boxes

        final Action action = group.getGroupLog().actions.get(actionIndex);
        final ArrayList<Operation> operationList = (ArrayList<Operation>) action.getOperations();
        final ArrayList<View> viewsList = new ArrayList<>();
        final TextView actionDescription = (TextView) findViewById(R.id.description_action);
        actionDescription.setText(action.getDescription());
        LinearLayout list = (LinearLayout) findViewById(R.id.list_of_action_users);
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (Operation oper : operationList) {
            View newView = vi.inflate(R.layout.operation_row, null);
            toastGen(this, "username: " + oper.username); //debug
            ((TextView) newView.findViewById(R.id.username_oper_row)).setText(oper.username);
            String textPaid = Double.toString(oper.getPaid());
            ((EditText) newView.findViewById(R.id.et_paid_oper)).setText(textPaid);
            String textShare = Double.toString(oper.share);
            if(oper.getHasShare()) {
                ((EditText) newView.findViewById(R.id.et_share_oper)).setText(textShare);
            } else{
                ((EditText) newView.findViewById(R.id.et_share_oper)).setHint(textShare);
            }
            newView.setTag(oper.userId);
            newView.setTag(R.string.for_tag_id,oper.getHasShare());
            list.addView(newView);
            viewsList.add(newView);
        }

        editActionButton = (Button) findViewById(R.id.save_changes_action_button);
        editActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //todo: check if change is needed

                //1. create opposite action:
                ArrayList<Operation> oppositeOperationList = new ArrayList<Operation>();
                for (Operation oper : operationList) {
                    String oppositeId = oper.userId;
                    String oppositeUsername = oper.username;

                    double oppositePaid = -1*oper.paid;
                    double oppositeShare = -1* oper.share;
                    oppositeOperationList.add(new Operation(oppositeId,oppositeUsername,oppositePaid,oppositeShare,oper.getHasShare()));
                }

                Action oppositeAction = new Action(creatorName, creatorId, action.getDescription() + " (CANCELED(edit))");
                oppositeAction.setGroupLogId(group.getGroupLog().getId());
                oppositeAction.operations = oppositeOperationList;
                oppositeAction.setTimeStamp(action.getTimeStamp());

                //todo: make this action uneditable

                group.consumeAction(oppositeAction);
                group.getGroupLog().addAction(getApplicationContext(), oppositeAction);

                //2. create the new action

                double totalPaid = 0.0;
                double totalShare = 0.0;
                SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
                String creatorName = settings.getString("name", "");
                String creatorId = settings.getString("id", "");
                Action editAction = new Action(creatorName, creatorId,action.getDescription()+"(Edited)" );
                ArrayList<Integer> noShareUsersIndexes = new ArrayList<Integer>();
                for(int i =0; i < viewsList.size(); i++){
                    String userId = viewsList.get(i).getTag().toString();
                    String userName = ((TextView)viewsList.get(i).findViewById(R.id.username_oper_row)).getText().toString();
                    String userPaidStr =((EditText)viewsList.get(i).findViewById(R.id.et_paid_oper)).getText().toString();
                    double userPaid = 0.0;
                    if(!userPaidStr.isEmpty()) {
                        userPaid = Double.parseDouble(userPaidStr);
                    }
                    totalPaid += userPaid;
                    double userShare;
                    String userShareStr = ((EditText)viewsList.get(i).findViewById(R.id.et_share_oper)).getText().toString();
                    if(!userShareStr.isEmpty()){
                        userShare = Double.parseDouble(userShareStr);
                        totalShare += userShare;
                        //can add the oper
                        editAction.addOperation(userId,userName,userPaid, userShare, true);
                    }else{
                        noShareUsersIndexes.add(i);
                    }
                }
                double totalPaidWithoutShares = totalPaid - totalShare;
                if (totalPaidWithoutShares < 0) {
                    //todo: Other solution for error(unable to press calculate while share is bigger than paid)
                    toastGen(getApplicationContext(), "Invalid input(Share sum is larger than paid)");
                    return;
                }
                if(noShareUsersIndexes.size() > 0) {
                    double splitEvenShare = totalPaidWithoutShares / noShareUsersIndexes.size();
                    for(int index:noShareUsersIndexes ){
                        String userId = viewsList.get(index).getTag().toString();
                        String userName = ((TextView)viewsList.get(index).findViewById(R.id.username_oper_row)).getText().toString();
                        String userPaidStr =((EditText)viewsList.get(index).findViewById(R.id.et_paid_oper)).getText().toString();
                        double userPaid = 0.0;
                        if(!userPaidStr.isEmpty()) {
                            userPaid = Double.parseDouble(userPaidStr);
                        }
                        editAction.addOperation(userId, userName, userPaid, splitEvenShare, false);
                    }
                }
                group.consumeAction(editAction);
                group.getGroupLog().addAction(getApplicationContext(),editAction);
                finish();
            }
        });

        backToActionsButton = (Button) findViewById(R.id.back_to_actions);
        backToActionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        deleteAction = (Button) findViewById(R.id.delete_action_button);
        deleteAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1. create opposite action:
                ArrayList<Operation> oppositeOperationList = new ArrayList<Operation>();
                for (Operation oper : operationList) {
                    String oppositeId = oper.userId;
                    String oppositeUsername = oper.username;
                    double oppositePaid = -1 * oper.paid;
                    double oppositeShare = -1 * oper.share;
                    oppositeOperationList.add(new Operation(oppositeId, oppositeUsername, oppositePaid, oppositeShare, oper.getHasShare()));
                }
                Action oppositeAction = new Action(creatorName, creatorId, action.getDescription() + "(CANCELED)");
                oppositeAction.setGroupLogId(group.getGroupLog().getId());
                oppositeAction.operations = oppositeOperationList;
                oppositeAction.setTimeStamp(action.getTimeStamp());
                group.consumeAction(oppositeAction);
                group.getGroupLog().addAction(getApplicationContext(),oppositeAction);
                toastGen(getApplicationContext(), "the action: " + action.getDescription() + "was successfully deleted.");
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_action_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toastGen(Context context, String msg) {
        Log.w("user", "in toastGen: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}

