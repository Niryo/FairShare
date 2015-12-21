package share.fair.fairshare.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import share.fair.fairshare.FairShareGroup;
import share.fair.fairshare.activities.MainActivity;
import share.fair.fairshare.R;

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("groupNameRecord", groupNameRecord);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(savedInstanceState!=null){
            this.groupNameRecord = (FairShareGroup.GroupNameRecord) savedInstanceState.getSerializable("groupNameRecord");
        }
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
