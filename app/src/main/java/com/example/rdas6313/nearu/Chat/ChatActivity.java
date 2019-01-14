package com.example.rdas6313.nearu.Chat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.rdas6313.nearu.R;
import com.example.rdas6313.nearu.Utility;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener,ChildEventListener,View.OnLayoutChangeListener {


    private String currentUserid,chatUserid,chatUsername;

    private Button sendBtn;
    private EditText inputEditText;

    private android.support.v7.widget.Toolbar toolbar;

    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;

    private DatabaseReference chatRef;

    private boolean registerListenerAgain = false;

    private static final String TAG = ChatActivity.class.getSimpleName();

    private long totalMsg = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ActionBar actionBar = getSupportActionBar();
        Intent intent = getIntent();
        if(intent == null || intent.getExtras() == null)
            return;
        Bundle bundle = intent.getExtras();
        if(!bundle.containsKey(getString(R.string.CURRENT_USER_ID)) || !bundle.containsKey(getString(R.string.CHAT_USER_ID)))
            return;
        currentUserid = bundle.getString(getString(R.string.CURRENT_USER_ID));
        chatUserid = bundle.getString(getString(R.string.CHAT_USER_ID));
        chatUsername = bundle.getString(getString(R.string.CHAT_USER_NAME));
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(chatUsername);
        }
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
        recyclerView.addOnLayoutChangeListener(this);
        sendBtn.setOnClickListener(this);
        Utility utility = Utility.getInstance();
        chatRef = FirebaseDatabase.getInstance().getReference(getString(R.string.USER_CHAT_REF)+"/"+utility.getChatUserId(currentUserid,chatUserid));

    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if(bottom < oldBottom){
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount()-1);
                }
            },100);
        }
    }

    private void markMsgAsSeen(){
        String loggedInUserPath = getString(R.string.USER_THREADS)+currentUserid+"/"+chatUserid+"/";
        FirebaseDatabase.getInstance().getReference(loggedInUserPath)
                .child(getString(R.string.CHAT_SEEN)).setValue(Boolean.TRUE);
    }

    private void loadMessages(){
        if(chatRef == null)
            return;
        chatRef.addChildEventListener(this);
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        if(dataSnapshot.getValue() == null || totalMsg>0) {
            totalMsg--;
            return;
        }
        Log.e(TAG,dataSnapshot.toString());
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
        String msg = inputEditText.getText().toString();
        if(TextUtils.isEmpty(msg)){
            return;
        }
        inputEditText.getText().clear();
        sendChatDataToServer(msg);
    }

    private void sendChatDataToServer(String msg){
        if(chatRef == null || msg == null || msg.length() == 0)
            return;

        HashMap<String,Object> data = new HashMap<>();
        data.put(getString(R.string.RECEIVER_ID),chatUserid);
        data.put(getString(R.string.SENDER_ID),currentUserid);
        data.put(getString(R.string.RECEIVER_NAME),chatUsername);
        data.put(getString(R.string.CHAT_MSG),msg.trim());
        data.put(getString(R.string.CHAT_TIMESTAMP),ServerValue.TIMESTAMP);
        data.put(getString(R.string.CHAT_SEEN),false);

        String senderPath = getString(R.string.USER_THREADS)+currentUserid+"/"+chatUserid+"/";
        String receiverPath = getString(R.string.USER_THREADS)+chatUserid+"/"+currentUserid+"/";
        String chatPath = chatRef.getPath().toString()+"/"+chatRef.push().getKey();
        HashMap<String,Object>updateUserChatPath = new HashMap<>();
        updateUserChatPath.put(senderPath,data);
        updateUserChatPath.put(receiverPath,data);
        updateUserChatPath.put(chatPath,data);
        FirebaseDatabase.getInstance().getReference().updateChildren(updateUserChatPath, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null){
                    Log.e(TAG, databaseError.getDetails());
                    Toast.makeText(ChatActivity.this,"Unable to send Message",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        markMsgAsSeen();

        if(chatRef != null)
            chatRef.removeEventListener(this);

        if(recyclerView != null)
            recyclerView.removeOnLayoutChangeListener(this);

        registerListenerAgain = true;
        totalMsg = adapter.getItemCount();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(registerListenerAgain){
            if(chatRef != null)
                chatRef.addChildEventListener(this);
            if(recyclerView != null)
                recyclerView.addOnLayoutChangeListener(this);
            registerListenerAgain = false;
        }
    }
}
