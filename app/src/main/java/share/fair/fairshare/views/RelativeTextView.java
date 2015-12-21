package share.fair.fairshare.views;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import share.fair.fairshare.R;

/**
 * Created by Nir on 28/11/2015.
 */
public class RelativeTextView extends TextView {
    public RelativeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RelativeView,
                0, 0);
        int factor;
        int configuration = getResources().getConfiguration().orientation;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            factor= attributes.getInteger(R.styleable.RelativeView_landscapeFactor, -1);;
        }else{
            factor= attributes.getInteger(R.styleable.RelativeView_portraitFactor, -1);
        }

        initTextSize(context,factor);

    }

    public RelativeTextView(Context context, int landscapeFactor, int portraitFactor){
        super(context);
        int configuration = getResources().getConfiguration().orientation;
        int factor;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            factor= landscapeFactor;
        }else{
            factor=portraitFactor;
        }
        initTextSize(context,factor);
    }

    private void initTextSize(Context context , int factor){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / factor));
    }



}
