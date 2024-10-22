package com.example.geoevents;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
    private List<Event> filteredEvents = new ArrayList<>();
    private LatLng userLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private EventManager eventManager;
    private SearchView searchView;
    private Spinner spinnerPriority;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        recyclerView = findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(filteredEvents, userLocation);
        recyclerView.setAdapter(eventAdapter);
        searchView = findViewById(R.id.searchView);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();

        eventManager = new EventManager();

        getCurrentLocation();

        loadEvents();

        setupSearch();
        setupPriorityFilter();
    }

    private void loadEvents() {
        eventManager.getAllEvents(new EventManager.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<Event> eventList) {
                events.clear();
                events.addAll(eventList);  // обновляем список событий
                filterEvents(searchView.getQuery().toString(), spinnerPriority.getSelectedItem().toString()); // обновляем фильтрацию
            }

            @Override
            public void onDataLoadFailed(Exception e) {
                Toast.makeText(EventListActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterEvents(query, spinnerPriority.getSelectedItem().toString());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText, spinnerPriority.getSelectedItem().toString());
                return true;
            }
        });
    }

    private void setupPriorityFilter() {
        spinnerPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String priority = parent.getItemAtPosition(position).toString();
                filterEvents(searchView.getQuery().toString(), priority);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Не требуется действие
            }
        });
    }

    private void filterEvents(String query, String priority) {
        List<Event> filteredList = new ArrayList<>();

        for (Event event : events) {
            boolean matchesText = event.getDescription().toLowerCase().contains(query.toLowerCase())
                    || event.getTitle().toLowerCase().contains(query.toLowerCase());

            boolean matchesPriority = priority.equals("Все") || event.getPriority().equalsIgnoreCase(priority);

            if (matchesText && matchesPriority) {
                filteredList.add(event);
            }
        }

        filteredEvents.clear();
        filteredEvents.addAll(filteredList);

        eventAdapter.notifyDataSetChanged();
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
