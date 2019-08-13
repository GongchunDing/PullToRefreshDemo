package com.xiaoding.pullrefreshbyviewgroup;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

class CustomAdapter extends BaseAdapter {
    Context mContext;
    ArrayList<String> mData;

    CustomAdapter(Context context,ArrayList<String> data) {
        mContext=context;
        mData=data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.listview_item, null);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.content_view);
            viewHolder.txtv_delete = (TextView) convertView.findViewById(R.id.delete_item_btn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textView.setText(mData.get(position));
        final int pos = position;
        viewHolder.txtv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, mData.get(pos) + "被删除了",
                        Toast.LENGTH_SHORT).show();
                mData.remove(pos);
                notifyDataSetChanged();
                mOnItemDeleteListener.onDeleteClick(true);

            }
        });
        return convertView;
    }

    class ViewHolder {
        public TextView textView;
        public TextView txtv_delete;
    }

    /**
     * 删除按钮的监听接口
     */
    public interface onItemDeleteListener {
        void onDeleteClick(boolean i);
    }

    private onItemDeleteListener mOnItemDeleteListener;

    public void setOnItemDeleteClickListener(onItemDeleteListener mOnItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener;
    }



}

