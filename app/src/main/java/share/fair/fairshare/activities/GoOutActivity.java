package share.fair.fairshare.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.ArrayList;

import share.fair.fairshare.Action;
import share.fair.fairshare.R;

public class GoOutActivity extends Activity {

    Button backToGroup;
    Button calculateButton;
    ArrayList<GoOutFragment.GoOutObject> goOutObjectList;
    EditText description;
    ShowcaseView showcaseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_out);
        isFirstRun();
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
        final String installationId =   getIntent().getStringExtra("installationId");
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
                Action action =  goOutFragment.calculate(installationId);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("action", action);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });


    }


    private void isFirstRun(){
        final SharedPreferences settings = getSharedPreferences("MAIN_PREFERENCES", 0);
        boolean isFirstRun = settings.getBoolean("isFirstRunGoOutActivity", true);
        if(isFirstRun){
            showTutorial();
            SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("isFirstRunGoOutActivity", false);
                editor.commit();
        }
    }


    private void showTutorial(){
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(Target.NONE).setContentTitle("'New bill' page").setContentText("Here you create a new bill.\n Choose bill title, for example: 'shopping', and then enter for each user how much he actually paid" +
                                " (under 'Amout paid'), and how much he should have paid (under 'Share').\n You can leave the Share field blank and FairShare will automatically calculate the user share.").build();

        showcaseView.setStyle(R.style.ShowCaseCustomStyle);
        showcaseView.setButtonText("Next");
    }

}
