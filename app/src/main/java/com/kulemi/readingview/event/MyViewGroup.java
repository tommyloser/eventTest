package com.kulemi.readingview.event;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import androidx.core.view.GestureDetectorCompat;

public class MyViewGroup extends RelativeLayout {
    public MyViewGroup(Context context) {
        super(context);
        init(context);
    }

    public MyViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public MyViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
        mDetector = new GestureDetectorCompat(context, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                Log.d(tag, "onDown");
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                Log.d(tag, "onShowPress");
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d(tag, "onSingleTapUp");
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.d(tag, "onScroll");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(tag, "onLongPress");
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.d(tag, "onFling");
                return true;
            }
        });

        interceptDetector = new GestureDetectorCompat(context, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.d(tag, "拦截 scroll");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.d(tag, "拦截 onFling");
                return true;
            }
        });
    }

    public String tag = this.getClass().getSimpleName();

    private GestureDetectorCompat mDetector;
    private GestureDetectorCompat interceptDetector;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mDetector.onTouchEvent(event)) {
            Log.d(tag, "mDetector = true");
            return true;
        }
//        printLog(event);
        boolean rs =  super.onTouchEvent(event);
        Log.d(tag, "onTouchEvent:" + rs);
        return rs;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        printLog(ev);
        boolean aaa = interceptDetector.onTouchEvent(ev);
        if (ev.getActionMasked() != MotionEvent.ACTION_DOWN && interceptDetector.onTouchEvent(ev)) {
            Log.d(tag, "onInterceptTouchEvent = true");
            return true;
        }
        boolean rs = super.onInterceptTouchEvent(ev);
        Log.d(tag, "onInterceptTouchEvent:" + rs);
        return rs;
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
////        printLog(ev);
//        Log.d(tag, "before dispatchTouchEvent:");
//        boolean rs = super.dispatchTouchEvent(ev);
//        Log.d(tag, "after dispatchTouchEvent:" + rs);
//        return rs;
//    }

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
