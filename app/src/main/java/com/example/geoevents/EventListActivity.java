package com.example.geoevents;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoevents.database.Event;
import com.example.geoevents.database.EventManager;
import com.example.geoevents.manage.EventAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class EventListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> events = new ArrayList<>();
    private LatLng userLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private EventManager eventManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        recyclerView = findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();

        eventManager = new EventManager();

        getCurrentLocation();

        loadEvents();

    }

    private void loadEvents() {
        eventManager.getAllEvents(new EventManager.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<Event> eventList) {
                events = eventList;
                eventAdapter = new EventAdapter(events, userLocation);
                recyclerView.setAdapter(eventAdapter);
            }

            @Override
            public void onDataLoadFailed(Exception e) {
                Toast.makeText(EventListActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    if (eventAdapter != null) {
                        eventAdapter.updateUserLocation(userLocation);
                    }
                } else {
                    Toast.makeText(this, "Местоположение не найдено", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Ошибка получения местоположения" + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
