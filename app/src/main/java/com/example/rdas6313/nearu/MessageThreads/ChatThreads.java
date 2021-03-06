package com.example.rdas6313.nearu.MessageThreads;


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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rdas6313.nearu.Map.FragmentCallback;
import com.example.rdas6313.nearu.R;
import com.example.rdas6313.nearu.Utility;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatThreads extends Fragment implements ChildEventListener,ThreadsClickListener,ChatModelContract{

    private DatabaseReference threadRef;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ThreadsAdapter threadsAdapter;
    private Query threadQuery;

    private Toolbar toolbar;

    private TextView errorView;

    private FragmentCallback fragmentCallback;
    private final static String TAG = ChatThreads.class.getSimpleName();

    private ChatModel chatModel;

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
        progressBar = (ProgressBar)root.findViewById(R.id.recyclerviewProgressbar);
        errorView = (TextView)root.findViewById(R.id.errorView);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fragmentCallback = (FragmentCallback) getActivity();

        Utility utility = Utility.getInstance();


        /*Setting Toolbar text and color here */
        errorView.setText(getString(R.string.CHAT_THREAD_ERROR_MSG));
        errorView.setVisibility(View.GONE);
        toolbar.setTitle(R.string.TOOLBAR_TITLE);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        threadsAdapter = new ThreadsAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(threadsAdapter);
        if(!utility.checkInternet(getContext())){
            Toast.makeText(getContext(),R.string.No_INTERNET_MSG,Toast.LENGTH_SHORT).show();
            return;
        }

        threadRef = FirebaseDatabase.getInstance().getReference(getString(R.string.USER_THREADS)+utility.getUserId());
        threadQuery = threadRef.orderByChild(getString(R.string.CHAT_TIMESTAMP));
        chatModel = ChatModel.getInstance(this);
    }

    @Override
    public void onChatItemClick(int pos) {
        Utility utility = Utility.getInstance();
        if(threadsAdapter == null || !utility.isUserLoggedIn())
            return;
        ThreadData threadData = threadsAdapter.getThreadDataFromPos(pos);
        if(fragmentCallback != null)
            fragmentCallback.onChatBtnCliked(utility.getUserId(),threadData.getKey(),threadData.getReceiverName());
    }

    private ThreadData makeThreadDataObject(DataSnapshot dataSnapshot){
        String name = dataSnapshot.child(getString(R.string.RECEIVER_NAME)).getValue(String.class);
        String senderId = dataSnapshot.child(getString(R.string.SENDER_ID)).getValue(String.class);
        String receiverId = dataSnapshot.child(getString(R.string.RECEIVER_ID)).getValue(String.class);
        String msg = dataSnapshot.child(getString(R.string.CHAT_MSG)).getValue(String.class);
        String key = dataSnapshot.getKey();
        long timestamp = dataSnapshot.child(getString(R.string.CHAT_TIMESTAMP)).getValue(Long.class);
        boolean is_msg_seen = dataSnapshot.child(getString(R.string.CHAT_MSG_SEEN_KEY)).getValue(Boolean.class);

        ThreadData threadData = new ThreadData(key,msg,receiverId,senderId,name,timestamp,is_msg_seen);
        return threadData;
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
    //    Log.d(TAG,"Child Added");

        if(progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }else if(errorView.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        if(dataSnapshot.getValue() == null) {
            return;
        }

        ThreadData threadData = makeThreadDataObject(dataSnapshot);
        if(threadsAdapter != null)
            threadsAdapter.add(threadData);

    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
    //    Log.d(TAG,"Child Changed");
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
    public void OnChatThreadsHasData(boolean hasData) {
        if(!hasData){
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            errorView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if(chatModel != null) {
            chatModel.setListener(this);
            chatModel.doesChatThreadsHasData(getContext());
        }
        if(threadQuery != null)
           threadQuery.addChildEventListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(chatModel != null)
            chatModel.setListener(null);
        if(threadQuery != null)
            threadQuery.removeEventListener(this);
        if(threadsAdapter != null)
            threadsAdapter.clearData();
    }
}
