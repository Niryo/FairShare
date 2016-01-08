package share.fair.fairshare.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import share.fair.fairshare.FairShareGroup;
import share.fair.fairshare.R;
import share.fair.fairshare.activities.MainActivity;

/**
 * The context menu of the group.
 * This dialog shows up when the user clicks and holds a groups name in the main activity
 */
public class GroupContextMenuDialog extends DialogFragment {
    FairShareGroup.GroupNameRecord groupNameRecord;

    public GroupContextMenuDialog() {
        // Empty constructor required for DialogFragment
    }

    public void setGroupNameRecord(FairShareGroup.GroupNameRecord groupNameRecord) {
        this.groupNameRecord = groupNameRecord;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("groupNameRecord", groupNameRecord);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.groupNameRecord = (FairShareGroup.GroupNameRecord) savedInstanceState.getSerializable("groupNameRecord");
        }
        View dialogLayout = inflater.inflate(R.layout.context_menu_group, container);
        getDialog().setTitle(groupNameRecord.getGroupName());

        //======================================Delete group =================================================
        Button btnDelete = (Button) dialogLayout.findViewById(R.id.group_context_menu_btn_remove);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).removeGroup(groupNameRecord);
                dismiss();
            }
        });
        return dialogLayout;
    }
}
