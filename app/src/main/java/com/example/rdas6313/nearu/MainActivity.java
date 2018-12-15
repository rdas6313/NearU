package com.example.rdas6313.nearu;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private MapFragment mapFragment;
    private FrndsFragment frndsFragment;
    private ProfileFragment profileFragment;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }

    private void init(){
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        mapFragment = new MapFragment();
        frndsFragment = new FrndsFragment();
        profileFragment = new ProfileFragment();
        loadFragments(mapFragment);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.location:
                loadFragments(mapFragment);
                return true;
            case R.id.frnds:
                loadFragments(frndsFragment);
                return true;
            case R.id.profile:
                loadFragments(profileFragment);
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
}
