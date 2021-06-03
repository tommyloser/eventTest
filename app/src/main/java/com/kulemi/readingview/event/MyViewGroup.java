package com.kulemi.readingview.event;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class MyViewGroup extends RelativeLayout {
    public MyViewGroup(Context context) {
        super(context);
    }

    public MyViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public String tag = this.getClass().getSimpleName();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        printLog(event);
        boolean rs =  super.onTouchEvent(event);
        Log.d(tag, "onTouchEvent:" + "true");
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        printLog(ev);
        boolean rs = super.onInterceptTouchEvent(ev);
        Log.d(tag, "onInterceptTouchEvent:" + rs);
        return rs;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        printLog(ev);
        Log.d(tag, "before dispatchTouchEvent:");
        boolean rs = super.dispatchTouchEvent(ev);
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
