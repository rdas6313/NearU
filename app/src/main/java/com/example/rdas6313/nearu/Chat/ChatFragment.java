package com.example.rdas6313.nearu.Chat;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements View.OnClickListener ,ChildEventListener {


    private String currentUserid,chatUserid;

    private Button sendBtn;
    private EditText inputEditText;

    private android.support.v7.widget.Toolbar toolbar;

    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;

    private DatabaseReference chatRef;

    private static final String TAG = ChatFragment.class.getSimpleName();

    public ChatFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = root.findViewById(R.id.recycler_view);
        toolbar = (android.support.v7.widget.Toolbar)root.findViewById(R.id.toolbar);
        inputEditText = (EditText)root.findViewById(R.id.chatinput);
        sendBtn = (Button)root.findViewById(R.id.chatSendBtn);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle == null || !bundle.containsKey(getString(R.string.CURRENT_USER_ID)) || !bundle.containsKey(getString(R.string.CHAT_USER_ID)))
            getActivity().getSupportFragmentManager().popBackStack();
        currentUserid = bundle.getString(getString(R.string.CURRENT_USER_ID));
        chatUserid = bundle.getString(getString(R.string.CHAT_USER_ID));
        init();
        loadMessages();
    }

    private void init(){
        if(recyclerView == null)
            return;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerAdapter(currentUserid);
        recyclerView.setAdapter(adapter);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        sendBtn.setOnClickListener(this);
        Utility utility = Utility.getInstance();
        chatRef = FirebaseDatabase.getInstance().getReference(getString(R.string.USER_CHAT_REF)+"/"+utility.getChatUserId(currentUserid,chatUserid));

    }

    @Override
    public void onClick(View v) {
        Utility utility = Utility.getInstance();
        String msg = inputEditText.getText().toString();
        if(!utility.isMsgOk(msg)){
            return;
        }
        HashMap<String,Object>data = new HashMap<>();
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

    private void loadMessages(){
        if(chatRef == null)
            return;
        chatRef.addChildEventListener(this);
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Log.d(TAG,dataSnapshot.toString());
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
    public void onStart() {
        super.onStart();
        loadMessages();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(chatRef != null)
            chatRef.removeEventListener(this);
    }


}
