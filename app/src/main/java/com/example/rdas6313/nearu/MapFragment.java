package com.example.rdas6313.nearu;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rdas6313.nearu.Permissions.PermissionUtils;
import com.example.rdas6313.nearu.Permissions.PermissionUtilsListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback,PermissionUtilsListener {

    private static final String TAG = MapFragment.class.getSimpleName();

    private static final int LOCATION_REQUEST_CODE = 2123;

    private MapView mapView;
    private MapboxMap map;

    private PermissionUtils permissionManager;

    private boolean isLocationPermissionGranted = false;


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
            isLocationPermissionGranted = true;
        }else{
            permissionManager = new PermissionUtils(this);
            permissionManager.requestLocationPermission(this);
        }
    }

    @Override
    public void onDontAskAgain() {
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

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        Log.d(TAG,"Map Ready");
        map = mapboxMap;
        addMarker(new LatLng(48.13863, 11.57603));
        //Todo:- do whatever u want to do after map loaded
    }

    private void addMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Test Heading")
                .snippet("Test snippet");
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .tilt(20)
                .zoom(12)
                .build();

        if(map != null) {
            map.addMarker(markerOptions);
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),3000);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //checking for location permission
        checkLocationPermission();
        if(mapView != null)
            mapView.onStart();
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
        if(mapView != null)
            mapView.onStop();
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
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mapView != null)
            mapView.onSaveInstanceState(outState);
    }

}
