package com.xiaoding.pullrefreshbyviewgroup;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PullToRefresh extends ViewGroup {
    private int yDown;
    private int yMove;
    private int xDown;
    private int xMove;

    public static final int STATUS_PULL_TO_REFRESH = 0;//下拉状态
    public static final int STATUS_RELEASE_TO_REFRESH = 1;//释放立即刷新状态
    public static final int STATUS_REFRESHING = 2;//正在刷新状态
    public static final int STATUS_REFRESH_FINISHED = 3;//刷新结束或未刷新状态
    private int currentStatus = STATUS_REFRESH_FINISHED;
    private int lastStatus = currentStatus;
    private int toushSlop;
    private boolean isfirst = true;
    private int symbolline;
    private int lastmove = 0;
    private int mId = 0;

    private View view1;
    private View view2;

    private PullToRefreshListener mListener;
    private SharedPreferences preferences;
    private View header;
    private ImageView allow;
    private ProgressBar progressBar;
    private TextView descriptionTextView;
    private TextView updatedTimeView;
    private Scroller scroller;

    public PullToRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
        preferences = context.getSharedPreferences("update_at" + mId, Context.MODE_PRIVATE);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        header = layoutInflater.inflate(R.layout.pull_to_refresh, null);
        allow = (ImageView) header.findViewById(R.id.arrow);
        progressBar = (ProgressBar) header.findViewById(R.id.progress_bar);
        descriptionTextView = (TextView) header.findViewById(R.id.description);
        updatedTimeView = (TextView) header.findViewById(R.id.update_at);

        toushSlop = ViewConfiguration.get(context).getScaledTouchSlop();//触发移动事件的最小距离
        addView(header);
//        setOrientation(VERTICAL);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        view1 = getChildAt(0);
        view1.layout(0, -view1.getMeasuredHeight(), view1.getMeasuredWidth(), 0);
        view2 = getChildAt(1);
        view2.layout(0, 0, view2.getMeasuredWidth(), view2.getMeasuredHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (currentStatus == STATUS_REFRESHING)
            return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                yDown = (int) event.getY();
                return false;
            }
            case MotionEvent.ACTION_MOVE:
                yMove = (int) event.getRawY();
                //listview还能不能往上滑动
                boolean a = getChildAt(1).canScrollVertically(-1);
                if (yMove - yDown > toushSlop * 5 && !a ) {
                    //下拉拦截
                    Log.d("Refreshableview", "ydown:" + yDown + "ymove:" + yMove);
                    lastmove = yMove;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            default:
                return false;
        }
        return false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_MOVE:
                yMove = (int) motionEvent.getRawY();
                int scrolly = lastmove - yMove;
                Log.d("Refreshableview", "lastmove=" + lastmove + "ymove=" + yMove);
                if (currentStatus != STATUS_REFRESHING) {
                    symbolline += scrolly / 2;
                    scrollBy(0, (int) scrolly / 2);
                }
                if (Math.abs(symbolline) > getChildAt(0).getMeasuredHeight() && isfirst) {
                    currentStatus = STATUS_RELEASE_TO_REFRESH;
                } else {
                    currentStatus = STATUS_PULL_TO_REFRESH;
                }
                updateHeaderView();
                lastmove = yMove;
                break;
            case MotionEvent.ACTION_UP:
            default:
                Log.d("RefreshableView", "松开");
                if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
                    refreshAction();
                } else if (currentStatus == STATUS_PULL_TO_REFRESH) {
                    finishAction();
                }
                break;
        }
        updateHeaderView();
        return super.onTouchEvent(motionEvent);
    }

    private void updateHeaderView() {
        if (lastStatus != currentStatus) {
            if (currentStatus == STATUS_PULL_TO_REFRESH) {
                descriptionTextView.setText(getResources().getString(R.string.pull_to_refresh));
                allow.setVisibility(View.VISIBLE);
                allow.setImageResource(R.drawable.down);
                progressBar.setVisibility(View.GONE);
                allow.setImageResource(R.drawable.down);
            } else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
                descriptionTextView.setText(getResources().getString(R.string.release_to_refresh));
                allow.setVisibility(View.VISIBLE);
                allow.setImageResource(R.drawable.up);
                progressBar.setVisibility(View.GONE);
                allow.setImageResource(R.drawable.up);
            } else if (currentStatus == STATUS_REFRESHING) {
                descriptionTextView.setText(getResources().getString(R.string.refreshing));
                progressBar.setVisibility(View.VISIBLE);
                allow.setVisibility(View.GONE);
            }
            long updatedTime = preferences.getLong("update_at" + mId, -1);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
            Date date = new Date(updatedTime);
            String time = formatter.format(date);
            updatedTimeView.setText("上次更新于" + time);
        }
    }

    private void refreshAction() {
        scrollTo(0, getChildAt(0).getTop());
        currentStatus = STATUS_REFRESHING;
        updateHeaderView();
        symbolline = 0;
        isfirst = true;
        if (mListener != null)
            mListener.onRefresh();
    }

    public void finishAction() {
        Log.d("Refreshableview", "结束");
        currentStatus = STATUS_REFRESH_FINISHED;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("update_at" + mId, System.currentTimeMillis()).commit();
        scrollTo(0, 0);
        updateHeaderView();
        isfirst = true;

    }

    public interface PullToRefreshListener {
        void onRefresh();
    }

    public void setOnRefreshListener(PullToRefreshListener listener, int id) {
        mListener = listener;
        mId = id;
    }
}
