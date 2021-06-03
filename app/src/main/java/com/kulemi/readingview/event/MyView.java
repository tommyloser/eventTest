package com.kulemi.readingview.event;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class MyView extends View {
    public MyView(Context context) {
        super(context);
    }

    public MyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public String tag = this.getClass().getSimpleName();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        printLog(event);
        boolean rs =  super.onTouchEvent(event);
        Log.d(tag, "onTouchEvent:" + rs);
        return rs;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        printLog(event);
        Log.d(tag, "before dispatchTouchEvent:");
        boolean rs = super.dispatchTouchEvent(event);
        Log.d(tag, "after dispatchTouchEvent:" + rs);
        return rs;
    }

    private void printLog(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(tag, "ACTION_DOWN event");
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(tag, "ACTION_POINTER_DOWN event");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(tag, "ACTION_MOVE event");
                break;

            case MotionEvent.ACTION_UP:
                Log.d(tag, "ACTION_UP event");
                break;
        }
    }
}
