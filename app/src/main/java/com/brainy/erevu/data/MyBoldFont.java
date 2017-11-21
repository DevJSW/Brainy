package com.brainy.erevu.data;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by John on 29-May-17.
 */
public class MyBoldFont extends TextView {


    public MyBoldFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MyBoldFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyBoldFont(Context context) {
        super(context);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Aller_Bd.ttf");
            setTypeface(tf);
        }
    }

}

