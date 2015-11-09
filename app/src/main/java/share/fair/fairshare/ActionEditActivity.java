package share.fair.fairshare;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ActionEditActivity extends Activity {


    Button saveButton;
    Button backToActionsButton;
    Button deleteAction;
    Button editButton;
    FairShareGroup group;
    int actionIndex;
    private String creatorName;
    private String creatorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_edit);
        SharedPreferences settings = getApplicationContext().getSharedPreferences("MAIN_PREFERENCES", 0);
        final String creatorName = settings.getString("name", "");
        final String creatorId= settings.getString("id", "");

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
        group  = FairShareGroup.loadGroupFromStorage(groupId);
        //todo: put the contents of the operations in the boxes

        final Action action = group.getGroupLog().actions.get(actionIndex);
        final ArrayList<Operation> operationList = (ArrayList<Operation>) action.getOperations();
        final ArrayList<View> viewsList = new ArrayList<>();
        String actionDescription = action.getDescription();
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


        saveButton = (Button) findViewById(R.id.save_changes_action_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo: check if change is needed
                //1. create opposite action:
                ArrayList<Operation> oppositeOperationList = new ArrayList<Operation>();
                for (Operation oper : operationList) {
                    String oppositeId = oper.userId;
                    String oppositeUsername = oper.username;

                    double oppositePaid = -1 * oper.paid;
                    double oppositeShare = -1 * oper.share;
                    oppositeOperationList.add(new Operation(oppositeId, oppositeUsername, oppositePaid, oppositeShare, oper.getHasShare()));
                }

                Action oppositeAction = new Action(creatorName, creatorId, action.getDescription() + " (CANCELED(edit))");
                oppositeAction.setGroupLogId(group.getGroupLog().getId());
                oppositeAction.operations = oppositeOperationList;
                oppositeAction.setTimeStamp(action.getTimeStamp());

                //todo: make this action uneditable

                group.consumeAction(oppositeAction);
                group.getGroupLog().addAction(getApplicationContext(), oppositeAction);

                //2. create the new action

                Action editAction = goOutFragment.calculate();
                if (editAction == null) {
                    //todo: problem
                }
                group.consumeAction(editAction);
                group.getGroupLog().addAction(getApplicationContext(), editAction);
                finish();
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
        initLayoutPreferences();
    }


    private void toastGen(Context context, String msg) {
        Log.w("user", "in toastGen: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    private void initLayoutPreferences() {
        double backButtonFactor;
        double regularButtonSizeFactor;

        int configuration = getResources().getConfiguration().orientation;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            backButtonFactor=15;
            regularButtonSizeFactor=40;

        } else {
            backButtonFactor=15;
            regularButtonSizeFactor=40;
        }
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;


        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) backToActionsButton.getLayoutParams();
        params.width = (int)(height / backButtonFactor);
        params.height = (int) (height / backButtonFactor);
        backToActionsButton.setLayoutParams(params);

        saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / regularButtonSizeFactor));
        deleteAction.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / regularButtonSizeFactor));
        editButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / regularButtonSizeFactor));

    }
}

