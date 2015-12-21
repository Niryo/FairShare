package share.fair.fairshare.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import share.fair.fairshare.activities.GroupActivity;
import share.fair.fairshare.R;

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
        return dialogLayout;
    }
}
