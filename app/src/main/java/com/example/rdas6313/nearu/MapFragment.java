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
import android.widget.Toast;

import com.example.rdas6313.nearu.Permissions.PermissionUtils;
import com.example.rdas6313.nearu.Permissions.PermissionUtilsListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
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
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, PermissionUtilsListener, LocationEngineListener, GeoQueryEventListener  {

    private static final String TAG = MapFragment.class.getSimpleName();

    private static final int LOCATION_REQUEST_CODE = 2123;
    private static final float SEARCH_USER_RADIUS = 0.5f; // in kilo meter
    private static final double CAMERA_ZOOM_LEVEL = 12;
    private static final int ANIMATE_CAMERA_DURATION = 4000;

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
    private GeoQuery geoQuery;

    private boolean gotCurrentLocation = false;


    private Utility utility;


    private HashMap<String,User> userData;


    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Mapbox.getInstance(getContext(), getString(R.string.MAPBOX_ACCESS_TOKEN));

        View root = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) root.findViewById(R.id.mapView);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        utility = Utility.getInstance();
    }

    /* Permission Related Methods Start from here */
    private void checkLocationPermission() {
        if (PermissionUtils.isLocationPermissionGranted(getContext())) {
            //granted
            initLocation();
            isLocationPermissionGranted = true;
        } else {
            permissionManager = new PermissionUtils(this);
            permissionManager.requestLocationPermission(this);
        }
    }

    @Override
    public void onDontAskAgain() {
        shouldCheckPermission = true;
        openApplicationSettings();
    }

    private void openApplicationSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
        intent.setData(uri);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivity(intent);
    }


    @Override
    public void onExplanation() {
        explanationDialog();
    }

    @Override
    public void onPermissionResult(boolean isGranted) {
        if (isGranted) {
            initLocation();
            isLocationPermissionGranted = true;
        } else {
            showDialog();
        }
    }

    private void explanationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.EXPLANATION_DIALOG_MSG))
                .setPositiveButton(getString(R.string.EXPLANATION_DIALOG_POSITIVE_BTN_TEXT), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (permissionManager != null)
                            permissionManager.explanationCompleted(MapFragment.this);
                    }
                });
        builder.create().show();
    }

    private void showDialog() {
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
        permissionManager.onRequestPermissionResult(requestCode, permissions, grantResults);
    }
    /* Permission Related Methods End in here */


    private void initLocation() {
        //load Current user Location From Server
        loadCurrentUserLocationFromServer();

        initLocationEngine();
        initLocationComponent();
        if (!isGpsEnabled())
            enableGps();
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        Log.d(TAG, "Map Ready");
        map = mapboxMap;
        checkLocationPermission();
    }

    @SuppressWarnings("MissingPermission")
    private void initLocationEngine() {
        Log.d(TAG, "initEngine");
        if (locationEngine == null) {
            locationEngine = new LocationEngineProvider(getContext()).obtainBestLocationEngineAvailable();
            locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
            locationEngine.addLocationEngineListener(this);
            locationEngine.activate();
        }
        Log.d(TAG, locationEngine.obtainType().name());
        Location location = locationEngine.getLastLocation();

        if (location != null) {
            currentLocation = location;
            sendLocationDataToServer(location);
            loadUserLocation(location, SEARCH_USER_RADIUS);
            gotCurrentLocation = true;
            onDisconnect();
        }
    }

    private void moveCamera(LatLng latLng) {
        if (map == null)
            return;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .tilt(20)
                .zoom(CAMERA_ZOOM_LEVEL)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), ANIMATE_CAMERA_DURATION);
    }

    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return true;
        return false;
    }

    private void enableGps() {
        gpsSettingsOn = true;
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    @SuppressWarnings("MissingPermission")
    private void initLocationComponent() {
        if (map == null)
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
        if (locationEngine == null)
            return;
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) { // When Device User Location Changed
        Log.d(TAG, "new Location " + location.getLatitude() + " " + location.getLongitude());
        currentLocation = location;
        sendLocationDataToServer(location);
        loadUserLocation(location, SEARCH_USER_RADIUS);
        gotCurrentLocation = true;
        onDisconnect();
    }

    private void initUserDataList() {   // initializing user Data List
        if (userData == null)
            userData = new HashMap();
    }

    private void loadUserLocation(Location location, float radius) { // fetching Near By Users using Device user Location and radius
        if (geoFire == null)
            return;
        if (geoQuery == null) { // for first time query
            geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), radius);
            geoQuery.addGeoQueryEventListener(this);
        }else{ // for update query
            geoQuery.setLocation(new GeoLocation(location.getLatitude(),location.getLongitude()),radius);
            initUserDataList();
            userData.clear();
            if(map != null)
                map.clear();
        }
    }

    private void onDisconnect(){
        Utility utility = Utility.getInstance();
        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference(getString(R.string.GEOFIRE_ROOT_REF)+"/"+utility.getUserId());
        locationRef.onDisconnect().removeValue();
        String lastLocationPath = getString(R.string.USER_DATA_KEY)+"/"+utility.getUserId()+"/"+getString(R.string.USER_LAST_LOCATION_KEY);
        DatabaseReference lastLocationRef = FirebaseDatabase.getInstance().getReference(lastLocationPath);
        HashMap<String,Object>hashMap = new HashMap<>();
        hashMap.put(getString(R.string.LATITUDE),currentLocation.getLatitude());
        hashMap.put(getString(R.string.LONGITUDE),currentLocation.getLongitude());
        lastLocationRef.updateChildren(hashMap);
    }

    public void fetchUsersDataFromServer(User user){ // fetching Near By Users Data from Server
        Utility utility = Utility.getInstance();
        if(!utility.isUserLoggedIn())
            return;
        String path = getString(R.string.USER_DATA_KEY)+"/"+utility.getUserId();
        DatabaseReference userDataRef = FirebaseDatabase.getInstance().getReference(path);
        userDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user.setName(dataSnapshot.child(getString(R.string.NAME_KEY)).getValue(String.class));
                user.setMarker(utility.addMarkerToMap(user.getLocation(),user.getName(),"",map));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG,databaseError.getDetails());
            }
        });
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        if(location.longitude == currentLocation.getLongitude() && location.latitude == currentLocation.getLatitude())
            return;

        Log.d(TAG,"Key Entered "+key);
        Toast.makeText(getContext(),"Key Entered "+key,Toast.LENGTH_SHORT).show();
        Utility utility = Utility.getInstance();
        initUserDataList();
        LatLng latLng = new LatLng(location.latitude,location.longitude);
        User user = new User(key,latLng,getContext());
        fetchUsersDataFromServer(user);
        userData.put(key,user);

    }

    @Override
    public void onKeyExited(String key) {
        Log.d(TAG,"Key Exited "+key);
        Toast.makeText(getContext(),"Key Exited "+key,Toast.LENGTH_SHORT).show();
        if(userData != null && userData.containsKey(key)){
            User user = userData.get(key);
            Utility utility = Utility.getInstance();
            utility.removeMarkerFromMap(user.getMarker(),map);
            userData.remove(key);
        }
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        Log.d(TAG,"Key Moved "+key);
        Toast.makeText(getContext(),"Key Moved "+key,Toast.LENGTH_SHORT).show();
        if(userData != null && userData.containsKey(key)){
            User user = userData.get(key);
            LatLng latLng = new LatLng(location.latitude,location.longitude);
            user.setLocation(latLng);
            user.getMarker().setPosition(latLng);
        }
    }

    @Override
    public void onGeoQueryReady() {
        Log.d(TAG,"Query Ready");
        Toast.makeText(getContext(),"Query Ready ",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        if(error != null)
            Log.e(TAG,error.getDetails());
    }

    private void sendLocationDataToServer(Location location) {
        Utility utility = Utility.getInstance();
        if(!utility.isUserLoggedIn()){
            Log.e(TAG,"User not loggedIn");
            return;
        }

        initGeofire();
        geoFire.setLocation(utility.getUserId(), new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    Log.e(TAG, error.getDetails());
                }
            }
        });
    }

    private void initGeofire() {
        if (geoFire == null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.GEOFIRE_ROOT_REF));
            geoFire = new GeoFire(reference);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "OnStart");
        if (shouldCheckPermission) {
            checkLocationPermission();
            shouldCheckPermission = false;
        }

        if (locationComponent != null) {
            locationComponent.onStart();
        }
        if (locationEngine != null) {
            locationEngine.addLocationEngineListener(this);
            locationEngine.requestLocationUpdates();
        }

        if (geoQuery != null)
            geoQuery.addGeoQueryEventListener(this);

        if (mapView != null)
            mapView.onStart();

        if (gpsSettingsOn) {
            gpsSettingsOn = false;
            if (!isGpsEnabled()) {
                enableGps();
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null)
            mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null)
            mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        shouldCheckPermission = true;
        if (mapView != null)
            mapView.onStop();

        if (locationEngine != null) {
            locationEngine.removeLocationEngineListener(this);
            locationEngine.removeLocationUpdates();
        }
        if (geoQuery != null)
            geoQuery.removeAllListeners();

        if(userData != null) {
            userData.clear();
            userData = null;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null)
            mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null)
            mapView.onDestroy();

        if (locationEngine != null)
            locationEngine.deactivate();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null)
            mapView.onSaveInstanceState(outState);
    }

    private void loadCurrentUserLocationFromServer(){
        Utility utility = Utility.getInstance();
        if(!utility.isUserLoggedIn()) {
            Log.d(TAG,"user not logged in");
            return;
        }
        DatabaseReference currentLocationRef = FirebaseDatabase.getInstance().getReference(getString(R.string.USER_DATA_KEY)+"/"+utility.getUserId()+"/"+getString(R.string.USER_LAST_LOCATION_KEY));
        currentLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG,dataSnapshot.toString());

                if(gotCurrentLocation || dataSnapshot.getValue() == null)
                    return;

                double lat =  dataSnapshot.child(getString(R.string.LATITUDE)).getValue(Double.class);
                double lng = dataSnapshot.child(getString(R.string.LONGITUDE)).getValue(Double.class);
                Log.e(TAG,lat+" "+lng);
                currentLocation = new Location(LocationManager.PASSIVE_PROVIDER);
                currentLocation.setLatitude(lat);
                currentLocation.setLongitude(lng);
                initGeofire();
                if(map != null)
                    map.getLocationComponent().forceLocationUpdate(currentLocation);
                //Todo:- comment these lines
                sendLocationDataToServer(currentLocation);
                loadUserLocation(currentLocation,SEARCH_USER_RADIUS);
                onDisconnect();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG,databaseError.getDetails());
            }
        });
    }
}
