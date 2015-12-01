package share.fair.fairshare;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Nir on 29/11/2015.
 */
public class RelativeEditText extends EditText {
    public RelativeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RelativeView,
                0, 0);


        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int height = size.y;
        int factor;
        int configuration = getResources().getConfiguration().orientation;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            factor= attributes.getInteger(R.styleable.RelativeView_landscapeFactor, -1);
        }else{
            factor= attributes.getInteger(R.styleable.RelativeView_portraitFactor, -1);
        }


        String placeHolderText= attributes.getString(R.styleable.RelativeView_placeHolderText);
        if(placeHolderText!=null){
            TextView textView= new TextView(context);
            textView.setText(placeHolderText);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / factor));
            final Rect bounds = new Rect();
            Paint textPaint = textView.getPaint();
            textPaint.getTextBounds(placeHolderText, 0, placeHolderText.length(), bounds);
            int test = bounds.width();
            setWidth(bounds.width());

        }

        setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / factor));

    }


}
