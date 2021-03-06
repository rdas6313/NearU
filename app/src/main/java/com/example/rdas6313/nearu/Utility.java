package com.example.rdas6313.nearu;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public final class Utility {

    private static Utility mObj;
    private final String SHARED_PREFERENCE_NAME = "settings_pref_file";
    private final int SHARED_PREFERENCE_DEFAULT_VALUE = 1;

    public static Utility getInstance(){
        if(mObj == null)
            mObj = new Utility();
        return mObj;
    }

    public boolean isUserLoggedIn(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null)
            return false;
        return true;
    }

    public String getUserId(){
        if(isUserLoggedIn()){
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }

    public Marker addMarkerToMap(LatLng latLng, String title, String snippet, MapboxMap map) {

        if (map == null)
            return null;

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snippet);

        Marker marker = map.addMarker(markerOptions);
        return marker;
    }

    public void removeMarkerFromMap(Marker marker,MapboxMap map){
        if(marker == null || map == null)
            return;
        map.removeMarker(marker);
    }

    public void addLatlngToList(LatLng latLng, ArrayList<LatLng>latLngList){
        if(latLngList == null)
            return;
        latLngList.add(latLng);
    }

    public void removeLatlngFromList(LatLng removeLatlng,ArrayList<LatLng>latlngList){
        if(latlngList == null)
            return;
        for (LatLng latlng:latlngList) {
            if(latlng.getLatitude() == removeLatlng.getLatitude() && latlng.getLongitude() == removeLatlng.getLongitude()){
                latlngList.remove(latlng);
            }
        }
    }

    public void updateLatlngFromList(int index,LatLng newLatlng,ArrayList<LatLng>latLngList){
        if(latLngList == null || newLatlng == null)
            return;
        latLngList.set(index,newLatlng);
    }

    public void updateLatlngFromList(LatLng olderLatlng,LatLng newLatlng,ArrayList<LatLng>latlngList){
        if(latlngList == null)
            return;
        removeLatlngFromList(olderLatlng,latlngList);
        addLatlngToList(newLatlng,latlngList);
    }

    public void adjustCameraZoomForMarkers(MapboxMap map,ArrayList<LatLng>latLngList,int camera_padding){
        if(map == null || latLngList == null || latLngList.size() ==0)
            return;
        else if(latLngList.size() == 1){
            moveCamera(latLngList.get(0),map,12,4000);
            return;
        }
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .includes(latLngList)
                .build();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,camera_padding));
    }

    private void moveCamera(LatLng latLng,MapboxMap map,double camera_zoom,int animation_duration) {
        if (map == null || latLng == null)
            return;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .tilt(20)
                .zoom(camera_zoom)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),animation_duration);
    }

    public String getChatUserId(String u1,String u2){
        if(u1.compareTo(u2) < 0)
            return u1+u2;
        else
            return u2+u1;
    }

    public boolean isMsgOk(String msg){
        if(msg == null || msg.length() == 0)
            return false;
        return true;
    }

    public String getDateFromTimeStamp(long timestamp){
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = calendar.getTimeZone();
        String pattern = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(timeZone);
        String serverDate = simpleDateFormat.format(new Date(timestamp));
        String todayDate = simpleDateFormat.format(calendar.getTime());
        if(!serverDate.equals(todayDate))
            pattern = "h:mm a, dd/MM/yyyy";
        else
            pattern = "h:mm a";
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(new Date(timestamp));
    }

    public String getFirstLetterFromName(String name){
        if(name == null || name.length() == 0)
            return null;
        return name.substring(0,1);
    }

    public String getDate(long timestamp){
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = calendar.getTimeZone();
        String pattern = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(timeZone);
        return simpleDateFormat.format(new Date(timestamp));
    }

    public String getTime(long timestamp){
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = calendar.getTimeZone();
        String pattern = "h:mm a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(timeZone);
        return simpleDateFormat.format(new Date(timestamp));

    }

    public void saveDataToSharedPreference(Context context,String key,int value){
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key,value);
        editor.commit();
    }

    public int getProgressValueFromSharedPreference(Context context,String key){
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key,SHARED_PREFERENCE_DEFAULT_VALUE);
    }

    public boolean checkInternet(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

}
