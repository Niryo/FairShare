package share.fair.fairshare.activities;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;

import share.fair.fairshare.Action;
import share.fair.fairshare.R;


/**
 * The fragment that represents the actual bill and it's calculation
 */
public class BillFragment extends Fragment {

    ArrayList<BillLine> billLineInfoList; //all the information needed for the calculation
    EditText etDescription; //edit text for the description
    boolean editMode = false; //indicates if this a new bill or we are in edit mode of an old bill
    ArrayList<View> viewsList = new ArrayList<>(); //holds all the view lines of the bill
    String billTitle; //will hold the bill title if we are in edit mode

    /**
     * This methode calculate the bill and create the action that represent it.
     * For decoupling purposes, the calculation doesn't work directly on the input EditText, instead,
     * it receives all the necessary information as a list of billLineInfo.
     * This way we can do the bill calculation without using the EditText, for example using the
     * "Pay for all" option.
     *
     * @param creatorName      who created the bill
     * @param creatorId        the Id of the creator
     * @param description      short description
     * @param billLineInfoList all the bill lines wrap in a billLineInfo object.
     * @return an actions that represents the bill
     */
    public static Action calculateAndCreateAction(String creatorName, String creatorId, String description, ArrayList<BillLine> billLineInfoList) {
        double totalPaid = 0.0;
        double totalShare = 0.0;
        String descriptionStr = description;
        Action action = new Action(creatorName, creatorId, descriptionStr);
        ArrayList<BillLine> noShareUsers = new ArrayList<BillLine>(); //a list of users that have no share.

        //we start by calculating the total paid sum and the total share.
        //the share of the users without share will (totalPaid-totalShare)/numOfUserWithoutShare
        for (BillLine billLineInfo : billLineInfoList) {
            double paidInput = billLineInfo.paid;
            totalPaid += paidInput;

            double shareInput;
            shareInput = billLineInfo.share;
            if (Double.isNaN(shareInput)) {
                noShareUsers.add(billLineInfo);
            } else {
                totalShare += shareInput;
                //if user have share, we can calculate it's balance right away;
                action.addOperation(billLineInfo.userId, billLineInfo.userName, paidInput, shareInput, true);
            }
        }

        //now we split the share evenly between all the users without a share:
        double totalPaidWithoutShares = totalPaid - totalShare;
        //if there isn't amount money to pay the bill, there is a problem:
        if(totalPaid<totalShare){
            return null;
        }
        double splitEvenShare = 0.0;
        int numOfUsersWithoutShare = noShareUsers.size();
        if (numOfUsersWithoutShare > 0) {
            splitEvenShare = totalPaidWithoutShares / numOfUsersWithoutShare;
        }
        if (numOfUsersWithoutShare == 0 && totalPaidWithoutShares > 0) {
            //todo: problem - there is no one left to pay the debt!
            return null;
        }

        for (BillLine noShareBillLineInfo : noShareUsers) {
            double paidInput = noShareBillLineInfo.paid;
            action.addOperation(noShareBillLineInfo.userId, noShareBillLineInfo.userName, paidInput, splitEvenShare, false);
        }
        return action;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_bill, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LinearLayout list = (LinearLayout) getActivity().findViewById(R.id.new_bill_list_of_users);
        etDescription = (EditText) getActivity().findViewById(R.id.new_bill_et_description);

        //If we are in edit mode, we will set all the lines using the billLineInfoList
        if (editMode) {
            etDescription.setText(billTitle);
            etDescription.setEnabled(false);
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            for (BillLine billLineInfo : billLineInfoList) {
                View newView = getNewGoOutRow(billLineInfo.userName);
                EditText paid = (EditText) newView.findViewById(R.id.new_bill_row_et_paid);
                paid.setText(Double.toString(billLineInfo.paid));
                paid.setEnabled(false);

                EditText share = (EditText) newView.findViewById(R.id.new_bill_row_et_share);
                if (!Double.isNaN(billLineInfo.share)) {
                    share.setText(Double.toString(billLineInfo.share));
                }
                share.setEnabled(false);

                list.addView(newView);
                viewsList.add(newView);
            }
        } else { //this is a new bill:
            for (BillLine billLineInfo : billLineInfoList) {
                View newView = getNewGoOutRow(billLineInfo.userName);
                list.addView(newView);
                viewsList.add(newView);
            }
        }

    }

