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
 * Created by Nir on 04/11/2015.
 */
public class GroupKeyDialog extends DialogFragment {

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }


    private String groupKey;
    private String groupName;

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
        if(savedInstanceState!=null){
             groupKey= savedInstanceState.getString("groupKey");
             groupName= savedInstanceState.getString("groupName");

        }
        View dialogLayout = inflater.inflate(R.layout.dialog_show_group_key, container);
        getDialog().setContentView(R.layout.dialog_show_group_key);
        getDialog().setTitle("Group key:");
        final TextView groupKeyText = (TextView) dialogLayout.findViewById(R.id.show_group_key_tv_key);
        String stringLength= groupName.length()<9? "0"+groupName.length(): String.valueOf(groupName.length());
        groupKeyText.setText(stringLength+groupName+groupKey);
        Button copyButton = (Button) dialogLayout.findViewById(R.id.show_group_key_btn_copy);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sdk = android.os.Build.VERSION.SDK_INT;
                if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(groupKeyText.getText().toString());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("text label",groupKeyText.getText().toString());
                    clipboard.setPrimaryClip(clip);
                }
                dismiss();
                Toast.makeText(getContext(), "Group key has been copied to clipboard", Toast.LENGTH_LONG).show();
            }
        });
        return dialogLayout;
    }
}
