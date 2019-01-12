package com.example.rdas6313.nearu.Map;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public interface MapModelContract {

    public void onFetchUserDataSuccess(DataSnapshot dataSnapshot);
    public void onFetchUserDataError(DatabaseError databaseError);

    public void onLoadCurrentUserLocationSuccess(DataSnapshot dataSnapshot);
    public void onLoadCurrentUserLocationError(DatabaseError databaseError);
}
