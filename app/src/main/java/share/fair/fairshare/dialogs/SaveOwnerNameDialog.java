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

import share.fair.fairshare.R;

/**
 * Save owner name dialog.
 * This dialog shows up when the user runs the app for the first time, and asks him for his name.
 * The name is being saved to the sharedPreference object and will be used as the creator name of
 * any action that the user creates.
 */
public class SaveOwnerNameDialog extends DialogFragment {
    public SaveOwnerNameDialog() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogLayout = inflater.inflate(R.layout.dialog_save_name, container);
        getDialog().setContentView(R.layout.dialog_save_name);
        getDialog().setTitle("Enter your name:");
        setCancelable(false);
        final EditText etName = (EditText) dialogLayout.findViewById(R.id.save_name_et_name);
        final Button btnSave = (Button) dialogLayout.findViewById(R.id.save_name_btn_save);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save the name in the sharedPreferences object:
                SharedPreferences settings = getActivity().getSharedPreferences("MAIN_PREFERENCES", 0);
                SharedPreferences.Editor editor = settings.edit();
                String nameToSave = etName.getText().toString();
                editor.putString("name", nameToSave);
                editor.commit();
                getDialog().dismiss();
            }
        });
        btnSave.setEnabled(false);
        //make shure that the save button is enable only when the user entered some text:
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
                    btnSave.setEnabled(true);
                } else {
                    btnSave.setEnabled(false);
                }
            }
        });
        return dialogLayout;
    }
}
