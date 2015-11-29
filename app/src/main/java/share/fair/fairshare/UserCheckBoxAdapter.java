package share.fair.fairshare;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ori on 5/20/2015.
 */
public class UserCheckBoxAdapter extends ArrayAdapter {

    ArrayList<User> checkedUsers;
    private List<User> userList;
    private Handler parentActivityMessageHandler;

    public UserCheckBoxAdapter(Context context, int textViewResourceId,
                               List<User> userList, Handler parentActivityMessageHandler) {
        super(context, textViewResourceId, userList);
        this.userList = userList;
        this.parentActivityMessageHandler = parentActivityMessageHandler;
        checkedUsers = new ArrayList<User>();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.user_check_row, null);
        }
        initLayoutPreferences(convertView);
        TextView userBalance = (TextView) convertView.findViewById(R.id.tv_user_balance);
        CheckBox cbUserRow = (CheckBox) convertView.findViewById(R.id.cb_user_row);
        cbUserRow.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                Log.w("user", "Clicked on Checkbox: " + cb.getText() + " is " + cb.isChecked());
                if (cb.isChecked()) {
                    checkedUsers.add(userList.get(position));
                    if (checkedUsers.size() > 0) {
                        Message msg = Message.obtain();
                        msg.what = GroupActivity.CHECKED_AVAILABLE;
                        parentActivityMessageHandler.sendMessage(msg);
                    }
                } else {
                    checkedUsers.remove(userList.get(position));
                    if (checkedUsers.size() == 0) {
                        Message msg = Message.obtain();
                        msg.what = GroupActivity.CHECKED_UNAVAILABLE;
                        parentActivityMessageHandler.sendMessage(msg);
                    }
                }
            }
        });

        cbUserRow.setText(userList.get(position).getName());
        if(userList.get(position).isNotified()){
            cbUserRow.setTextColor(Color.parseColor("#38B074"));
        }
        else{
            cbUserRow.setTextColor(Color.BLACK);
        }
//            String userN = (userListView.get(position)).getName();
//            holder.hUserName = userN;

//            userBalMap.get(userN).toString();
        userBalance.setText(new DecimalFormat("##.##").format(userList.get(position).getBalance()+0));
        return convertView;
    }

    private void toastGen(Context context, String msg) {
        Log.w("user", "in toastGen: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public ArrayList<User> getCheckedArray() {
        return checkedUsers;
    }

    public void clearChecked() {
        this.checkedUsers.clear();
    }


    private void initLayoutPreferences(View convertView) {


        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int screenSize;
        int configuration = getContext().getResources().getConfiguration().orientation;
        double fontSizeFactor;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            fontSizeFactor=30;
        } else {
            fontSizeFactor=30;
        }
        Display display =  wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;


        CheckBox cbUserRow = (CheckBox) convertView.findViewById(R.id.cb_user_row);
        cbUserRow.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height/fontSizeFactor));


    }
}

