package com.example.rdas6313.nearu;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class User {

    private String name;
    private LatLng location;
    private boolean isOnline;
    private String id;
    private Marker marker;


    private Context mContext;

    private static final String TAG = User.class.getSimpleName();

    public User(String user_id, LatLng currentLocation, Context context){
        id = user_id;
        location = currentLocation;
        mContext = context;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setLocation(LatLng currentLocation){
        location = currentLocation;
    }

    public void setOnline(boolean online){
        isOnline = online;
    }

    public void setUserId(String user_id){
        id = user_id;
    }

    public void setMarker(Marker marker){
        this.marker = marker;
    }

    public Marker getMarker(){
        return marker;
    }

    public String getName(){
        return name;
    }

    public LatLng getLocation(){
        return location;
    }

    public boolean isOnline(){
        return isOnline;
    }

    public String getId(){
        return id;
    }

}
