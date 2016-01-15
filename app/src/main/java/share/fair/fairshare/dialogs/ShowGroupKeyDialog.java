package share.fair.fairshare.dialogs;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import share.fair.fairshare.R;

/**
 * Group key dialog.
 * This dialog shows up when the user clicks on "show group key" inside the options menu of the gorup.
 */
public class ShowGroupKeyDialog extends DialogFragment {

    private String groupKey;
    private String groupName;

    /**
     * Set group key
     *
     * @param groupKey
     */
    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    /**
     * Set group name
     *
     * @param groupName
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("groupKey", groupKey);
        outState.putString("groupName", groupName);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            groupKey = savedInstanceState.getString("groupKey");
            groupName = savedInstanceState.getString("groupName");

        }
        View dialogLayout = inflater.inflate(R.layout.dialog_show_group_key, container);
        getDialog().setContentView(R.layout.dialog_show_group_key);
        getDialog().setTitle("Group key:");
        final TextView tvGroupKey = (TextView) dialogLayout.findViewById(R.id.show_group_key_tv_key);
        //concert the length to string, with leading zero if needed:
        String stringLength = groupName.length() < 9 ? "0" + groupName.length() : String.valueOf(groupName.length());
        tvGroupKey.setText(stringLength + groupName + groupKey);
        //set copy to clipboard button:
        Button btnCopy = (Button) dialogLayout.findViewById(R.id.show_group_key_btn_copy);
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(tvGroupKey.getText().toString());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("text label", tvGroupKey.getText().toString());
                    clipboard.setPrimaryClip(clip);
                }
                dismiss();
                Toast.makeText(getContext(), "Group key has been copied to clipboard", Toast.LENGTH_LONG).show();
            }
        });
        return dialogLayout;
    }
}
