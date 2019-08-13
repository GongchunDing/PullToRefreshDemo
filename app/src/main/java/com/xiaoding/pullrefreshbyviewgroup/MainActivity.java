package com.xiaoding.pullrefreshbyviewgroup;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private PullToRefresh pullToRefresh;

    SlidingListView listView;
    CustomAdapter mAdapter;
    private ArrayList<String> mDataList = new ArrayList<String>() {
        {
            for (int i = 0; i < 50; i++) {
                add("ListView item  " + i);
            }
        }
    };
    Handler handler = new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        pullToRefresh =  findViewById(R.id.refreshable_view);
        pullToRefresh.setBackgroundColor(Color.GRAY);
        listView =  findViewById(R.id.list_view);
        mAdapter = new CustomAdapter(this, mDataList);

        listView.setAdapter(mAdapter);

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                listView.turnNormal();
////                Toast.makeText(MainActivity.this, mDataList.get(position) + "被点击了", Toast.LENGTH_SHORT).show();
//                }
//            });


        mAdapter.setOnItemDeleteClickListener(new CustomAdapter.onItemDeleteListener() {
            @Override
            public void onDeleteClick(boolean i) {
                listView.turnNormal();
            }
        });


        pullToRefresh.setOnRefreshListener(new PullToRefresh.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullToRefresh.finishAction();
                    }
                }, 3000);

            }
        }, 0);


    }
}
