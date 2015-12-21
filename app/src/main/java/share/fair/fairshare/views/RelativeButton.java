package share.fair.fairshare.views;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import share.fair.fairshare.R;

/**
 * Created by Nir on 28/11/2015.
 */
public class RelativeButton extends Button {

    public RelativeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RelativeView,
                0, 0);

         final int buttonSizeFactor;
         final int textSizeFactor;
        int configuration = getResources().getConfiguration().orientation;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            buttonSizeFactor= attributes.getInteger(R.styleable.RelativeView_landscapeButtonSizeFactor, -1);
            textSizeFactor=attributes.getInteger(R.styleable.RelativeView_landscapeTextSizeFactor, -1);
        }else{
            buttonSizeFactor=attributes.getInteger(R.styleable.RelativeView_portraitButtonSizeFactor, -1);
            textSizeFactor=attributes.getInteger(R.styleable.RelativeView_portraitTextSizeFactor, -1);
        }


        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int height = size.y;


        post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) getLayoutParams();
                double originalFactor = getBackground().getIntrinsicHeight() / getBackground().getIntrinsicWidth();
                int newWidth = 0;
                if (buttonSizeFactor != -1) {
                    newWidth = (int) (height / buttonSizeFactor);
                    params.width = newWidth;
                }
                if (buttonSizeFactor != -1) {
                    params.height = (int) (originalFactor * newWidth);
                }
                setLayoutParams(params);

                if (textSizeFactor != -1) {
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / textSizeFactor));
                }
            }
        });

    }

}
