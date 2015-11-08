package share.fair.fairshare;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Created by Nir on 21/10/2015.
 */
public class GroupOptionsMenuDialog extends DialogFragment {
    private int x;
    private int y;

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
public GroupOptionsMenuDialog(){}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setGravity(Gravity.TOP | Gravity.LEFT);

        WindowManager.LayoutParams params = window.getAttributes();
        params.x = x;
        params.y = y;
        window.setAttributes(params);

        getDialog().setContentView(R.layout.group_options_menu_dialog);
        View dialogLayout = inflater.inflate(R.layout.group_options_menu_dialog, container);
//=========================== payment history=================================================
        Button paymentHistory= (Button) dialogLayout.findViewById(R.id.options_menu_payment_history);
        paymentHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                ((GroupActivity)getActivity()).goToActionActivity();
            }
        });

        //=========================== settle up =================================================

        //=========================== show group key =================================================

        Button showGroupKey = (Button) dialogLayout.findViewById(R.id.options_menu_group_key);
        showGroupKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ((GroupActivity)getActivity()).showGroupKeyDialog();
            }
        });
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