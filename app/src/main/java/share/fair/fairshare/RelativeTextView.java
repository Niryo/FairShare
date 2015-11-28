package share.fair.fairshare;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by Nir on 28/11/2015.
 */
public class RelativeTextView extends TextView {
    public RelativeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RelativeTextView,
                0, 0);



        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        int factor;
        int configuration = getResources().getConfiguration().orientation;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            factor= attributes.getInteger(R.styleable.RelativeTextView_landscapeFactor, -1);;
        }else{
            factor= attributes.getInteger(R.styleable.RelativeTextView_portraitFactor, -1);
        }

        setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / factor));

    }



}
