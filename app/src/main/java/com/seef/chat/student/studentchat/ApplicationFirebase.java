package com.seef.chat.student.studentchat;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by SALGUERO on 24/11/2016.
 */

public class ApplicationFirebase extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
