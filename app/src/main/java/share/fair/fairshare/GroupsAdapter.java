package share.fair.fairshare;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ori on 7/30/2015.
 */
public class GroupsAdapter extends ArrayAdapter {


    public HashMap userBalMap;
    public ArrayList<String> goOutNameList;
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


        Log.d("user", "ConvertView " + String.valueOf(position));

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.group_row, null);
            initLayoutPreferences(convertView);
        }
        TextView tvGrpName = (TextView) convertView.findViewById(R.id.tv_row_grp_name);


        tvGrpName.setText(groupNameList.get(position).getGroupName());
        return convertView;
    }

    private void initLayoutPreferences(View convertView) {
        double groupNameFactor;
        int arrowFactor;
        double rowFactor;
        int configuration = getContext().getResources().getConfiguration().orientation;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            groupNameFactor = 27;
            arrowFactor = 14;
            rowFactor = 9;
        } else {
            groupNameFactor = 23;
            arrowFactor = 17;
            rowFactor = 10;
        }
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        TextView textView = (TextView) convertView.findViewById(R.id.tv_row_grp_name);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / groupNameFactor));
        ImageView arrow = (ImageView) convertView.findViewById(R.id.group_row_arrow);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) arrow.getLayoutParams();
        params.width = height / arrowFactor;
        params.height = height / arrowFactor;
        arrow.setLayoutParams(params);
        RelativeLayout row = (RelativeLayout) convertView.findViewById(R.id.group_row_container);
        row.setMinimumHeight((int) (height / rowFactor));


    }


    private void toastGen(Context context, String msg) {
        Log.d("user", "in toastGen: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

}
