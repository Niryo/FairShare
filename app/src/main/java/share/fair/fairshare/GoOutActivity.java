package share.fair.fairshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class GoOutActivity extends Activity {

    Button backToGroup;
    Button calculateButton;
    ArrayList<User> nameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_out);
        backToGroup = (Button)findViewById(R.id.back_to_group_button);
        backToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo: go back to the last screen(group screen)
                Intent group = new Intent(getApplicationContext(), GroupActivity.class);
                setResult(RESULT_CANCELED);
                startActivity(group);

                finish();
            }
        });
        nameList = (ArrayList<User>)getIntent().getSerializableExtra("goOutList");
        final ArrayList<View> viewsList = new ArrayList<>();
        LinearLayout list= (LinearLayout) findViewById(R.id.list_of_users);
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(User user: nameList){
        View newView = vi.inflate(R.layout.user_go_out_row, null);
            ((TextView)newView.findViewById(R.id.tv_go_out_user_name)).setText(user.getName());
            String textBalance = Double.toString(user.getBalance());
            ((TextView)newView.findViewById(R.id.tv_go_out_user_balance)).setText(textBalance);
            list.addView(newView);
            viewsList.add(newView);
        }
        calculateButton = (Button) findViewById(R.id.calculate_button);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo: log: group name, name : paid - share
                double totalPaid = 0.0;
                double totalShare = 0.0;
                Action action= new Action();//todo: take description

                ArrayList<Integer>  noShareUsersIndexes= new ArrayList<Integer>();

                for (int i = 0; i < nameList.size(); i++) {
                    double paidInput=0.0;
                    String paidInputStr = ((EditText) (viewsList.get(i)).findViewById(R.id.et_paid)).getText().toString();
                    if (!paidInputStr.isEmpty()) {
                        paidInput = Double.parseDouble(paidInputStr);
                        totalPaid += paidInput;
                    }
                    double shareInput;
                    String shareInputStr = ((EditText) (viewsList.get(i)).findViewById(R.id.et_special_share)).getText().toString();
                    if (shareInputStr.isEmpty()) {
                        noShareUsersIndexes.add(i);
                    } else {
                        shareInput = Double.parseDouble(shareInputStr);
                        totalShare += shareInput;
                        //if user have share, we can calculate it's balance right now;
                        nameList.get(i).addToBalance(paidInput-shareInput); //todo: check this line
                        action.addOperation(nameList.get(i).getId(), paidInput - shareInput);
                    }
                }
                double totalPaidWithoutShares = totalPaid - totalShare;
                if (totalPaidWithoutShares < 0) {
                    //todo: Other solution for error(unable to press calculate while share is bigger than paid)
                    toastGen(getApplicationContext(), "Invalid input(Share sum is larger than paid)");
                    return;
                }
                double splitEvenShare = 0.0;
                int noShareUsers = noShareUsersIndexes.size();
                if (noShareUsers > 0) {
                    splitEvenShare = totalPaidWithoutShares / noShareUsers;
                }
                for (int index : noShareUsersIndexes) {
                    String paidInputStr = ((EditText) (viewsList.get(index)).findViewById(R.id.et_paid)).getText().toString();
                    double paidInput=0.0;
                    if (!paidInputStr.isEmpty()) {
                        paidInput = Double.parseDouble(paidInputStr);
                    }
                    nameList.get(index).addToBalance(paidInput - splitEvenShare);
                    action.addOperation(nameList.get(index).getId(), paidInput - splitEvenShare);
                }
                for(User user: nameList ){
                    toastGen(getApplicationContext(),"usernameGo: "+ user.getName()+" balGo: "+user.getBalance());
                }
                Intent returnIntent = new Intent();
                returnIntent.putExtra("resultUserList", nameList);
                returnIntent.putExtra("action", action);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }
    private void toastGen(Context context,String msg){
        Log.w("user", "in toastGen: " + msg);
//        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
