package share.fair.fairshare.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import share.fair.fairshare.R;
import share.fair.fairshare.activities.GroupActivity;

/**
 * Invite by mail dialog.
 * This dialog shows up when the user click the invite button in the group options menu
 */
public class InviteByMailDialog extends DialogFragment {
    public InviteByMailDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogLayout = inflater.inflate(R.layout.dialog_invite_by_mail, container);
        getDialog().setContentView(R.layout.dialog_invite_by_mail);
        getDialog().setTitle("Enter email address:");

        final EditText etInput = (EditText) dialogLayout.findViewById(R.id.invite_by_mail_dialog_et_input);
        final Button btnSend = (Button) dialogLayout.findViewById(R.id.invite_by_mail_btn_send);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((GroupActivity) getActivity()).inviteByMail(etInput.getText().toString());
                getDialog().dismiss();
            }
        });
        return dialogLayout;
    }
}
