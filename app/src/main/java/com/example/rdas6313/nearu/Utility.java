package com.example.rdas6313.nearu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;

public final class Utility {

    private static Utility mObj;

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

}
