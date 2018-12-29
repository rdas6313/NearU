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
import com.example.rdas6313.nearu.SignUp.SignUpActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    private BottomNavigationView bottomNavigationView;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        logUserToFabric();
    }

    public void forceCrash() {
        throw new RuntimeException("This is a crash");
    }



    private void init(){
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        loadFragments(new MapFragment());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.location:
                loadFragments(new MapFragment());
                return true;
            case R.id.frnds:
                loadFragments(new FrndsFragment());
                return true;
            case R.id.profile:
                loadFragments(new ProfileFragment());
                return true;
            default:
                return false;
        }
    }


    private void loadFragments(Fragment fragment){
        if(fragment == null)
            return;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container,fragment)
                .commit();
    }

    private void startSignUpActivity(){
        Intent intent = new Intent(this,SignUpActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
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

}
