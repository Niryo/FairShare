package share.fair.fairshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.Parse;

public class MainActivity extends Activity implements View.OnClickListener {

    Button btAddGroup;
    ListView lvGroupList;
    AlertDialog adGroupCreation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "nLLyqbfak5UsJbwJ086zWMCr5Ux6RvzXOM1kBpX3", "sauupds6DzHf2EroSxBjbnORMgMLbY87UKbFW0u9");

        btAddGroup =(Button) findViewById(R.id.bt_create_new_group);
        btAddGroup.setOnClickListener(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.ad_group_creation);
        dialog.setTitle("Choose group name:");

        final Context context = this;
        final EditText etGroupName = (EditText) dialog.findViewById(R.id.et_group_creation_title);
        final Button btCreate  = (Button) dialog.findViewById(R.id.bt_group_creation_create);
        final Button btBack  = (Button) dialog.findViewById(R.id.bt_group_creation_back);
        btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etGroupName.getText().toString();
                toastGen(context,"Group name to add: "+name);
                dialog.dismiss();
            }
        });
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btCreate.setEnabled(false);
        etGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (etGroupName.getText().toString().length() > 0) {
                    btCreate.setEnabled(true);
                } else {
                    btCreate.setEnabled(false);
                }
            }
        });
        dialog.show();
    }

    private void toastGen(Context context,String msg){
        Log.d("user", "in toastGen: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

}
