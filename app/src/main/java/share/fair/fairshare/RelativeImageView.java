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
import android.widget.ImageView;

/**
 * Created by Nir on 29/11/2015.
 */
public class RelativeImageView extends ImageView {


    public RelativeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RelativeView,
                0, 0);

        final int imageSizeFactor;
        int configuration = getResources().getConfiguration().orientation;
        if (configuration == Configuration.ORIENTATION_LANDSCAPE) {
            imageSizeFactor= attributes.getInteger(R.styleable.RelativeView_landscapeFactor, -1);
        }else{
            imageSizeFactor=attributes.getInteger(R.styleable.RelativeView_portraitFactor, -1);
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
                if (imageSizeFactor != -1) {
                    newWidth = (int) (height / imageSizeFactor);
                    params.width = newWidth;
                }
                if (imageSizeFactor != -1) {
                    params.height = (int) (originalFactor * newWidth);
                }
                setLayoutParams(params);

            }
        });
    }
}
