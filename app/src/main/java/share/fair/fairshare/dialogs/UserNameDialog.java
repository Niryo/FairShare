package share.fair.fairshare.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import share.fair.fairshare.activities.GroupActivity;
import share.fair.fairshare.R;

/**
 * Created by Ori on 10/11/2015.
 */


public class UserNameDialog extends DialogFragment {


    public UserNameDialog() {
        // Empty constructor required for DialogFragment
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View dialogLayout = inflater.inflate(R.layout.new_user_dialog_layout, container);
        getDialog().setContentView(R.layout.new_user_dialog_layout);
        getDialog().setTitle("Choose user name:");

        final EditText nameEditText = (EditText) dialogLayout.findViewById(R.id.user_name_edit_text);
        nameEditText.setHint("User's name");
        final Button createButton = (Button) dialogLayout.findViewById(R.id.create_button_user);
        final Button cancelButton = (Button) dialogLayout.findViewById(R.id.cancel_button_user);
        final EditText emailEditText = (EditText) dialogLayout.findViewById(R.id.user_mail_edit_text);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                ((GroupActivity) getActivity()).addNewUser(name);
                if (!email.isEmpty()) {
                    ((GroupActivity) getActivity()).inviteByMail(email);
                }
                getDialog().dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();

            }
        });
        createButton.setEnabled(false);
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (nameEditText.getText().toString().length() > 0) {
                    createButton.setEnabled(true);
                } else {
                    createButton.setEnabled(false);
                }
            }

        });
        return dialogLayout;
    }


}


