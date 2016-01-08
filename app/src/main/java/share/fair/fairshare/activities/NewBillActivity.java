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

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;

import java.util.ArrayList;

import share.fair.fairshare.Action;
import share.fair.fairshare.R;

/**
 * The new bill activity
 */
public class NewBillActivity extends Activity {

    Button btnBackToGroup; //back to group button
    Button btnCalculate; //calculate new bill
    ArrayList<BillFragment.BillLine> billLineInfoList; //lines info for the calculate fragment
    ShowcaseView showcaseView; //for the tutorial

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_bill);
        isFirstRun();
        btnBackToGroup = (Button) findViewById(R.id.new_bill_btn_back);
        btnBackToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();

            }
        });

        billLineInfoList = (ArrayList<BillFragment.BillLine>) getIntent().getSerializableExtra("goOutList");
        final String installationId =   getIntent().getStringExtra("installationId");

        //sets the calculation fragment:
        final BillFragment goOutFragment = new BillFragment();
        goOutFragment.billLineInfoList = billLineInfoList;
        final FragmentManager fm = getFragmentManager();
        FragmentTransaction ft= fm.beginTransaction();
        ft.add(R.id.go_out_activity_fragment_container, goOutFragment, "goOutFragment");
        ft.commit();


        btnCalculate = (Button) findViewById(R.id.new_bill_btn_done);
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Action action = goOutFragment.createNewBill(installationId);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("action", action);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });


    }

    /**
     * Cehck if this is the first run of the activity and if so run launch tutorial
     */
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

    /**
     * Tutorial for this activity
     */
    private void showTutorial(){
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(Target.NONE).setContentTitle("'New bill' page").setContentText("Here you create a new bill.\n Choose bill title, for example: 'shopping', and then enter for each user how much he actually paid" +
                                " (under 'Amout paid'), and how much he should have paid (under 'Share').\n You can leave the Share field blank and FairShare will automatically calculate the user share.").build();

        showcaseView.setStyle(R.style.ShowCaseCustomStyle);
        showcaseView.setButtonText("Next");
    }

}
