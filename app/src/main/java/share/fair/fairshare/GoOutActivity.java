package share.fair.fairshare;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class GoOutActivity extends Activity {


    Button backToGroup;
    Button calculateButton;
    ArrayList<User> nameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_out);
        backToGroup = (Button)findViewById(R.id.back_to_group_button);
        backToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo: go back to the last screen(group screen)
            }
        });

        nameList = new ArrayList<User>();
        nameList.add(new User("kipi1", -1.23));
        nameList.add(new User("kipi2", -1.23));
        nameList.add(new User("kipi3", -1.23));
        nameList.add(new User("kipi4", -1.23));
        nameList.add(new User("kipi5", -1.23));
        nameList.add(new User("kipi6", -1.23));
        nameList.add(new User("kipi7", -1.23));
        nameList.add(new User("kipi8", -1.23));
        nameList.add(new User("kipi9", -1.23));
        nameList.add(new User("kipi10", -1.23));
        nameList.add(new User("kipi11", -1.23));
        nameList.add(new User("kipi12", -1.23));
        nameList.add(new User("kipi13", -1.23));

        final ArrayList<View> viewsList = new ArrayList<>();
        LinearLayout list= (LinearLayout) findViewById(R.id.list_of_users);
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(User user: nameList){
        View newView = vi.inflate(R.layout.user_go_out_row, null);
            ((TextView)newView.findViewById(R.id.tv_go_out_user_name)).setText(user.getName());
            String textBalance = Double.toString(user.getBalance());
            ((TextView)newView.findViewById(R.id.tv_go_out_user_balance)).setText(textBalance);
            list.addView(newView);
            viewsList.add(newView);
            user.resetPaidAndShare();
        }
        calculateButton = (Button) findViewById(R.id.calculate_button);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(View row: viewsList){
                    double paidInput = Double.parseDouble(((EditText)row.findViewById(R.id.et_paid)).getText().toString());


                }


            }
        });

    }
}
