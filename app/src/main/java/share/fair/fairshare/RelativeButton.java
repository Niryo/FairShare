package share.fair.fairshare;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Created by Nir on 28/11/2015.
 */
public class RelativeButton extends Button {

    public RelativeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RelativeButton,
                0, 0);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int height = size.y;
        final int buttonWidthFactor;
        final int buttonHeightFactor;
        final int textSizeFactor;

        int configuration = getResources().getConfiguration().orientation;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            buttonWidthFactor= attributes.getInteger(R.styleable.RelativeButton_landscapeWidthFactor, -1);;
             buttonHeightFactor=attributes.getInteger(R.styleable.RelativeButton_landscapeHeightFactor, -1);;
             textSizeFactor=attributes.getInteger(R.styleable.RelativeButton_landscapeTextFactor, -1);;
        }else{
            buttonWidthFactor=attributes.getInteger(R.styleable.RelativeButton_portraitWidthFactor, -1);;
            buttonHeightFactor=attributes.getInteger(R.styleable.RelativeButton_portraitHeightFactor, -1);;
            textSizeFactor=attributes.getInteger(R.styleable.RelativeButton_portraitTextFactor, -1);;
        }
        post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) getLayoutParams();
                if(buttonWidthFactor!=-1) {
                    params.width = (int) (height / buttonWidthFactor);
                }
                if(buttonHeightFactor!=-1) {
                    params.height = (int) (height / buttonHeightFactor);
                }
                setLayoutParams(params);

                if(textSizeFactor!=-1){
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (height / textSizeFactor));
                }


            }
        });

    }


}
