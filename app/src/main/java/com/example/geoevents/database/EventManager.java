package com.example.geoevents.database;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private DatabaseReference eventsRef;

    public EventManager() {
        eventsRef = FirebaseDatabaseHelper.getInstance().getReference("events");
    }

    public void addEvent(String title, String description, LatLng latLng, String priority,
                         String date, String time, String endDatetime,
                         OnEventAddedListener listener) {
        String eventId = eventsRef.push().getKey();
        String authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Event event = new Event(eventId, title, description, latLng.latitude, latLng.longitude, priority,
                date, time, endDatetime, authorId);

        eventsRef.child(eventId).setValue(event).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onEventAdded(event);
            } else {
                listener.onEventAddFailed();
            }
        });
    }

    public void getAllEvents(final OnEventsLoadedListener listener) {
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Event> eventList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Event event = snapshot.getValue(Event.class);
                    eventList.add(event);
                }
                listener.onEventsLoaded(eventList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onDataLoadFailed(error.toException());
            }
        });
    }

    public void updateEvent(Event event, OnEventUpdateListener listener) {
        eventsRef.child(event.getId()).setValue(event).addOnCompleteListener( task -> {
            if (task.isSuccessful()) {
                listener.onEventUpdate();
            } else {
                listener.onEventUpdateFailed(task.getException());
            }
        });
    }
    public void deleteEvent(String eventId, OnEventDeletedListener listener) {
        eventsRef.child(eventId).removeValue().addOnCompleteListener( task -> {
            if (task.isSuccessful()) {
                listener.onEventDeleted();
            } else {
                listener.onEventDeletedFailed(task.getException());
            }
        });
    }

    public interface OnEventAddedListener {
        void onEventAdded(Event event);
        void onEventAddFailed();
    }

    public interface OnEventsLoadedListener {
        void onEventsLoaded(List<Event> eventList);
        void onDataLoadFailed(Exception e);
    }
    public interface OnEventUpdateListener {
        void onEventUpdate();
        void onEventUpdateFailed(Exception e);
    }
    public interface OnEventDeletedListener {
        void onEventDeleted();
        void onEventDeletedFailed(Exception e);
    }
}
