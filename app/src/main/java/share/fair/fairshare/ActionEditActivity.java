package share.fair.fairshare;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class ActionEditActivity extends AppCompatActivity {


    Button editActionButton;
    Button backToActionsButton;
    Button deleteAction;
    Group group;
    int actionIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_edit);
        actionIndex =(int) getIntent().getIntExtra("actionIndex",-1);
        toastGen(this,"Action index is: "+actionIndex); //debug
        if(actionIndex < 0){
            toastGen(this, "problem with action index"); //debug
            return;
        }

        group = (Group)getIntent().getSerializableExtra("group");
        //todo: put the contents of the operations in the boxes

        final ArrayList<Operation> operationList =(ArrayList<Operation>) group.getGroupLog().actions.get(actionIndex).getOperations();
        final ArrayList<View> viewsList = new ArrayList<>();
        TextView actionDescription = (TextView)findViewById(R.id.description_action);
        actionDescription.setText(group.getGroupLog().actions.get(actionIndex).getDescription());
        LinearLayout list= (LinearLayout) findViewById(R.id.list_of_action_users);
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(Operation oper: operationList){
            View newView = vi.inflate(R.layout.operation_row, null);
            toastGen(this, "username: "+oper.username);
            ((TextView)newView.findViewById(R.id.username_oper_row)).setText(oper.username);
            String textPaid = Double.toString(oper.paid);
            ((EditText)newView.findViewById(R.id.et_paid_oper)).setText(textPaid);
            String textShare = Double.toString(oper.share);
            ((EditText)newView.findViewById(R.id.et_share_oper)).setText(textShare);
            list.addView(newView);
            viewsList.add(newView);
        }


        editActionButton = (Button) findViewById(R.id.save_changes_action_button);
        editActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //todo: add an oppsite action, then create a new one
                 //do it by swapping share and paid, or by -paid and -share
                
                //todo: check if total paid = total share
                //gain a map of username : newOperBalance
                HashMap<String, Double> newOperationBalanceMap = new HashMap<String,Double>();
                double newTotalPaid = 0;
                double newTotalShare = 0;
                for(int j= 0; j < viewsList.size(); j++){
                    View row = viewsList.get(j);
                    String username = ((TextView)row.findViewById(R.id.username_oper_row)).getText().toString();
                    Double newPaid = Double.parseDouble(((EditText) row.findViewById(R.id.et_paid_oper)).getText().toString());
                    Double newShare = Double.parseDouble(((EditText) row.findViewById(R.id.et_share_oper)).getText().toString());
                    newOperationBalanceMap.put(username, newPaid - newShare);
                    

                }




            }
        });

        backToActionsButton = (Button) findViewById(R.id.back_to_actions);
        backToActionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent actions = new Intent(getApplicationContext(), ActionsActivity.class);
                startActivity(actions);
                finish();
            }
        });
        deleteAction = (Button) findViewById(R.id.delete_action_button);
        deleteAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
    private void toastGen(Context context,String msg){
        Log.w("user", "in toastGen: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
