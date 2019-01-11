package com.example.rdas6313.nearu.Chat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rdas6313.nearu.R;
import com.example.rdas6313.nearu.Utility;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {

    private String loggedInUserId;
    private ArrayList<Message>messages;

    private final int LEFT_VIEW = 0;
    private final int RIGHT_VIEW = 1;

    private RecyclerView recyclerView;

    private final String TAG = RecyclerAdapter.class.getName();

    public RecyclerAdapter(String loggedInUserId,RecyclerView recyclerView){
        messages = new ArrayList<>();
        this.loggedInUserId = loggedInUserId;
        this.recyclerView = recyclerView;
    }

    public void addMessage(Message message){
        if(messages == null)
            return;
        messages.add(message);
        notifyItemInserted(messages.size()-1);
        recyclerView.scrollToPosition(messages.size()-1);
    }

    public void addMessages(ArrayList<Message> messagelist){
        if(messages == null)
            return;
        messages.addAll(messagelist);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if(messages == null)
            return -1;
        Message message = messages.get(position);
        if(message == null)
            return -1;
        Log.d(TAG,message.getSenderId()+" "+loggedInUserId);
        if(message.getSenderId().equals(loggedInUserId))
            return RIGHT_VIEW;
        else
            return LEFT_VIEW;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View root = null;
        Log.d(TAG,"View type "+viewType);
        if(viewType == RIGHT_VIEW)
            root = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.msg_sender_layout,viewGroup,false);
        else if(viewType == LEFT_VIEW)
            root = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.msg_reciver_layout,viewGroup,false);
        else
            return null;

        RecyclerViewHolder viewHolder = new RecyclerViewHolder(root);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder recyclerViewHolder, int pos) {
        if(messages == null || pos < 0 || pos >= messages.size())
            return;
        Message message = messages.get(pos);
        recyclerViewHolder.setData(message);
    }

    @Override
    public int getItemCount() {
        if(messages == null)
            return 0;
        return messages.size();
    }


    public static class RecyclerViewHolder extends RecyclerView.ViewHolder{

        private TextView msgView,timeAndDateView;

        public RecyclerViewHolder(View v){
            super(v);
            msgView = (TextView)v.findViewById(R.id.msg);
            timeAndDateView = (TextView)v.findViewById(R.id.timeAndDate);
        }

        public void setData(Message message){
            if(message == null)
                return;
            msgView.setText(message.getMsg());
            Utility utility = Utility.getInstance();
            timeAndDateView.setText(utility.getDateFromTimeStamp(message.getTimestamp()));
        }

    }
}
