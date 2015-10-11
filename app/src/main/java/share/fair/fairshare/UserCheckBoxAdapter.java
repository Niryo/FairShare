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

    private ArrayList userList;
    Context context;
    public HashMap userBalMap;
    public ArrayList<String> goOutNameList;

    public UserCheckBoxAdapter(Context context, int textViewResourceId,
                               ArrayList userList) {
    super(context, textViewResourceId, userList);
    this.context = context;
    this.userList = CurGroup.grpUserList;
    this.userBalMap = CurGroup.grpUserBalMap;
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
                holder.cbUserRow.setOnClickListener( new OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        Log.d("user", "Clicked on Checkbox: " + cb.getText() + " is " + cb.isChecked());
                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.cbUserRow.setText(userList.get(position).toString()+"   ");
            holder.hUserName = userList.get(position).toString();
            String userN = (userList.get(position)).toString();

//            userBalMap.get(userN).toString();
            holder.userBalance.setText(new DecimalFormat("##.##").format(userBalMap.get(userN)));
            final ViewHolder holderFinal = holder;
            holder.cbUserRow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    Boolean contains = false;

                    if(cb.isChecked()){
                        if(! (contains = goOutNameList.contains( holderFinal.hUserName )) ) {
                            goOutNameList.add(holderFinal.hUserName);
                            CurGroup.grpGoOutList = goOutNameList;
                        }
                    }
                    if(!cb.isChecked()){
                        if((contains = goOutNameList.contains(holderFinal.hUserName) ) ){
                            goOutNameList.remove(holderFinal.hUserName);
                            CurGroup.grpGoOutList = goOutNameList;
                        }
                    }

                }
            });
            return convertView;

        }
    private void toastGen(Context context,String msg){
        Log.d("user", "in toastGen: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}


