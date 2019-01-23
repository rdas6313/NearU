package com.example.rdas6313.nearu.MessageThreads;

import android.content.Context;
import android.support.annotation.NonNull;

import com.example.rdas6313.nearu.R;
import com.example.rdas6313.nearu.Utility;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ChatModel {

    private static ChatModel chatModel;
    private ChatModelContract mChatModelContract;

    private ChatModel(ChatModelContract chatModelContract){
        mChatModelContract = chatModelContract;
    }

    public static ChatModel getInstance(ChatModelContract chatModelContract){
        if(chatModel == null)
            chatModel = new ChatModel(chatModelContract);
        return chatModel;
    }

    public void setListener(ChatModelContract chatModelContract){
        mChatModelContract = chatModelContract;
    }

    public void doesChatThreadsHasData(Context context){
        Utility utility = Utility.getInstance();
        if(!utility.isUserLoggedIn()) {
            if(mChatModelContract != null)
                mChatModelContract.OnChatThreadsHasData(false);
            return;
        }
        DatabaseReference threadRef = FirebaseDatabase.getInstance().getReference(context.getString(R.string.USER_THREADS)+utility.getUserId());
        Query threadQuery = threadRef.orderByChild(context.getString(R.string.CHAT_TIMESTAMP));
        threadQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean hasData = (dataSnapshot.getValue() == null)? false:true;
                if(mChatModelContract != null)
                    mChatModelContract.OnChatThreadsHasData(hasData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(mChatModelContract != null)
                    mChatModelContract.OnChatThreadsHasData(false);

            }
        });

    }

}
