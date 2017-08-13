package com.brainy.brainy.data;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.TextView;

import static java.security.AccessController.getContext;
import static java.security.AccessController.getContext;
import static java.security.AccessController.getContext;
import static java.security.AccessController.getContext;

/**
 * Created by John on 29-May-17.
 */
public class MyFont extends TextView {


    public MyFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MyFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyFont(Context context) {
        super(context);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Aller_Rg.ttf");
            setTypeface(tf);
        }
    }

}

