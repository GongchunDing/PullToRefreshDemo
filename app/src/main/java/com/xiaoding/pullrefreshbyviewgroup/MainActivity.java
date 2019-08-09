package com.xiaoding.pullrefreshbyviewgroup;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private PullToRefresh pullToRefresh;

    ListView listView;
    ArrayAdapter<String> adapter;
    String[] items = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L" };
    Handler handler = new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        pullToRefresh = (PullToRefresh) findViewById(R.id.refreshable_view);
        pullToRefresh.setBackgroundColor(Color.RED);
        listView = (ListView) findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

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
