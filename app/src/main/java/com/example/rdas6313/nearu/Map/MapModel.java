package com.example.rdas6313.nearu.Map;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.rdas6313.nearu.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.annotations.Marker;

public class MapModel {

    private static MapModel mapModel;
    private MapModelContract mListener;
    private Context mContext;

    private MapModel(Context context){
        mContext = context;
    }

    public static MapModel getInstance(Context context){
        if(mapModel == null)
            mapModel = new MapModel(context);
        return mapModel;
    }

    public void setListener(MapModelContract listener){
        mListener = listener;
    }

    public void fetchUserData(String uid){
        String path = mContext.getString(R.string.USER_DATA_KEY)+"/"+uid;
        DatabaseReference userDataRef = FirebaseDatabase.getInstance().getReference(path);
        userDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if(mListener == null)
                   return;
               mListener.onFetchUserDataSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
               if(mListener == null)
                   return;
               mListener.onFetchUserDataError(databaseError);
            }
        });
    }

    public void loadCurrentUserLocation(String uid){
        DatabaseReference currentLocationRef = FirebaseDatabase.getInstance().getReference(mContext.getString(R.string.USER_DATA_KEY)+"/"+uid+"/"+mContext.getString(R.string.USER_LAST_LOCATION_KEY));
        currentLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mListener == null)
                    return;
                mListener.onLoadCurrentUserLocationSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(mListener == null)
                    return;
                mListener.onLoadCurrentUserLocationError(databaseError);
            }
        });
    }
}
