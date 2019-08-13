package com.xiaoding.pullrefreshbyviewgroup;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;

public class SlidingListView extends ListView {

    private int xDown;
    private int yDown;
    private int xMove;
    private int yMove;
    private int mScreenWidth;
    private boolean isDeleteShow = false;
    private ViewGroup mPointChild;
    private int mDeleteWidth;
    private int mPointPosition;
    private int toushSlop;
    private LinearLayout.LayoutParams mItemLayoutParams;//手指按下时所在的item的布局参数

    public SlidingListView(Context context){
        super(context);
    }

    public SlidingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        toushSlop = ViewConfiguration.get(context).getScaledTouchSlop();//触发移动事件的最小距离

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {//事件响应
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                performActionDown(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                performActionMove(ev);
                break;
            case MotionEvent.ACTION_UP:
                performActionUp(ev);
                setEnabled(true);
                break;
        }

        return super.onTouchEvent(ev);
    }

    private void performActionDown(MotionEvent ev) {
        xDown = (int) ev.getX();
        yDown = (int) ev.getY();
        if (isDeleteShow) {
            ViewGroup tmpViewGroup = (ViewGroup) getChildAt(pointToPosition(xDown, yDown) - getFirstVisiblePosition());
            //pointToPosition依据触摸点的坐标计算出点击的是ListView的哪个Item,减去当前可见的顶部的item位置
            //getChildAt单独获取某个view进行修改
            if (!mPointChild.equals(tmpViewGroup)) {
                turnNormal();
            }
        }
        if(pointToPosition(xDown,yDown)!=-1){
            mPointChild = (ViewGroup) getChildAt(pointToPosition(xDown, yDown) - getFirstVisiblePosition());
            mDeleteWidth = mPointChild.getChildAt(1).getLayoutParams().width;
            mItemLayoutParams = (LinearLayout.LayoutParams) mPointChild.getChildAt(0).getLayoutParams();
        }
    }

    private boolean performActionMove(MotionEvent ev) {
        int nowX = (int) ev.getX();
        int nowY = (int) ev.getY();
        int diffX = nowX - xDown;
        if (Math.abs(diffX) > Math.abs(nowY - yDown)) {
            if (!isDeleteShow && nowX < xDown) {//删除按钮未显示&&向左滑
                if (-diffX >= mDeleteWidth) {//如果滑动距离大于删除组件的宽度
                    diffX = -mDeleteWidth;
                }
                mItemLayoutParams.leftMargin = diffX;
                mPointChild.getChildAt(0).setLayoutParams(mItemLayoutParams);
            } else if (isDeleteShow && nowX > xDown) {//删除按钮显示&&向右滑
                if (diffX >= mDeleteWidth) {
                    diffX = mDeleteWidth;
                }
                mItemLayoutParams.leftMargin = diffX - mDeleteWidth;
                mPointChild.getChildAt(0).setLayoutParams(mItemLayoutParams);
            }
            this.getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }
        return true;
    }

    private void performActionUp(MotionEvent ev) {
        Log.d("ListView","up事件"+-mItemLayoutParams.leftMargin+"宽/2="+mDeleteWidth / 2);
        if (-mItemLayoutParams.leftMargin >= mDeleteWidth / 2) {
            mItemLayoutParams.leftMargin = -mDeleteWidth;
            mPointChild.getChildAt(0).setLayoutParams(mItemLayoutParams);
            isDeleteShow = true;
        } else
            turnNormal();
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {//事件拦截
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                setEnabled(true);
                //侧滑删除
                xDown = (int) ev.getX();
                yDown = (int) ev.getY();
                mPointPosition = pointToPosition(xDown, yDown);
                if (mPointPosition != -1) {
                    if (isDeleteShow) {
                        ViewGroup tmpViewGroup = (ViewGroup) getChildAt(mPointPosition - getFirstVisiblePosition());
                        if (!mPointChild.equals(tmpViewGroup)) {
                            turnNormal();
                        }
                    }
                    //获取当前的item
                    mPointChild = (ViewGroup) getChildAt(mPointPosition - getFirstVisiblePosition());

                    mDeleteWidth = mPointChild.getChildAt(1).getLayoutParams().width;
                    mItemLayoutParams = (LinearLayout.LayoutParams) mPointChild.getChildAt(0).getLayoutParams();

                    mItemLayoutParams.width = mScreenWidth;//为什么不能再布局文件里设置matchParent
                    mPointChild.getChildAt(0).setLayoutParams(mItemLayoutParams);

                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                xMove= (int) ev.getX();
                yMove= (int) ev.getY();
                int nowX = (int) ev.getX();
                int nowY = (int) ev.getY();
                int diffX = nowX - xDown;
                if (Math.abs(diffX) > toushSlop&&Math.abs(yDown-yMove)<20) {
                    setEnabled(false);
                    return true;//避免子布局中有点击的控件时滑动无效
                }
                break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }


    public void turnNormal() {
        mItemLayoutParams.leftMargin = 0;
        mPointChild.getChildAt(0).setLayoutParams(mItemLayoutParams);
        isDeleteShow = false;
        Log.d("ListView","turnNormal()"+mItemLayoutParams.leftMargin);
    }

}