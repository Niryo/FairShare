package share.fair.fairshare.dialogs;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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

import share.fair.fairshare.Alert;
import share.fair.fairshare.R;
import share.fair.fairshare.views.RelativeTextView;

/**
 * An alert dialog to show all changes in user balance, if notified
 */
public class BalanceChangeAlertsDialog extends DialogFragment {
    //the position of the dialog on the screen:
    private int x;
    private int y;
    private ArrayList<Alert.AlertObject> alerts; //keeps all the information about the alerts

    /**
     * Set the alert object
     *
     * @param alerts
     */
    public void setAlerts(ArrayList<Alert.AlertObject> alerts) {
        this.alerts = alerts;
    }

    /**
     * Set the x position of the dialog
     *
     * @param x
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Set the y position of the dialog
     *
     * @param y
     */
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("x", x);
        outState.putInt("y", y);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.x = savedInstanceState.getInt("x");
            this.y = savedInstanceState.getInt("y");
        }
        Window window = getDialog().getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setGravity(Gravity.TOP | Gravity.LEFT);

        //position the dialog on the sceen:
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = x;
        params.y = y;
        window.setAttributes(params);


        View dialogLayout = inflater.inflate(R.layout.alert_dialog_layout, container);
        LinearLayout alertsContainer = (LinearLayout) dialogLayout.findViewById(R.id.alert_dialog_container);
        //show the alerts:
        if (alerts != null) {
            for (Alert.AlertObject alert : alerts) {
                View alertRow = inflater.inflate(R.layout.alert_row, null);
                RelativeTextView description = (RelativeTextView) alertRow.findViewById(R.id.alert_row_description);
                description.setText(alert.description);

                RelativeTextView tvUserName = (RelativeTextView) alertRow.findViewById(R.id.alert_row_username);
                tvUserName.setText(alert.useNrame);

                RelativeTextView tvPaid = (RelativeTextView) alertRow.findViewById(R.id.alert_row_paid);
                tvPaid.setText(new DecimalFormat("##.##").format(alert.paid + 0));
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
