package com.example.rdas6313.nearu;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.example.rdas6313.nearu.Chat.ChatActivity;
import com.example.rdas6313.nearu.Map.FragmentCallback;
import com.example.rdas6313.nearu.Map.MapFragment;
import com.example.rdas6313.nearu.MessageThreads.ChatThreads;
import com.example.rdas6313.nearu.SignUp.SignUpActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements FragmentCallback {


    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        logUserToFabric();
    }

    private void init(){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container,new MapFragment())
                .commit();
    }


    @Override
    public void onLoadRecentChatThreads() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container,new ChatThreads())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onLoadSettings() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container,new SettingsFragment())
                .addToBackStack(null)
                .commit();
    }

    private void startSignUpActivity(){
        Intent intent = new Intent(this,SignUpActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        Utility utility = Utility.getInstance();
        if(!utility.isUserLoggedIn()){
            Log.e(TAG,"Invalid User");
            startSignUpActivity();
            finish();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void logUserToFabric() {
        // TODO: Use the current user's information
        // You can call any combination of these three methods
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null)
            return;
        Crashlytics.setUserIdentifier(user.getUid());
        Crashlytics.setUserName(user.getDisplayName());
    }

    @Override
    public void onChatBtnCliked(String currentUserid, String chatUserid,String chatUsername) {
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.CURRENT_USER_ID),currentUserid);
        bundle.putString(getString(R.string.CHAT_USER_ID),chatUserid);
        bundle.putString(getString(R.string.CHAT_USER_NAME),chatUsername);
        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
