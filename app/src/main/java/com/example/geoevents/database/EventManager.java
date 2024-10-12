package com.example.geoevents.database;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;

public class EventManager {
    private DatabaseReference eventsRef;

    public EventManager() {
        eventsRef = FirebaseDatabaseHelper.getInstance().getReference("events");
    }

    public void addEvent(String title, String description, LatLng latLng, String priority,
                         String date, String time, String endDatetime,
                         OnEventAddedListener listener) {
        String eventId = eventsRef.push().getKey();
        Event event = new Event(title, description, latLng.latitude, latLng.longitude, priority,
                date, time, endDatetime);

        eventsRef.child(eventId).setValue(event).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onEventAdded(event);
            } else {
                listener.onEventAddFailed();
            }
        });
    }

    public interface OnEventAddedListener {
        void onEventAdded(Event event);
        void onEventAddFailed();
    }
}
