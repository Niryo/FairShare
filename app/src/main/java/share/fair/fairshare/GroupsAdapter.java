package share.fair.fairshare;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ori on 7/30/2015.
 */
public class GroupsAdapter extends ArrayAdapter {


    public HashMap userBalMap;
    public ArrayList<String> goOutNameList;
    Context context;
    private ArrayList<NameAndKey> nameAndKeys;

    public GroupsAdapter(Context context, int textViewResourceId,
                         ArrayList userList) {
        super(context, textViewResourceId, userList);
        this.context = context;
        this.nameAndKeys = userList;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        Log.d("user", "ConvertView " + String.valueOf(position));

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.group_row, null);
        }
        TextView tvGrpName = (TextView) convertView.findViewById(R.id.tv_row_grp_name);


        tvGrpName.setText(nameAndKeys.get(position).getName());
        return convertView;
    }

    private void toastGen(Context context, String msg) {
        Log.d("user", "in toastGen: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

}
