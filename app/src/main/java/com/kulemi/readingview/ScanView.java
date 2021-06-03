package com.kulemi.readingview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.core.view.GestureDetectorCompat;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 继承relativeLayout
 */
public class ScanView extends RelativeLayout
{
    public static final String TAG = "ScanView";
    private boolean isInit = true;
    // 滑动的时候存在两页可滑动，要判断是哪一页在滑动
    private boolean isPreMoving = true, isCurrMoving = true;
    // 当前是第几页
    private int index;  //当前页码
    private float lastX;
    // 前一页，当前页，下一页的左边位置
    private int prePageLeft = 0, currPageLeft = 0, nextPageLeft = 0;
    // 三张页面
    private View prePage, currPage, nextPage;
    // 页面状态
    private static final int STATE_MOVE = 0; //正在动画
    private static final int STATE_STOP = 1; //停止动画
    // 滑动的页面，只有前一页和当前页可滑
    private static final int PRE = 2;
    private static final int CURR = 3;
    private int state = STATE_STOP;
    // 正在滑动的页面右边位置，用于绘制阴影
    private float right;
    // 手指滑动的距离
    private float moveLenght;
    // 页面宽高
    private int mWidth, mHeight;
    // 获取滑动速度
    private VelocityTracker vt;
    // 防止抖动
    private float speed_shake = 20;
    // 当前滑动速度
    private float speed;
    private Timer timer;
    private MyTimerTask mTask;
    // 滑动动画的移动速度
    public static final int MOVE_SPEED = 20;
    // 页面适配器
    private PageAdapter adapter;
    /**
     * 过滤多点触碰的控制变量
     */
    private int mEvents;

    private GestureDetectorCompat mDetector;

