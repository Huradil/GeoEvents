package com.example.geoevents.database;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDatabaseHelper {
    private static FirebaseDatabase instance;

    private FirebaseDatabaseHelper() {}

    public static FirebaseDatabase getInstance() {
        if (instance == null) {
            instance = FirebaseDatabase.getInstance("https://geoevents-f42ce-default-rtdb.asia-southeast1.firebasedatabase.app");

            instance.setPersistenceEnabled(true);
        }
        return instance;
    }

}
