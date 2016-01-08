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
import android.widget.Toast;

import share.fair.fairshare.FairShareGroup;
import share.fair.fairshare.activities.MainActivity;
import share.fair.fairshare.R;

/**
 *  Join group with key dialog.
 *  This dialog shows up when the user click on "Join group with key" inside the groups options menu.
 */
public class JoinGroupWithKeyDialog extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogLayout = inflater.inflate(R.layout.dialog_join_group_with_key, container);
        getDialog().setContentView(R.layout.dialog_join_group_with_key);
        getDialog().setTitle("Enter Group key:");

        final EditText etGroupKey = (EditText) dialogLayout.findViewById(R.id.join_group_with_key_et_input);
        final Button btnJoin = (Button) dialogLayout.findViewById(R.id.join_group_with_key_btn_join);

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rawKey = etGroupKey.getText().toString();
                if (rawKey.length() < 2) { //key must be at least the size of 2, because the length of the key is the first to chars
                    Toast.makeText(getContext(), "Key error: cannot join group", Toast.LENGTH_LONG).show();
                    return;
                }
                int groupNameLength = 0;
                try {
                    groupNameLength = Integer.parseInt(rawKey.substring(0, 2));
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Key error: cannot join group", Toast.LENGTH_LONG).show();
                    return;
                }

                if (groupNameLength + 29 != rawKey.length()) { //group key must be 2 (first to chars)+ length of groups name+ length of key(27 chars)
                    Toast.makeText(getContext(), "Key error: cannot join group", Toast.LENGTH_LONG).show();
                    return;
                }
                String groupName = rawKey.substring(2, groupNameLength + 2);
                String groupKey = rawKey.substring(groupNameLength + 2);
                FairShareGroup.joinGroupWithKey(getContext(), groupName, groupKey);
                ((MainActivity) getActivity()).notifyGroupListChanged();
                getDialog().dismiss();
            }
        });
        btnJoin.setEnabled(false);
        etGroupKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (etGroupKey.getText().toString().length() > 0) {
                    btnJoin.setEnabled(true);
                } else {
                    btnJoin.setEnabled(false);
                }
            }
        });
        return dialogLayout;
    }
}
