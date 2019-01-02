package com.example.rdas6313.nearu.Permissions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;


public class PermissionUtils {

    private static final int LOCATION_PERMISSION_REQUEST = 11023;
    private PermissionUtilsListener mListener;

    private static final String TAG = PermissionUtils.class.getSimpleName();

    public PermissionUtils(PermissionUtilsListener listener){
        mListener = listener;
    }

    public void requestLocationPermission(Fragment fragment){
        if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) && fragment.shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {
                explain();
        }else{
            if(mListener != null)
                mListener.onDontAskAgain();
        }

    }

    public void explanationCompleted(Fragment fragment){
        requestPermission(fragment);
    }

    private void requestPermission(Fragment fragment){
        fragment.requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_PHONE_STATE
        },LOCATION_PERMISSION_REQUEST);
    }

    public static boolean isPermissionGranted(Context context){
        if(ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context,Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }

    public void onRequestPermissionResult(int requestCode,String permissions[],int grantResults[]){
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    sendPermissionResult(true);
                }else{
                    //Permission Denied
                    sendPermissionResult(false);
                }
            }
        }
    }

    private void sendPermissionResult(boolean result){
        if(mListener == null)
            return;
        mListener.onPermissionResult(result);
    }

    private void explain(){
        if(mListener == null)
            return;
        mListener.onExplanation();
    }
}

