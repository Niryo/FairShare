package share.fair.fairshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class GoOutActivity extends Activity {

    Button backToGroup;
    Button calculateButton;
    ArrayList<User> nameList;
    EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_out);
        backToGroup = (Button) findViewById(R.id.back_to_group_button);
        backToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo: go back to the last screen(group screen)
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();

            }
        });
        nameList = (ArrayList<User>) getIntent().getSerializableExtra("goOutList");
        final ArrayList<View> viewsList = new ArrayList<>();
        LinearLayout list = (LinearLayout) findViewById(R.id.list_of_users);
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (User user : nameList) {
            View newView = vi.inflate(R.layout.user_go_out_row, null);
            ((TextView) newView.findViewById(R.id.tv_go_out_user_name)).setText(user.getName());
            String textBalance = Double.toString(user.getBalance());
            ((TextView) newView.findViewById(R.id.tv_go_out_user_balance)).setText(textBalance);
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
                SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
                String name = settings.getString("name", "");
                String id = settings.getString("id", "");
                String descriptionStr = description.getText().toString();
                Action action = new Action(name, id, descriptionStr);


                ArrayList<Integer> noShareUsersIndexes = new ArrayList<Integer>();

                for (int i = 0; i < nameList.size(); i++) {
                    double paidInput = 0.0;
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
                        action.addOperation(nameList.get(i).getUserId(), nameList.get(i).getName(), paidInput, shareInput);
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
                    double paidInput = 0.0;
                    if (!paidInputStr.isEmpty()) {
                        paidInput = Double.parseDouble(paidInputStr);
                    }
                    action.addOperation(nameList.get(index).getUserId(), nameList.get(index).getName(), paidInput, splitEvenShare);
                }
                for (User user : nameList) {
                    toastGen(getApplicationContext(), "usernameGo: " + user.getName() + " balGo: " + user.getBalance());
                }
                Intent returnIntent = new Intent();
                returnIntent.putExtra("action", action);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
        description = (EditText) findViewById(R.id.description);
    }

    private void toastGen(Context context, String msg) {
        Log.w("user", "in toastGen: " + msg);
//        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
