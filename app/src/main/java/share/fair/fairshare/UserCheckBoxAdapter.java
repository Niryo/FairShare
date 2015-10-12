package share.fair.fairshare;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ori on 5/20/2015.
 */
public class UserCheckBoxAdapter extends ArrayAdapter {

    private ArrayList<User> userList;
    Context context;
//    public HashMap userBalMap;
    public ArrayList<String> goOutNameList;

    public UserCheckBoxAdapter(Context context, int textViewResourceId,
                               ArrayList<User> userList) {
    super(context, textViewResourceId, userList);
    this.context = context;
    this.userList = userList;
//    this.userBalMap = CurGroup.grpUserBalMap;
    this.goOutNameList = new ArrayList<String>();
}
        private class ViewHolder {
            TextView userBalance;
            CheckBox cbUserRow;
            String hUserName;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Log.d("user", "ConvertView " + String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.user_check_row, null);
                holder = new ViewHolder();
                holder.userBalance = (TextView) convertView.findViewById(R.id.tv_user_balance);
                holder.cbUserRow = (CheckBox) convertView.findViewById(R.id.cb_user_row);
                convertView.setTag(holder);
                holder.cbUserRow.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        Log.d("user", "Clicked on Checkbox: " + cb.getText() + " is " + cb.isChecked());
                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.cbUserRow.setText(userList.get(position).getName()+"   ");
//            String userN = (userList.get(position)).getName();
//            holder.hUserName = userN;

//            userBalMap.get(userN).toString();
            holder.userBalance.setText(new DecimalFormat("##.##").format(userList.get(position).getBalance()));
            return convertView;
        }
    private void toastGen(Context context,String msg){
        Log.w("user", "in toastGen: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}


