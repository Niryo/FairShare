package share.fair.fairshare;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import share.fair.fairshare.activities.GroupActivity;

/**
 * An adapter for user list on the New bill
 */
public class UserCheckBoxAdapter extends ArrayAdapter {

    ArrayList<User> checkedUsers; //list of checked users
    private List<User> userList; //list of all users
    private GroupActivity groupActivity; //the parent group activity


    public UserCheckBoxAdapter(Context context, GroupActivity activity, int textViewResourceId,
                               List<User> userList) {
        super(context, textViewResourceId, userList);
        this.userList = userList;
        checkedUsers = new ArrayList<>();
        this.groupActivity = activity;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.row_user_checkbox_adapter, null);
        }
        initLayoutPreferences(convertView);
        TextView tvUserBalance = (TextView) convertView.findViewById(R.id.user_checkbox_adapter_tv_balance);
        CheckBox cbUserRow = (CheckBox) convertView.findViewById(R.id.user_checkbox_adapter_cb_user_name);
        cbUserRow.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                //when we click on the checkbox, we add the user to the list and
                //update the group activity:
                if (cb.isChecked()) {
                    checkedUsers.add(userList.get(position));
                    if (checkedUsers.size() > 0) {
                        groupActivity.messageHandler(GroupActivity.CHECKED_AVAILABLE, null);
                    }
                } else {
                    checkedUsers.remove(userList.get(position));
                    if (checkedUsers.size() == 0) {
                        groupActivity.messageHandler(GroupActivity.CHECKED_UNAVAILABLE, null);
                    }
                }
            }
        });

        cbUserRow.setText(userList.get(position).getUserName());

        if (userList.get(position).isNotified()) {
            cbUserRow.setTextColor(Color.parseColor("#38B074"));
        } else {
            cbUserRow.setTextColor(Color.BLACK);
        }

        if (userList.get(position).isGhost()) {
            cbUserRow.setTextColor(Color.RED);
        }


        double userBalance = userList.get(position).getBalance();
        if (userBalance == -0) {
            userBalance = 0;
        }
        tvUserBalance.setText(new DecimalFormat("##.##").format(userBalance));
        return convertView;
    }


    /**
     * Returns a list with all the checked users
     *
     * @return list of checked users
     */
    public ArrayList<User> getCheckedArray() {
        Collections.sort(checkedUsers, new Comparator<User>() {
            @Override
            public int compare(User first, User second) {
                return first.getUserName().compareTo(second.getUserName());
            }
        });
        return checkedUsers;
    }

    /**
     * Clear the list of checked users
     */
    public void clearChecked() {
        this.checkedUsers.clear();
    }


    /**
     * Sets the sytle of the checkbox view.
     *
     * @param convertView
     */
    private void initLayoutPreferences(View convertView) {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int screenSize;
        int configuration = getContext().getResources().getConfiguration().orientation;
        double fontSizeFactor;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            fontSizeFactor = 30;
        } else {
            fontSizeFactor = 30;
        }
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;


        CheckBox cbUserRow = (CheckBox) convertView.findViewById(R.id.user_checkbox_adapter_cb_user_name);
        cbUserRow.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / fontSizeFactor));


    }
}

