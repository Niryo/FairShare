package share.fair.fairshare;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ActionsActivity extends AppCompatActivity {

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
                finish();
            }
        });

        actionList = (LinearLayout) findViewById(R.id.list_of_actions);
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ArrayList<View> viewsList = new ArrayList<>();
      Long groupId = getIntent().getLongExtra("groupId",-1);
        if(groupId==-1){
            //todo: problem
        }
        group= FairShareGroup.loadGroupFromStorage(groupId);
//        groupLog = (GroupLog) getIntent().getSerializableExtra("groupLog");

//        for(Action act : groupLog.actions){

        for (int i = 0; i < group.getGroupLog().actions.size(); i++) {
            View newView = vi.inflate(R.layout.action_row, null);
            ((TextView) newView.findViewById(R.id.date)).setText(getDate(group.getGroupLog().actions.get(i).getTimeStamp())); //todo: add date
            ((TextView) newView.findViewById(R.id.hour)).setText(getHour(group.getGroupLog().actions.get(i).getTimeStamp())); //todo: add hour
            ((TextView) newView.findViewById(R.id.description)).setText(group.getGroupLog().actions.get(i).getDescription()); //todo: add description
            newView.setTag(i);
            actionList.addView(newView);
            viewsList.add(newView);
        }
        for (View view : viewsList) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toastGen(getApplicationContext(), "clicked"); //debug
                    Intent editAction = new Intent(getApplicationContext(), ActionEditActivity.class);
                    editAction.putExtra("actionIndex", (int) v.getTag());
                    editAction.putExtra("groupId", group.getId());
                    startActivity(editAction);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actions, menu);
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

    private String getDate(long timeStamp) {

        try {
            DateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "date failed";
        }
    }

    private String getHour(long timeStamp) {

        try {
            DateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");
            Date netHour = (new Date(timeStamp));
            return sdf.format(netHour);
        } catch (Exception ex) {
            return "hour failed";
        }
    }

}
