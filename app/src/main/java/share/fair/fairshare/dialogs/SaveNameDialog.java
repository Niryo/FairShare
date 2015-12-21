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

import java.math.BigInteger;
import java.security.SecureRandom;

import share.fair.fairshare.R;

/**
 * Created by Nir on 18/10/2015.
 */
public class SaveNameDialog extends DialogFragment {
    public SaveNameDialog() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogLayout = inflater.inflate(R.layout.save_name_dialog, container);
        getDialog().setContentView(R.layout.save_name_dialog);
        getDialog().setTitle("Enter your name:");
        setCancelable(false);
        final EditText nameEditText = (EditText) dialogLayout.findViewById(R.id.name_edit_text);
        final Button saveButton = (Button) dialogLayout.findViewById(R.id.name_save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getActivity().getSharedPreferences("MAIN_PREFERENCES", 0);
                SharedPreferences.Editor editor = settings.edit();
                String nameToSave = nameEditText.getText().toString();
                editor.putString("name", nameToSave);
                editor.putString("id", new BigInteger(130, new SecureRandom()).toString(32));
                editor.commit();
                getDialog().dismiss();
            }
        });
        saveButton.setEnabled(false);
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
                    saveButton.setEnabled(true);
                } else {
                    saveButton.setEnabled(false);
                }
            }
        });
        return dialogLayout;
    }
}
