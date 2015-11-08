package share.fair.fairshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.Serializable;
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

        final ArrayList<View> viewsList = new ArrayList<>();
        nameList = (ArrayList<User>) getIntent().getSerializableExtra("goOutList");
        LinearLayout list = (LinearLayout) findViewById(R.id.list_of_users);
        description = (EditText) findViewById(R.id.description);
        calculateButton = (Button) findViewById(R.id.calculate_button);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
                String name = settings.getString("name", "");
                String id = settings.getString("id", "");
                String descriptionStr = description.getText().toString();
                ArrayList<GoOutObject> goOutObjectList = new ArrayList<GoOutObject>();

                for (int i=0; i< viewsList.size(); i++) {
                    double paidInput = 0.0;
                    String paidInputStr = ((EditText) viewsList.get(i).findViewById(R.id.et_paid)).getText().toString();
                    if (!paidInputStr.isEmpty()) {
                        paidInput = Double.parseDouble(paidInputStr);
                    }
                    double shareInput=0.0;
                    String shareInputStr = ((EditText) viewsList.get(i).findViewById(R.id.et_special_share)).getText().toString();
                    if(!shareInputStr.isEmpty()) {
                        shareInput = Double.parseDouble(shareInputStr);
                    }
                    User user = nameList.get(i);
                    goOutObjectList.add(new GoOutObject(user, paidInput,shareInput));

                }

                Action action = createAction(name, id, descriptionStr, goOutObjectList);

                Intent returnIntent = new Intent();
                returnIntent.putExtra("action", action);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        if(getIntent().hasExtra("editMode")){
            //todo: put correct values in each row and put the description
        }
        else {

            for (User user : nameList) {
                View newView = getNewGoOutRow(user);
                list.addView(newView);
                viewsList.add(newView);
            }
        }
        initLayoutPreferences();
    }

    public static Action createAction(String creatorName, String creatorId, String description, ArrayList<GoOutObject> goOutObjectList){
        double totalPaid = 0.0;
        double totalShare = 0.0;
        String descriptionStr = description;
        Action action = new Action(creatorName, creatorId, descriptionStr);
        ArrayList<GoOutObject> noShareUsersIndexes = new ArrayList<GoOutObject>();

        for (GoOutObject goOutObject : goOutObjectList) {
            double paidInput = 0.0;
            paidInput = goOutObject.paid;
            totalPaid += paidInput;

            double shareInput;
            shareInput = goOutObject.share;
            if (shareInput == 0) {
                noShareUsersIndexes.add(goOutObject);
            } else {
                totalShare += shareInput;
                //if user have share, we can calculate it's balance right now;
                action.addOperation(goOutObject.user.getUserId(), goOutObject.user.getName(), paidInput, shareInput , true);
            }
        }
        double totalPaidWithoutShares = totalPaid - totalShare;
        if (totalPaidWithoutShares < 0) {
            //todo: Other solution for error(unable to press calculate while share is bigger than paid)
            return null;
        }
        double splitEvenShare = 0.0;
        int noShareUsers = noShareUsersIndexes.size();
        if (noShareUsers > 0) {
            splitEvenShare = totalPaidWithoutShares / noShareUsers;
        }
        for (GoOutObject noShareGoOutObject : noShareUsersIndexes) {
            double paidInput = noShareGoOutObject.paid;
            action.addOperation(noShareGoOutObject.user.getUserId(), noShareGoOutObject.user.getName(), paidInput, splitEvenShare,false);
        }
        return action;
    }

    private void toastGen(Context context, String msg) {
        Log.w("user", "in toastGen: " + msg);
//        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    private View getNewGoOutRow(User user){
        int regularTextSizeFactor;

        int configuration = getResources().getConfiguration().orientation;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {

            regularTextSizeFactor=30;
        } else {

            regularTextSizeFactor=30;
        }
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newView = vi.inflate(R.layout.user_go_out_row, null);

        TextView userName= (TextView) newView.findViewById(R.id.tv_go_out_user_name);
        userName.setText(user.getName());
        userName.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / regularTextSizeFactor));

        TextView userPaidPlaceHolder= (TextView) newView.findViewById(R.id.go_out_activity_user_paid_place_holder);
        userPaidPlaceHolder.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / regularTextSizeFactor));

        TextView userSharePlaceHolder= (TextView) newView.findViewById(R.id.go_out_activity_user_share_place_holder);
        userSharePlaceHolder.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / regularTextSizeFactor));

        EditText userPaidEditText = (EditText) newView.findViewById(R.id.et_paid);
        userPaidEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / regularTextSizeFactor));
        userPaidEditText.setWidth(userPaidPlaceHolder.getWidth());

        EditText userShareEditText = (EditText) newView.findViewById(R.id.et_special_share);
        userShareEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / regularTextSizeFactor));
        userShareEditText.setWidth(userSharePlaceHolder.getWidth());


        return newView;
    }
    private void initLayoutPreferences() {

        int headLineFactor;
        double backButtonFactor;
        double regularButtonSizeFactor;
        double regularTextSizeFactor;

        int screenSize;
        int configuration = getResources().getConfiguration().orientation;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            headLineFactor=10;
            backButtonFactor=15;
            regularButtonSizeFactor=40;
            regularTextSizeFactor=30;

        } else {
            headLineFactor=10;
            backButtonFactor=15;
            regularButtonSizeFactor=40;
            regularTextSizeFactor=30;
        }
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;


//        TextView headLine = (TextView) findViewById(R.id.new_bill_headline);
//        headLine.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height/headLineFactor));


        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) backToGroup.getLayoutParams();
        params.width = (int)(height / backButtonFactor);
        params.height = (int) (height / backButtonFactor);
        backToGroup.setLayoutParams(params);


        calculateButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / regularButtonSizeFactor));

//        TextView shortDescription = (TextView) findViewById(R.id.go_out_activity_short_desctiption);
//        shortDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height/ regularTextSizeFactor));

        TextView sumPaidTitle = (TextView) findViewById(R.id.sum_paid_title);
        sumPaidTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height/ regularTextSizeFactor));

        TextView userShareTitle = (TextView) findViewById(R.id.user_share_title);
        userShareTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height/ regularTextSizeFactor));

        TextView userTitle = (TextView) findViewById(R.id.go_out_activity_user_title);
        userTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height/ regularTextSizeFactor));
    }

         public static class GoOutObject implements Serializable{
             public User user;
             public double paid;
             public double share;

             public GoOutObject(User user, double paid, double share) {
                 this.user = user;
                 this.paid = paid;
                 this.share = share;
             }
         }

}
