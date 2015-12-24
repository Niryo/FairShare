package share.fair.fairshare.activities;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;



import java.io.Serializable;
import java.util.ArrayList;

import share.fair.fairshare.Action;
import share.fair.fairshare.R;


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



        if(editMode){
            description.setText(billTitle);
            description.setEnabled(false);
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            for (GoOutObject goOutObject : goOutObjectList) {
                View newView = getNewGoOutRow(goOutObject.userName);
               EditText paid= (EditText)newView.findViewById(R.id.et_paid);
                paid.setText(Double.toString(goOutObject.paid));

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

    }

public void enableEdit(){
    description.setEnabled(true);
    for(View view : viewsList){
       view.findViewById(R.id.et_paid).setEnabled(true);
      view.findViewById(R.id.et_special_share).setEnabled(true);
    }
    description.requestFocus();
}
    public Action calculate(String creatorId){
        SharedPreferences settings = getActivity().getApplicationContext().getSharedPreferences("MAIN_PREFERENCES", 0);
        String name = settings.getString("name", "");
        String descriptionStr = description.getText().toString();
        if(descriptionStr.isEmpty()){
            descriptionStr="...";
        }
        ArrayList<GoOutObject> goOutObjectList = new ArrayList<>();

        for (int i=0; i< viewsList.size(); i++) {
            double paidInput = 0.0;
            String paidInputStr = ((EditText) viewsList.get(i).findViewById(R.id.et_paid)).getText().toString();
            if (!paidInputStr.isEmpty()) {
                paidInput = Double.parseDouble(paidInputStr);
            }
            double shareInput=Double.NaN;
            String shareInputStr = ((EditText) viewsList.get(i).findViewById(R.id.et_special_share)).getText().toString();
            if(!shareInputStr.isEmpty()) {
                shareInput = Double.parseDouble(shareInputStr);
            }
            GoOutObject goOutObject = this.goOutObjectList.get(i);
            goOutObjectList.add(new GoOutObject(goOutObject.userId,goOutObject.userName, paidInput,shareInput));

        }

        Action action = createAction(name,creatorId, descriptionStr, goOutObjectList);
        return action;
    }

    public static Action createAction(String creatorName,String creatorId, String description, ArrayList<GoOutObject> goOutObjectList){
        double totalPaid = 0.0;
        double totalShare = 0.0;
        String descriptionStr = description;
        Action action = new Action(creatorName, creatorId, descriptionStr);
        ArrayList<GoOutObject> noShareUsersIndexes = new ArrayList<GoOutObject>();

        for (GoOutObject goOutObject : goOutObjectList) {
            double paidInput =  goOutObject.paid;
            totalPaid += paidInput;

            double shareInput;
            shareInput = goOutObject.share;
            if (Double.isNaN(shareInput)) {
                noShareUsersIndexes.add(goOutObject);
            } else {
                totalShare += shareInput;
                //if user have share, we can calculate it's balance right now;
                action.addOperation(goOutObject.userId, goOutObject.userName, paidInput, shareInput , true);
            }
        }
        double totalPaidWithoutShares = totalPaid - totalShare;

//        if (totalPaidWithoutShares < 0) {
//            //todo: Other solution for error(unable to press calculate while share is bigger than paid)
//            return null;
//        }
        double splitEvenShare = 0.0;
        int noShareUsers = noShareUsersIndexes.size();
        if (noShareUsers > 0) {
            splitEvenShare = totalPaidWithoutShares / noShareUsers;
        }
        if(noShareUsers == 0 && totalPaidWithoutShares>0){
            //todo: problem - there is no one left to pay the debt!
            return null;
        }

        for (GoOutObject noShareGoOutObject : noShareUsersIndexes) {
            double paidInput = noShareGoOutObject.paid;
            action.addOperation(noShareGoOutObject.userId, noShareGoOutObject.userName, paidInput, splitEvenShare,false);
        }
        return action;
    }

    private View getNewGoOutRow(String userName){
        LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newView = vi.inflate(R.layout.user_go_out_row, null);
        TextView userNameText= (TextView) newView.findViewById(R.id.tv_go_out_user_name);
        final EditText paid = (EditText) newView.findViewById(R.id.et_paid);
        paid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //check that there is only one dot:
                String strToValidate = paid.getText().toString();
                int count = strToValidate.length() - strToValidate.replace(".", "").length();
                if (count > 1|| (count==1 && strToValidate.length()==1 )) {
                    String newText = strToValidate.substring(0, strToValidate.length() - 1);
                    paid.setText(newText);
                    paid.setSelection(newText.length());
                }
            }

        });

        final EditText share = (EditText) newView.findViewById(R.id.et_special_share);
        share.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //check that there is only one dot:
                String strToValidate = share.getText().toString();
                int count = strToValidate.length() - strToValidate.replace(".", "").length();
                if (count > 1 || (count == 1 && strToValidate.length() == 1)) {
                    String newText = strToValidate.substring(0, strToValidate.length() - 1);
                    share.setText(newText);
                    share.setSelection(newText.length());
                }
            }

        });
        userNameText.setText(userName);
        return newView;
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
