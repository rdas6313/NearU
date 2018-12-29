package com.example.rdas6313.nearu;

import com.crashlytics.android.Crashlytics;
import android.app.Application;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }
}
