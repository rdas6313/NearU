package com.example.rdas6313.nearu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

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

}
