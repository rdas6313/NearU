package com.example.rdas6313.nearu;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ThreadsAdapter extends RecyclerView.Adapter<ThreadsAdapter.ThreadsViewHolder> {

    private static final String TAG = ThreadsAdapter.class.getName();
    private ArrayList<ThreadData>data;

    public ThreadsAdapter(){
        data = new ArrayList<>();
    }

    public void add(ThreadData threadData){
        if(data == null)
            return;
        data.add(0,threadData);
        notifyDataSetChanged();
    }

    private int getPosFromKey(String key){
        int i = -1;
        for(i=0;i<data.size();i++){
            ThreadData threadData = data.get(i);
            if(threadData.getKey().equals(key))
                return i;
        }
        return -1;
    }

    public void changeData(ThreadData newThreadData){
        if(data == null || data.size() == 0 || newThreadData == null)
            return;

        int pos = getPosFromKey(newThreadData.getKey());
        if(pos == -1)
            return;
        data.remove(pos);
        add(newThreadData);
    }


    @NonNull
    @Override
    public ThreadsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View root = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.threads_item_layout,viewGroup,false);
        ThreadsViewHolder viewHolder = new ThreadsViewHolder(root);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ThreadsViewHolder threadsViewHolder, int i) {
        if(data == null || i < 0 || i >= data.size())
            return;
        ThreadData threadData = (ThreadData) data.get(i);
        threadsViewHolder.setData(threadData);
    }

    @Override
    public int getItemCount() {
        if(data == null)
            return 0;
        return data.size();
    }

    public static class ThreadsViewHolder extends RecyclerView.ViewHolder{

        private TextView nameView,msgView,ImageTextView,dateView,timeView;

        public ThreadsViewHolder(View v){
            super(v);
            nameView = (TextView)v.findViewById(R.id.titleName);
            msgView = (TextView)v.findViewById(R.id.lastmsg);
            ImageTextView = (TextView)v.findViewById(R.id.ImageText);
            dateView = (TextView)v.findViewById(R.id.thread_date);
            timeView = (TextView)v.findViewById(R.id.thread_time);
        }

        public void setData(ThreadData threadData){
            if(threadData == null)
                return;
            Utility utility = Utility.getInstance();
            nameView.setText(threadData.getReceiverName());
            msgView.setText(threadData.getMsg());
            ImageTextView.setText(utility.getFirstLetterFromName(threadData.getReceiverName()));
            dateView.setText(utility.getDate(threadData.getTimestamp()));
            timeView.setText(utility.getTime(threadData.getTimestamp()));
        }
    }
}
