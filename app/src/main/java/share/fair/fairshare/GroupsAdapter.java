package share.fair.fairshare;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ori on 7/30/2015.
 */
public class GroupsAdapter extends ArrayAdapter {


    private ArrayList userList;
    Context context;
    public HashMap userBalMap;
    public ArrayList<String> goOutNameList;

    public GroupsAdapter(Context context, int textViewResourceId,
                         ArrayList userList) {
        super(context, textViewResourceId, userList);
        this.context = context;
        this.userList = userList;

    }

    private class ViewHolder {
        ImageView ivImageGrp;
        TextView tvGrpName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        Log.d("user", "ConvertView " + String.valueOf(position));

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.group_row, null);
            holder = new ViewHolder();
            holder.ivImageGrp = (ImageView) convertView.findViewById(R.id.iv_grp_img);
            holder.tvGrpName = (TextView) convertView.findViewById(R.id.tv_row_grp_name);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvGrpName.setText( (HashMap)(userList.get(position)) );
        return convertView;
    }
    private void toastGen(Context context,String msg){
        Log.d("user", "in toastGen: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

}
