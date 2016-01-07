package share.fair.fairshare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * An adapter for the groups in the main activity
 */
public class GroupsAdapter extends ArrayAdapter {
    Context context;
    private List<FairShareGroup.GroupNameRecord> groupNameList;

    public GroupsAdapter(Context context, int textViewResourceId,
                         List<FairShareGroup.GroupNameRecord> userList) {
        super(context, textViewResourceId, userList);

        this.context = context;
        this.groupNameList = userList;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.row_groups_adapter, null);
        }

        TextView tvGroupName = (TextView) convertView.findViewById(R.id.groups_adapter_row_tv_group_name);
        tvGroupName.setText(groupNameList.get(position).getGroupName());
        return convertView;
    }


}
