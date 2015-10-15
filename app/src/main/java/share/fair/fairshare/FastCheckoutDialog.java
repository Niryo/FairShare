package share.fair.fairshare;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class FastCheckoutDialog extends DialogFragment {

    private User user;
    private ArrayList<User> allUsers;
    private double paid;
    private double share;

    public void setAllUsers(ArrayList<User> allUsers) {
        this.allUsers = allUsers;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View dialogLayout = inflater.inflate(R.layout.activity_fast_checkcout_dialog, container);
        getDialog().setContentView(R.layout.activity_fast_checkcout_dialog);
        getDialog().setTitle(this.user.getName());
        final TextView title = (TextView) dialogLayout.findViewById(R.id.fast_checkout_title);
        title.setText("Enter paid amount:");
        final EditText input = (EditText) dialogLayout.findViewById(R.id.fast_checkout_input);
        input.requestFocus();
        final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        Button nextButton =  (Button) dialogLayout.findViewById(R.id.fast_checkout_next);
        final Button doneButton = (Button) dialogLayout.findViewById(R.id.fast_checkout_done);
        paid = 0.0;
        share = 0.0;
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paid = input.getText().toString().isEmpty()?  0.0: Double.parseDouble(input.getText().toString());
                title.setText("Enter user's share:");
                input.setText("");
                v.setVisibility(View.GONE);
                doneButton.setVisibility(View.VISIBLE);
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share = input.getText().toString().isEmpty()?  0.0: Double.parseDouble(input.getText().toString());
                imm.hideSoftInputFromWindow(getDialog().getCurrentFocus().getWindowToken(), 0);
                getDialog().dismiss();

            }
        });

        return dialogLayout;
    }

    private void calculate(){
        //todo: if share is bigger the paid, can't press calculate
        Action action =new Action("Fast checkout");
        user.addToBalance(paid-share);
        action.addOperation(user.getId(), paid-share);

    }
}
