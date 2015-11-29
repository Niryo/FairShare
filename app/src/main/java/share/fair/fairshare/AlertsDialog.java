package share.fair.fairshare;


import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Nir on 22/10/2015.
 */
public class AlertsDialog extends DialogFragment {
    private int x;
    private int y;


    public void setAlerts(ArrayList<Alert.AlertObject> alerts) {
        this.alerts = alerts;
    }

    private ArrayList<Alert.AlertObject> alerts;

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setGravity(Gravity.TOP | Gravity.LEFT);

        WindowManager.LayoutParams params = window.getAttributes();
        params.x = x;
        params.y = y;
        window.setAttributes(params);



        View dialogLayout = inflater.inflate(R.layout.alert_dialog_layout, container);
        LinearLayout alertsContainer = (LinearLayout) dialogLayout.findViewById(R.id.alert_dialog_container);
        if(alerts!=null){
        for (Alert.AlertObject alert : alerts) {
            View alertRow = inflater.inflate(R.layout.alert_row, null);
            TextView description = (TextView) alertRow.findViewById(R.id.alert_row_description);
            description.setText(alert.description);

            TextView userName = (TextView) alertRow.findViewById(R.id.alert_row_username);
            userName.setText(alert.useNrame);

            TextView paid = (TextView) alertRow.findViewById(R.id.alert_row_paid);
            paid.setText(new DecimalFormat("##.##").format(alert.paid+0));
            alertsContainer.addView(alertRow);

        }
        getDialog().setContentView(R.layout.alert_dialog_layout);
    }



        return dialogLayout;
    }


    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);
    }
}
