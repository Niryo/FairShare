package share.fair.fairshare;


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

/**
 * Created by Nir on 11/11/2015.
 */
public class JoinGroupWithKeyDialog extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogLayout = inflater.inflate(R.layout.join_group_with_key_dialog, container);
        getDialog().setContentView(R.layout.join_group_with_key_dialog);
        getDialog().setTitle("Enter Group key:");

        final EditText groupKeyEditText = (EditText) dialogLayout.findViewById(R.id.join_group_edit_text);
        final Button joinButton = (Button) dialogLayout.findViewById(R.id.join_group_with_key_join_button);

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rawKey = groupKeyEditText.getText().toString();
                //todo: check correct string format
                int groupNameLength= Integer.parseInt(rawKey.substring(0, 2));

                String groupName=rawKey.substring(2, groupNameLength+2);
                String groupKey= rawKey.substring(groupNameLength+2,groupNameLength+29);
                String groupLogKey= rawKey.substring(groupNameLength+29);
                FairShareGroup.joinGroupWithKey(getContext(),groupName,groupKey,groupLogKey);
                ((MainActivity)getActivity()).notifyGroupListChanged();
                getDialog().dismiss();
            }
        });
        joinButton.setEnabled(false);
        groupKeyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (groupKeyEditText.getText().toString().length() > 0) {
                    joinButton.setEnabled(true);
                } else {
                    joinButton.setEnabled(false);
                }
            }
        });
        return dialogLayout;
    }
}