    /**
     * Make the bill editable
     */
    public void enableEdit() {
        etDescription.setEnabled(true);
        for (View view : viewsList) {
            view.findViewById(R.id.new_bill_row_et_paid).setEnabled(true);
            view.findViewById(R.id.new_bill_row_et_share).setEnabled(true);
        }
        etDescription.requestFocus();
    }

    /**
     * Creates the new bill
     *
     * @param creatorId the creator ID
     * @return an action that represents the bill
     */
    public Action createNewBill(String creatorId) {
        SharedPreferences settings = getActivity().getApplicationContext().getSharedPreferences("MAIN_PREFERENCES", 0);
        String name = settings.getString("name", "");
        String descriptionStr = etDescription.getText().toString();
        if (descriptionStr.isEmpty()) {
            descriptionStr = "...";
        }
        ArrayList<BillLine> billLineInfoList = new ArrayList<>();
        //we iterate over all bill lines and calculate the bill:
        for (int i = 0; i < viewsList.size(); i++) {
            //get amount paid:
            double paidInput = 0.0;
            String paidInputStr = ((EditText) viewsList.get(i).findViewById(R.id.new_bill_row_et_paid)).getText().toString();
            if (!paidInputStr.isEmpty()) {
                paidInput = Double.parseDouble(paidInputStr);
            }
            //get user's share:
            double shareInput = Double.NaN;
            String shareInputStr = ((EditText) viewsList.get(i).findViewById(R.id.new_bill_row_et_share)).getText().toString();
            if (!shareInputStr.isEmpty()) {
                shareInput = Double.parseDouble(shareInputStr);
            }
            BillLine billLineInfo = this.billLineInfoList.get(i);
            billLineInfo.paid = paidInput;
            billLineInfo.share = shareInput;
            billLineInfoList.add(billLineInfo);

        }

        Action action = calculateAndCreateAction(name, creatorId, descriptionStr, billLineInfoList);
        return action;
    }

    /**
     * Prepares the goOutRow
     *
     * @param userName User name
     * @return a goOutRow
     */
    private View getNewGoOutRow(String userName) {
        LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newView = vi.inflate(R.layout.row_new_bill, null);
        TextView tvUserNameText = (TextView) newView.findViewById(R.id.new_bill_row_tv_user_name);
        tvUserNameText.setText(userName);
        final EditText etPaid = (EditText) newView.findViewById(R.id.new_bill_row_et_paid);

        //we create a listener that verify that the input is a valid floating point number
        etPaid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //check that there is only one dot:
                String strToValidate = etPaid.getText().toString();

                //count how many dots are in the string:
                int dotCount = strToValidate.length() - strToValidate.replace(".", "").length();
                //if we have more then one dot or we have exactly one dot but no other digits,
                //we erase the last char:
                if (dotCount > 1 || (dotCount == 1 && strToValidate.length() == 1)) {
                    String newText = strToValidate.substring(0, strToValidate.length() - 1);
                    etPaid.setText(newText);
                    etPaid.setSelection(newText.length());
                }
            }

        });

        //the same validation on the paid is being done on the share:
        final EditText share = (EditText) newView.findViewById(R.id.new_bill_row_et_share);
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

        return newView;
    }


    /**
     * This class holds all the information of a bill line that is needed for the calculation of the
     * bill. A list of billLineInfo represents a complete bill.
     */
    public static class BillLine implements Serializable {
        public String userId;
        public String userName;
        public double paid;
        public double share;

        public BillLine(String userId, String userName, double paid, double share) {
            this.userId = userId;
            this.userName = userName;
            this.paid = paid;
            this.share = share;
        }
    }


}
