package share.fair.fairshare.dialogs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import share.fair.fairshare.FairShareGroup;
import share.fair.fairshare.R;
import share.fair.fairshare.activities.MainActivity;

/**
 * New group dialog.
 * This dialog shows up when the user click on "create new group" in the main activity
 */


public class CreateNewGroupDialog extends DialogFragment {


    public CreateNewGroupDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View dialogLayout = inflater.inflate(R.layout.dialog_create_new_group, container);
        getDialog().setContentView(R.layout.dialog_create_new_group);
        getDialog().setTitle("Choose group name:");


        final EditText etName = (EditText) dialogLayout.findViewById(R.id.create_new_group_btn_create);
        final EditText specialUserNameText = (EditText) dialogLayout.findViewById(R.id.create_new_group_et_special_name);
        etName.setHint("Group's name");
        final Button btnCreate = (Button) dialogLayout.findViewById(R.id.create_new_group_btn_create);
        final Button btnCancel = (Button) dialogLayout.findViewById(R.id.create_new_group_btn_cancel);
        //sugest the default name:
        SharedPreferences settings = getActivity().getSharedPreferences("MAIN_PREFERENCES", 0);
        final String ownerName = settings.getString("name", "");
        specialUserNameText.setHint("Your name in the group: " + ownerName);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userNameInGroup = specialUserNameText.getText().toString();
                if (userNameInGroup.isEmpty()) {
                    userNameInGroup = ownerName;

                }

                String name = etName.getText().toString();
                FairShareGroup.groupBuilder(getContext(), name, userNameInGroup);
                //notify mainActivity:
                ((MainActivity) getActivity()).notifyGroupListChanged();
                getDialog().dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();

            }
        });
        btnCreate.setEnabled(false);
        //make the create button visible only if editText contains text:
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (etName.getText().toString().length() > 0) {
                    btnCreate.setEnabled(true);
                } else {
                    btnCreate.setEnabled(false);
                }
            }

        });
        return dialogLayout;
    }


}


