package share.fair.fairshare;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class GoOutActivity extends Activity {

    Button backToGroup;
    Button calculateButton;
    ArrayList<GoOutFragment.GoOutObject> goOutObjectList;
    EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_out);

        backToGroup = (Button) findViewById(R.id.back_to_group_button);
        backToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();

            }
        });

        goOutObjectList = (ArrayList<GoOutFragment.GoOutObject>) getIntent().getSerializableExtra("goOutList");

        final GoOutFragment goOutFragment= new GoOutFragment();
        goOutFragment.goOutObjectList = goOutObjectList;
        final FragmentManager fm = getFragmentManager();
        FragmentTransaction ft= fm.beginTransaction();
        ft.add(R.id.go_out_activity_fragment_container, goOutFragment, "goOutFragment");
        ft.commit();


        calculateButton = (Button) findViewById(R.id.calculate_button);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Action action =  goOutFragment.calculate();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("action", action);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });


        initLayoutPreferences();
    }





    private void initLayoutPreferences() {
        double backButtonFactor;
        double regularButtonSizeFactor;

        int configuration = getResources().getConfiguration().orientation;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            backButtonFactor=15;
            regularButtonSizeFactor=40;

        } else {
            backButtonFactor=15;
            regularButtonSizeFactor=40;
        }
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;


        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) backToGroup.getLayoutParams();
        params.width = (int)(height / backButtonFactor);
        params.height = (int) (height / backButtonFactor);
        backToGroup.setLayoutParams(params);

        calculateButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / regularButtonSizeFactor));

    }


}
