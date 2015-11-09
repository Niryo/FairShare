package share.fair.fairshare;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Created by Nir on 08/11/2015.
 */
public class GoOutFragment extends Fragment {

    ArrayList<GoOutFragment.GoOutObject> goOutObjectList;
    EditText description;
    boolean editMode=false;
    ArrayList<View> viewsList = new ArrayList<>();
    String billTitle;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.go_out_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LinearLayout list = (LinearLayout) getActivity().findViewById(R.id.list_of_users);
        description = (EditText) getActivity().findViewById(R.id.description);
        initLayoutPreferences();




        if(editMode){
            description.setText(billTitle);
            description.setEnabled(false);
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            for (GoOutObject goOutObject : goOutObjectList) {
                View newView = getNewGoOutRow(goOutObject.userName);
               EditText paid= (EditText)newView.findViewById(R.id.et_paid);
                if(goOutObject.paid>0){
                paid.setText(Double.toString(goOutObject.paid));
                }
                paid.setEnabled(false);

                EditText share= (EditText)newView.findViewById(R.id.et_special_share);
                if(!Double.isNaN(goOutObject.share)){
                share.setText(Double.toString(goOutObject.share));}
                share.setEnabled(false);

                list.addView(newView);
                viewsList.add(newView);
            }
        }
        else {
            for (GoOutObject goOutObject : goOutObjectList) {
                View newView = getNewGoOutRow(goOutObject.userName);
                list.addView(newView);
                viewsList.add(newView);
            }
        }
        initLayoutPreferences();

    }

public void enableEdit(){
    description.setEnabled(true);
    for(View view : viewsList){
       view.findViewById(R.id.et_paid).setEnabled(true);
      view.findViewById(R.id.et_special_share).setEnabled(true);
    }
    description.requestFocus();
}
    public Action calculate(){
        SharedPreferences settings = getActivity().getApplicationContext().getSharedPreferences("MAIN_PREFERENCES", 0);
        String name = settings.getString("name", "");
        String id = settings.getString("id", "");
        String descriptionStr = description.getText().toString();
        ArrayList<GoOutObject> goOutObjectList = new ArrayList<>();

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
            GoOutObject goOutObject = this.goOutObjectList.get(i);
            goOutObjectList.add(new GoOutObject(goOutObject.userId,goOutObject.userName, paidInput,shareInput));

        }

        Action action = createAction(name, id, descriptionStr, goOutObjectList);
        return action;
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
                action.addOperation(goOutObject.userId, goOutObject.userName, paidInput, shareInput , true);
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
            action.addOperation(noShareGoOutObject.userId, noShareGoOutObject.userName, paidInput, splitEvenShare,false);
        }
        return action;
    }

    private View getNewGoOutRow(String userName){
        int regularTextSizeFactor;

        int configuration = getResources().getConfiguration().orientation;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {

            regularTextSizeFactor=30;
        } else {

            regularTextSizeFactor=30;
        }
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newView = vi.inflate(R.layout.user_go_out_row, null);

        TextView userNameText= (TextView) newView.findViewById(R.id.tv_go_out_user_name);
        userNameText.setText(userName);
        userNameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / regularTextSizeFactor));

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

        double regularTextSizeFactor;

        int configuration = getResources().getConfiguration().orientation;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {

            regularTextSizeFactor=30;

        } else {
            regularTextSizeFactor=30;
        }
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;



        TextView sumPaidTitle = (TextView) getActivity().findViewById(R.id.sum_paid_title);
        sumPaidTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height/ regularTextSizeFactor));

        TextView userShareTitle = (TextView) getActivity().findViewById(R.id.user_share_title);
        userShareTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height/ regularTextSizeFactor));

        TextView userTitle = (TextView) getActivity().findViewById(R.id.go_out_activity_user_title);
        userTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height/ regularTextSizeFactor));
    }


    public static class GoOutObject implements Serializable {
        public String userId;
        public String userName;
        public double paid;
        public double share;

        public GoOutObject(String userId,String userName, double paid, double share) {
            this.userId = userId;
            this.userName= userName;
            this.paid = paid;
            this.share = share;
        }
    }
}
