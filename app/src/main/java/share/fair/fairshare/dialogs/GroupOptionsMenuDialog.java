package share.fair.fairshare.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import share.fair.fairshare.R;
import share.fair.fairshare.activities.GroupActivity;

/**
 * Group's options menu dialog.
 * This dialog shows up when the user clicks on the options menu button inside the group activity
 */
public class GroupOptionsMenuDialog extends DialogFragment {
    //position on the screen:
    private int x;
    private int y;

    public GroupOptionsMenuDialog() {
    }

    public void setX(int x) {
        this.x = x;
    }

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
        //position the dialog on the screen:
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = x;
        params.y = y;
        window.setAttributes(params);

        getDialog().setContentView(R.layout.options_menu_group);
        View dialogLayout = inflater.inflate(R.layout.options_menu_group, container);
//=========================== payment history=================================================

        Button btnPaymentHistory = (Button) dialogLayout.findViewById(R.id.group_options_menu_btn_payment_history);
        btnPaymentHistory.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                ((GroupActivity) getActivity()).goToActionActivity();
            }
        });

        //=========================== settle up =================================================

        Button btnSettleUp = (Button) dialogLayout.findViewById(R.id.group_options_menu_btn_settle_up);
        btnSettleUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ((GroupActivity) getActivity()).settleUp();
            }
        });
        //=========================== show group key =================================================

        Button btnShowGroupKey = (Button) dialogLayout.findViewById(R.id.group_options_menu_btn_group_key);
        btnShowGroupKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ((GroupActivity) getActivity()).showGroupKeyDialog();
            }
        });

        //=========================== Invite by mail =================================================

        Button btnInviteByMail = (Button) dialogLayout.findViewById(R.id.group_options_menu_btn_invite_by_mail);
        btnInviteByMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new InviteByMailDialog().show(getFragmentManager(), "InviteByMailDialog");
                dismiss();
            }
        });
        return dialogLayout;


    }

    @Override
    public void onStart() {
        super.onStart();
        //Make background transparent:
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);
    }


}
