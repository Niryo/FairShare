package share.fair.fairshare;


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

/**
 * Created by Nir on 04/11/2015.
 */
public class GroupKeyDialog extends DialogFragment {

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    private String groupKey;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogLayout = inflater.inflate(R.layout.group_key_dialog, container);
        getDialog().setContentView(R.layout.group_key_dialog);
        getDialog().setTitle("Group key:");
        TextView groupKeyText = (TextView) dialogLayout.findViewById(R.id.group_key_dialog_text);
        groupKeyText.setText(groupKey);
        Button copyButton = (Button) dialogLayout.findViewById(R.id.group_key_dialog_copy);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sdk = android.os.Build.VERSION.SDK_INT;
                if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(groupKey);
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("text label",groupKey);
                    clipboard.setPrimaryClip(clip);
                }
                dismiss();
                Toast.makeText(getContext(), "Group key has been copied to clipboard", Toast.LENGTH_LONG).show();
            }
        });
        return dialogLayout;
    }
}
