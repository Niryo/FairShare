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
 * Created by Ori on 10/23/2015.
 */


//this class is useless until chosen otherwise
public class UserNameForGroupDialog extends DialogFragment {

    private FairShareGroup group;

    public UserNameForGroupDialog() {
        // Empty constructor required for DialogFragment
    }

    public void setGroup(FairShareGroup group) {
        this.group = group;
    }


    //todo: change it all
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogLayout = inflater.inflate(R.layout.user_name_for_group_dialog_layout, container);
        getDialog().setContentView(R.layout.user_name_for_group_dialog_layout);
        getDialog().setTitle("Choose your name for this group:");
        setCancelable(false);
        final EditText nameEditText = (EditText) dialogLayout.findViewById(R.id.special_name_edit_text);
        final Button saveButton = (Button) dialogLayout.findViewById(R.id.special_name_save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                if(name.isEmpty()){
                    SharedPreferences settings = getActivity().getSharedPreferences("MAIN_PREFERENCES", 0);
                    name = settings.getString("name","default_name_debug");
                }

                //todo: add user named: name to group

                getDialog().dismiss();
            }
        });


        return dialogLayout;
    }


}