    public void setAdapter(ScanViewAdapter adapter)
    {
        removeAllViews();
        this.adapter = adapter;
        //添加上一页
        prePage = adapter.getView();
        addView(prePage, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        //数据绑定上一页
        adapter.addContent(prePage, index - 1);

        //添加当前页
        currPage = adapter.getView();
        addView(currPage, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        adapter.addContent(currPage, index);

        //添加下一页
        nextPage = adapter.getView();
        addView(nextPage, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        adapter.addContent(nextPage, index + 1);


        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "OnClickListener");
            }
        });
    }

    /**
     * 向左滑。注意可以滑动的页面只有当前页和前一页
     *
     * @param which
     */
    private void moveLeft(int which)
    {
        switch (which)
        {
            case PRE: //上一页左滑
                prePageLeft -= MOVE_SPEED; //上一页左边 -10
                if (prePageLeft < -mWidth) //直到 上一页滑到 -width
                    prePageLeft = -mWidth;
                right =  prePageLeft + mWidth; //计算右边阴影位置
                break;
            case CURR://当前页左滑
                currPageLeft -= MOVE_SPEED; //当前页左边 -10
                if (currPageLeft < -mWidth) //直到 -width
                    currPageLeft = -mWidth;
                right = currPageLeft + mWidth; //计算右边阴影位置
                break;
        }
    }

    /**
     * 向右滑。注意可以滑动的页面只有当前页和前一页
     *
     * @param which
     */
    private void moveRight(int which)
    {
        switch (which)
        {
            case PRE: //上一页右滑
                prePageLeft += MOVE_SPEED;
                if (prePageLeft > 0)
                    prePageLeft = 0;
                right = mWidth + prePageLeft;
                break;
            case CURR: //当前页右滑
                currPageLeft += MOVE_SPEED;
                if (currPageLeft > 0)
                    currPageLeft = 0;
                right = mWidth + currPageLeft;
                break;
        }
    }

    /**
     * 当往回翻过一页时添加前一页在最左边
     * 最上面添加一页， 最底下删除一页
     */
    private void addPrePage()
    {
        removeView(nextPage);
        //-1 是加在最后
        addView(nextPage, -1, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        // 从适配器获取前一页内容
        adapter.addContent(nextPage, index - 1);
        // 交换顺序
        View temp = nextPage;
        nextPage = currPage;
        currPage = prePage;
        prePage = temp;
        prePageLeft = -mWidth; //重置prePageLeft
    }

    /**
     * 当往前翻过一页时，添加一页在最底下
     * 删除最上页， 底下添加新一页
     */
    private void addNextPage()
    {
        removeView(prePage);
        addView(prePage, 0, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        // 从适配器获取后一页内容
        adapter.addContent(prePage, index + 1);
        // 交换顺序
        View temp = currPage;
        currPage = nextPage;
        nextPage = prePage;
        prePage = temp;
        currPageLeft = 0;
    }

    Handler updateHandler = new Handler()
    {

        @Override
        public void handleMessage(Message msg)
        {
            //状态 = 移动才向下执行， 下面的操作不断改变属性， requestLayout 实现动画。
            if (state != STATE_MOVE)
                return;

            // 移动页面
            // 翻回，先判断当前哪一页处于未返回状态
            if (prePageLeft > -mWidth && speed <= 0)
            {
                // 前一页处于未返回状态
                moveLeft(PRE);
            } else if (currPageLeft < 0 && speed >= 0)
            {
                // 当前页处于未返回状态
                moveRight(CURR);
            } else if (speed < 0 && index < adapter.getCount())  //不是上一页返回，else 就是当前页左翻
            {
                // 向左翻，翻动的是当前页
                moveLeft(CURR);
                if (currPageLeft == (-mWidth)) //完成左翻后。
                {
                    index++; //页码加一
                    // 翻过一页，在底下添加一页，把最上层页面移除
                    addNextPage(); //添加新一页
                }
            } else if (speed > 0 && index > 1)
            {
                // 向右翻，翻动的是前一页
                moveRight(PRE);
                if (prePageLeft == 0) //完成右翻页
                {
                    index--; //页码减一
                    // 翻回一页，添加一页在最上层，隐藏在最左边
                    addPrePage();
                }
            }
            if (right == 0 || right == mWidth)
            {
                releaseMoving();
                state = STATE_STOP;
                quitMove();
            }
            requestLayout(); //重新放置 子view
        }

    };

    public ScanView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    public ScanView(Context context)
    {
        super(context);
        init();
    }

    public ScanView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    /**
     * 退出动画翻页
     * //取消task 就停止发送 handler 消息
     */
    public void quitMove()
    {
        if (mTask != null)
        {
            mTask.cancel();
            mTask = null;
        }
    }

    /**
     * 初始化计时器
     */
    private void init()
    {
        index = 1;
        timer = new Timer();
        mTask = new MyTimerTask(updateHandler);

        mDetector = new GestureDetectorCompat(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                Log.d(TAG, "onDown"); //按下
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                Log.d(TAG, "onShowPress");
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d(TAG, "onSingleTapUp");
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.d(TAG, "onScroll " + "disx:" + distanceX + " disy:" + distanceY);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(TAG, "onLongPress");
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.d(TAG, "onFling:" + " velx:" + velocityX + " vely:" + velocityY);
                return true;
            }
        });
    }

    /**
     * 释放动作，不限制手滑动方向
     */
    private void releaseMoving()
    {
        isPreMoving = true;
        isCurrMoving = true;
    }

    float downX;
    float downY;

    @Override
    public boolean performClick() {
        Log.d(TAG, "performClick");  //没有执行。
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");
        downX = event.getX();
        downY = event.getY();
        if (mDetector.onTouchEvent(event)) {
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(TAG, "onInterceptTouchEvent");
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        Log.d(TAG, "dispatchTouchEvent");

        if (adapter != null)
            switch (event.getActionMasked())
            {
                case MotionEvent.ACTION_DOWN:
                    lastX = event.getX();
                    try
                    {
                        if (vt == null)
                        {
                            vt = VelocityTracker.obtain();
                        } else
                        {
                            vt.clear();
                        }
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    vt.addMovement(event);
                    mEvents = 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_POINTER_UP:
                    mEvents = -1;
                    break;
                case MotionEvent.ACTION_MOVE:
                    // 取消动画
                    quitMove();
//                    Log.d("index", "mEvents = " + mEvents + ", isPreMoving = "
//                            + isPreMoving + ", isCurrMoving = " + isCurrMoving);
                    vt.addMovement(event);
                    vt.computeCurrentVelocity(500);
                    speed = vt.getXVelocity(); //移动的时候不断计算速率
                    moveLenght = event.getX() - lastX;
                    /*
                    1.不是多点触碰
                    2.上一页正在移动
                    3.当前页不在移动或者向右滑
                    进入上一页移动操作， 上一页随手指移动
                     */
                    if ((moveLenght > 0 || !isCurrMoving) && isPreMoving
                            && mEvents == 0)
                    {
                        isPreMoving = true;
                        isCurrMoving = false;
                        if (index == 1) //没有上一页了
                        {
                            // 第一页不能再往右翻，跳转到前一个activity
                            state = STATE_MOVE;
                            releaseMoving();
                        } else
                        {
                            // 非第一页
                            prePageLeft += (int) moveLenght; //上一页随着你移动
                            // 防止滑过边界
                            if (prePageLeft > 0)
                                prePageLeft = 0;
                            else if (prePageLeft < -mWidth)
                            {
                                // 边界判断，释放动作，防止来回滑动导致滑动前一页时当前页无法滑动
                                prePageLeft = -mWidth;
                                releaseMoving();
                            }
                            right = mWidth + prePageLeft;
                            state = STATE_MOVE; //突然释放就执行动画
                        }
                        /*
                        下一页操作, 当前页随手指移动
                        1. 向左或者上一页false
                         */
                    } else if ((moveLenght < 0 || !isPreMoving) && isCurrMoving
                            && mEvents == 0)
                    {
                        isPreMoving = false;
                        isCurrMoving = true;
                        if (index == adapter.getCount())
                        {
                            // 最后一页不能再往左翻
                            state = STATE_STOP;
                            releaseMoving();
                        } else
                        {
                            currPageLeft += (int) moveLenght;
                            // 防止滑过边界
                            if (currPageLeft < -mWidth)
                                currPageLeft = -mWidth;
                            else if (currPageLeft > 0)
                            {
                                // 边界判断，释放动作，防止来回滑动导致滑动当前页是前一页无法滑动
                                currPageLeft = 0;
                                releaseMoving();
                            }
                            right = mWidth + currPageLeft;
                            state = STATE_MOVE;
                        }

                    } else
                        mEvents = 0;
                    lastX = event.getX();
                    requestLayout();
                    break;
                case MotionEvent.ACTION_UP:
                    //speed 忽略小移动
                    if (Math.abs(speed) < speed_shake)
                        speed = 0;
                    quitMove(); //停止动画
                    //执行新动画
                    mTask = new MyTimerTask(updateHandler);
                    timer.schedule(mTask, 0, 5);
                    //回收vt
                    try
                    {
                        vt.clear();
                        vt.recycle();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        return super.dispatchTouchEvent(event);
//        return true;
    }



    /*
     * （非 Javadoc） 在这里绘制翻页阴影效果
     *  绘制翻页阴影
     *
     * @see android.view.ViewGroup#dispatchDraw(android.graphics.Canvas)
     */
    @Override
    protected void dispatchDraw(Canvas canvas)
    {
        super.dispatchDraw(canvas); //绘制完成后， 最后加个阴影
        if (right == 0 || right == mWidth)
            return;
        RectF rectF = new RectF(right, 0, mWidth, mHeight);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        //阴影 36
        LinearGradient linearGradient = new LinearGradient(right, 0,
                right + 36, 0, 0xffbbbbbb, 0x00bbbbbb, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(rectF, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //测量之后，初始化宽高
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        if (isInit)
        {
            // 初始状态，一页放在左边隐藏起来，两页叠在一块
            prePageLeft = -mWidth;
            currPageLeft = 0;
            nextPageLeft = 0;
            isInit = false;
        }
    }

    /**
     * 根据 三个Left 布局三个页面位置
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        if (adapter == null)
            return;
        prePage.layout(prePageLeft, 0,
                prePageLeft + prePage.getMeasuredWidth(),
                prePage.getMeasuredHeight());
        currPage.layout(currPageLeft, 0,
                currPageLeft + currPage.getMeasuredWidth(),
                currPage.getMeasuredHeight());
        nextPage.layout(nextPageLeft, 0,
                nextPageLeft + nextPage.getMeasuredWidth(),
                nextPage.getMeasuredHeight());
        invalidate();
    }

    /**
     * 计时器，不断发送消息
     */
    class MyTimerTask extends TimerTask
    {
        Handler handler;

        public MyTimerTask(Handler handler)
        {
            this.handler = handler;
        }

        @Override
        public void run()
        {
            handler.sendMessage(handler.obtainMessage());
        }

    }
}
