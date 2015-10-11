package share.fair.fairshare;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ori on 5/27/2015.
 */
public class GoOutAdapter extends ArrayAdapter{

    private ArrayList<User> userList;

    public GoOutAdapter(Context context, int textViewResourceId,
                        ArrayList<User> userList) {
        super(context, textViewResourceId,userList);

        this.userList = userList;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        Log.d("user", "ConvertView " + String.valueOf(position));
//        toastGen(context, "called getview(goOut)");

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.user_go_out_row, null);
        }
        ((TextView)convertView.findViewById(R.id.tv_go_out_user_name)).setText(userList.get(position).getName());
        String textBalance = Double.toString(userList.get(position).getBalance());
        ((TextView)convertView.findViewById(R.id.tv_go_out_user_balance)).setText(textBalance);
        return convertView;
    }
    private void toastGen(Context context,String msg){
        Log.w("user", "in toastGen: " + msg);
//        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
