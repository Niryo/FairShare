package share.fair.fairshare;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

/**
 * Created by Nir on 16/10/2015.
 */
public class GroupContextMenuDialog extends DialogFragment {
    public void setGroupNameRecord(FairShareGroup.GroupNameRecord groupNameRecord) {
        this.groupNameRecord = groupNameRecord;
    }

    FairShareGroup.GroupNameRecord groupNameRecord;

    public GroupContextMenuDialog() {
        // Empty constructor required for DialogFragment
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogLayout = inflater.inflate(R.layout.group_context_menu_dialog, container);
        getDialog().setTitle(groupNameRecord.getGroupName());

        //======================================Delete group =================================================
        Button deleteButton = (Button) dialogLayout.findViewById(R.id.group_context_menu_remove);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).removeGroup(groupNameRecord);
                dismiss();
            }
        });
        return dialogLayout;
    }
}
