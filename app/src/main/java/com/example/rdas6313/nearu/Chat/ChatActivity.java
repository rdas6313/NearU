package com.example.rdas6313.nearu.Chat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.rdas6313.nearu.R;
import com.example.rdas6313.nearu.Utility;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener,ChildEventListener {


    private String currentUserid,chatUserid;

    private Button sendBtn;
    private EditText inputEditText;

    private android.support.v7.widget.Toolbar toolbar;

    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;

    private DatabaseReference chatRef;

    private static final String TAG = ChatActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        if(intent == null || intent.getExtras() == null)
            return;
        Bundle bundle = intent.getExtras();
        if(!bundle.containsKey(getString(R.string.CURRENT_USER_ID)) || !bundle.containsKey(getString(R.string.CHAT_USER_ID)))
            return;
        currentUserid = bundle.getString(getString(R.string.CURRENT_USER_ID));
        chatUserid = bundle.getString(getString(R.string.CHAT_USER_ID));
        init();
        loadMessages();
    }

    private void init(){
        recyclerView = findViewById(R.id.recycler_view);
        inputEditText = (EditText)findViewById(R.id.chatinput);
        sendBtn = (Button)findViewById(R.id.chatSendBtn);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerAdapter(currentUserid,recyclerView);
        recyclerView.setAdapter(adapter);
        sendBtn.setOnClickListener(this);
        Utility utility = Utility.getInstance();
        chatRef = FirebaseDatabase.getInstance().getReference(getString(R.string.USER_CHAT_REF)+"/"+utility.getChatUserId(currentUserid,chatUserid));

    }

    private void loadMessages(){
        if(chatRef == null)
            return;
        chatRef.addChildEventListener(this);
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Log.e(TAG,dataSnapshot.toString());
        if(dataSnapshot.getValue() == null)
            return;
        String receiverId = dataSnapshot.child(getString(R.string.RECEIVER_ID)).getValue(String.class);
        String senderId = dataSnapshot.child(getString(R.string.SENDER_ID)).getValue(String.class);
        String msg = dataSnapshot.child(getString(R.string.CHAT_MSG)).getValue(String.class);
        long timestamp = dataSnapshot.child(getString(R.string.CHAT_TIMESTAMP)).getValue(Long.class);
        Message message = new Message(receiverId,senderId,msg,timestamp);
        adapter.addMessage(message);
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Log.e(TAG,databaseError.getDetails());
    }

    @Override
    public void onClick(View v) {
        Utility utility = Utility.getInstance();
        String msg = inputEditText.getText().toString();
        if(!utility.isMsgOk(msg)){
            return;
        }
        HashMap<String,Object> data = new HashMap<>();
        data.put(getString(R.string.RECEIVER_ID),chatUserid);
        data.put(getString(R.string.SENDER_ID),currentUserid);
        data.put(getString(R.string.CHAT_MSG),msg.trim());
        data.put(getString(R.string.CHAT_TIMESTAMP),ServerValue.TIMESTAMP);
        chatRef.push().updateChildren(data, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null)
                    Log.e(TAG,databaseError.getDetails());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(chatRef != null)
            chatRef.removeEventListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
