package com.example.rdas6313.nearu;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatThreads extends Fragment implements ChildEventListener {

    private DatabaseReference threadRef;
    private RecyclerView recyclerView;
    private ThreadsAdapter threadsAdapter;
    private Query threadQuery;

    private Toolbar toolbar;

    private int totalThreads = 0;

    private final static String TAG = ChatThreads.class.getSimpleName();

    public ChatThreads() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_chat_threads, container, false);
        recyclerView = (RecyclerView)root.findViewById(R.id.recycleView);
        toolbar = (Toolbar)root.findViewById(R.id.toolBar);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Utility utility = Utility.getInstance();
        threadRef = FirebaseDatabase.getInstance().getReference(getString(R.string.USER_THREADS)+utility.getUserId());
        threadQuery = threadRef.orderByChild(getString(R.string.CHAT_TIMESTAMP));
        threadsAdapter = new ThreadsAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(threadsAdapter);

        /*Setting Toolbar text and color here */
        toolbar.setTitle(R.string.TOOLBAR_TITLE);
        toolbar.setTitleTextColor(Color.WHITE);
    }

    private ThreadData makeThreadDataObject(DataSnapshot dataSnapshot){
        String name = dataSnapshot.child(getString(R.string.RECEIVER_NAME)).getValue(String.class);
        String senderId = dataSnapshot.child(getString(R.string.SENDER_ID)).getValue(String.class);
        String receiverId = dataSnapshot.child(getString(R.string.RECEIVER_ID)).getValue(String.class);
        String msg = dataSnapshot.child(getString(R.string.CHAT_MSG)).getValue(String.class);
        String key = dataSnapshot.getKey();
        long timestamp = dataSnapshot.child(getString(R.string.CHAT_TIMESTAMP)).getValue(Long.class);

        ThreadData threadData = new ThreadData(key,msg,receiverId,senderId,name,timestamp);
        return threadData;
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        if(dataSnapshot.getValue() == null || totalThreads>0) {
            if(totalThreads > 0)
                totalThreads-=1;
            return;
        }
        ThreadData threadData = makeThreadDataObject(dataSnapshot);
        if(threadsAdapter != null)
            threadsAdapter.add(threadData);

    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        if(dataSnapshot.getValue() == null)
            return;
        ThreadData threadData = makeThreadDataObject(dataSnapshot);
        if(threadsAdapter != null)
            threadsAdapter.changeData(threadData);
    }

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
       if(threadQuery != null)
           threadQuery.addChildEventListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(threadQuery != null)
            threadQuery.removeEventListener(this);
        if(threadsAdapter != null)
            totalThreads = threadsAdapter.getItemCount();
    }
}
