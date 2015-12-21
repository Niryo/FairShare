package share.fair.fairshare.dialogs;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import share.fair.fairshare.R;

/**
 * Created by Nir on 07/11/2015.
 */
public class MainOptionsMenuDialog  extends DialogFragment {
    private int x;
    private int y;

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
        if(savedInstanceState!=null){
            this.x= savedInstanceState.getInt("x");
            this.y= savedInstanceState.getInt("y");
        }
        Window window = getDialog().getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setGravity(Gravity.TOP | Gravity.LEFT);

        WindowManager.LayoutParams params = window.getAttributes();
        params.x = x;
        params.y = y;
        window.setAttributes(params);

        getDialog().setContentView(R.layout.main_options_menu_dialog);
        View dialogLayout = inflater.inflate(R.layout.main_options_menu_dialog, container);

//=========================== Join group with key=================================================
        Button joinGroup = (Button) dialogLayout.findViewById(R.id.main_options_menu_join_group);
        joinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JoinGroupWithKeyDialog().show(getFragmentManager(), "join_group_with_key_dialog");
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
