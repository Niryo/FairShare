package share.fair.fairshare;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by Nir on 18/10/2015.
 */
public class InviteByMailDialog extends DialogFragment {
    public InviteByMailDialog() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogLayout = inflater.inflate(R.layout.invite_by_mail_dialog, container);
        getDialog().setContentView(R.layout.invite_by_mail_dialog);
        getDialog().setTitle("Enter email address:");

        final EditText inputEditText = (EditText) dialogLayout.findViewById(R.id.invite_by_mail_dialog_input);
        final Button sendButton = (Button) dialogLayout.findViewById(R.id.invite_by_mail_send);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GroupActivity) getActivity()).inviteByMail(inputEditText.getText().toString());
                getDialog().dismiss();
            }
        });
        sendButton.setEnabled(false);
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
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }
        });
        return dialogLayout;
    }
}
