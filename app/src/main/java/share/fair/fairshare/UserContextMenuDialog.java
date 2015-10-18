package share.fair.fairshare;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Nir on 16/10/2015.
 */
public class UserContextMenuDialog extends DialogFragment {
    private User user;
    private double paid;
    private double share;

    public UserContextMenuDialog() {
        // Empty constructor required for DialogFragment
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogLayout = inflater.inflate(R.layout.user_context_menu_dialog_layout, container);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getDialog().setTitle(user.getName());
        //==================================== First Item ==========================================
        final EditText inputEditText = (EditText) dialogLayout.findViewById(R.id.edit_text_user_context_menu);
        inputEditText.setHint("Enter amount paid");
        final Button doneButton = (Button) dialogLayout.findViewById(R.id.done_button_user_context_menu);
        final Button nextButton = (Button) dialogLayout.findViewById(R.id.next_button_user_context_menu);
        nextButton.setEnabled(false);
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (inputEditText.getText().toString().length() > 0) {
                    nextButton.setEnabled(true);
                } else {
                    nextButton.setEnabled(false);
                }
            }

        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                doneButton.setVisibility(View.VISIBLE);
                paid = inputEditText.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(inputEditText.getText().toString());
                inputEditText.setText("");
                inputEditText.setHint("Enter user's share");
            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share = inputEditText.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(inputEditText.getText().toString());
                InputMethodManager imm = (InputMethodManager) getDialog().getContext().getSystemService(getDialog().getContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getDialog().getCurrentFocus().getWindowToken(), 0);
                ((GroupActivity) getActivity()).fastCheckoutCalculation(user,paid,share);
                getDialog().dismiss();
            }
        });
        //==================================== Second Item ==========================================

        return dialogLayout;
    }
}
