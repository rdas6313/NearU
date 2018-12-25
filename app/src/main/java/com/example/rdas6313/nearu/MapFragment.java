package com.example.rdas6313.nearu;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rdas6313.nearu.Permissions.PermissionUtils;
import com.example.rdas6313.nearu.Permissions.PermissionUtilsListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback,PermissionUtilsListener,LocationEngineListener {

    private static final String TAG = MapFragment.class.getSimpleName();

    private static final int LOCATION_REQUEST_CODE = 2123;

    private MapView mapView;
    private MapboxMap map;

    private PermissionUtils permissionManager;

    private boolean isLocationPermissionGranted = false;
    private boolean shouldCheckPermission = false;
    private boolean gpsSettingsOn = false;

    private LocationEngine locationEngine;
    private LocationComponent locationComponent;

    private Location currentLocation;

    private GeoFire geoFire;

    private final String SERVER_LOCATION_KEY = "current_location";

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Mapbox.getInstance(getContext(),getString(R.string.MAPBOX_ACCESS_TOKEN));

        View root = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView)root.findViewById(R.id.mapView);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    /* Permission Related Methods Start from here */
    private void checkLocationPermission(){
        if(PermissionUtils.isLocationPermissionGranted(getContext())){
            //granted
            initLocation();
            isLocationPermissionGranted = true;
        }else{
            permissionManager = new PermissionUtils(this);
            permissionManager.requestLocationPermission(this);
        }
    }

    @Override
    public void onDontAskAgain() {
        shouldCheckPermission = true;
        openApplicationSettings();
    }

    private void openApplicationSettings(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
        intent.setData(uri);
        if(intent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivity(intent);
    }


    @Override
    public void onExplanation() {
        explanationDialog();
    }

    @Override
    public void onPermissionResult(boolean isGranted) {
        if(isGranted){
            initLocation();
            isLocationPermissionGranted = true;
        }else{
            showDialog();
        }
    }

    private void explanationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.EXPLANATION_DIALOG_MSG))
                .setPositiveButton(getString(R.string.EXPLANATION_DIALOG_POSITIVE_BTN_TEXT), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if(permissionManager != null)
                            permissionManager.explanationCompleted(MapFragment.this);
                    }
                });
        builder.create().show();
    }

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.SHOW_DIALOG_MSG))
                .setPositiveButton(getString(R.string.SHOW_DIALOG_POSITIVE_BTN_TEXT), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        checkLocationPermission();
                    }
                }).setNegativeButton(getString(R.string.SHOW_DIALOG_NEGETIVE_BTN_TEXT), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().finish();
                    }
                });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.onRequestPermissionResult(requestCode,permissions,grantResults);
    }
    /* Permission Related Methods End in here */


    private void initLocation(){
        initGeofire();
        initLocationEngine();
        initLocationComponent();
        if(!isGpsEnabled())
            enableGps();
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        Log.d(TAG,"Map Ready");
        map = mapboxMap;
        checkLocationPermission();
        //addMarker(new LatLng(48.13863, 11.57603));
        //Todo:- do whatever u want to do after map loaded
    }

    @SuppressWarnings("MissingPermission")
    private void initLocationEngine(){
        Log.d(TAG,"initEngine");
        if(locationEngine == null) {
            locationEngine = new LocationEngineProvider(getContext()).obtainBestLocationEngineAvailable();
            locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
            locationEngine.addLocationEngineListener(this);
            locationEngine.activate();
        }
        Log.d(TAG,locationEngine.obtainType().name());
        Location location = locationEngine.getLastLocation();

        if(location != null){
            currentLocation = location;
            moveCamera(new LatLng(location.getLatitude(),location.getLongitude()));
            sendLocationDataToServer(location);
        }
    }

    private void moveCamera(LatLng latLng){
        if(map == null)
            return;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .tilt(20)
                .zoom(12)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),3000);
    }

    private boolean isGpsEnabled(){
        LocationManager locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return true;
        return false;
    }

    private void enableGps(){
        gpsSettingsOn = true;
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    @SuppressWarnings("MissingPermission")
    private void initLocationComponent(){
        if(map == null)
            return;
        locationComponent = map.getLocationComponent();
        locationComponent.activateLocationComponent(getContext());
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        if(locationEngine == null)
            return;
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"new Location "+location.getLongitude()+" "+location.getLongitude());
        currentLocation = location;
        moveCamera(new LatLng(location.getLatitude(),location.getLongitude()));
        sendLocationDataToServer(location);
    }

    private void sendLocationDataToServer(Location location){
        geoFire.setLocation(SERVER_LOCATION_KEY, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if(error != null){
                    Log.e(TAG,error.getDetails());
                }
            }
        });
    }

    private void initGeofire(){
        if(geoFire == null){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user == null){
                Log.e(TAG,"User is not logged in");
                return;
            }
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(user.getUid()+"/");
            geoFire = new GeoFire(reference);
        }

    }

    private void addMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Test Heading")
                .snippet("Test snippet");

        if(map != null) {
            map.addMarker(markerOptions);
            moveCamera(latLng);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"OnStart");
        if(shouldCheckPermission) {
            checkLocationPermission();
            shouldCheckPermission = false;
        }

        if(locationComponent != null){
            locationComponent.onStart();
        }
        if(locationEngine != null){
            locationEngine.addLocationEngineListener(this);
            locationEngine.requestLocationUpdates();
        }

        if(mapView != null)
            mapView.onStart();

        if(gpsSettingsOn){
            gpsSettingsOn = false;
            if(!isGpsEnabled()){
                enableGps();
            }

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if(mapView != null)
            mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mapView != null)
            mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        shouldCheckPermission = true;
        if(mapView != null)
            mapView.onStop();

        if(locationEngine != null) {
            locationEngine.removeLocationEngineListener(this);
            locationEngine.removeLocationUpdates();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(mapView != null)
            mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mapView != null)
            mapView.onDestroy();

        if(locationEngine != null)
            locationEngine.deactivate();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mapView != null)
            mapView.onSaveInstanceState(outState);
    }
}
