package com.example.geoevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geoevents.database.Event;
import com.example.geoevents.database.EventManager;
import com.example.geoevents.manage.MarkerManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private MarkerManager markerManager;
    private EventManager eventManager;
    private FusedLocationProviderClient fusedLocationClient;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация Auth
        mAuth = FirebaseAuth.getInstance();
        eventManager = new EventManager();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        markerManager = new MarkerManager(mMap, this);

        mMap.setOnMapLongClickListener(latLng -> showAddEventDialog(latLng));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        // get all event on map
        eventManager.getAllEvents(new EventManager.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<Event> eventList) {
                for (Event event : eventList) {
                    LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
                    String title = event.getTitle();
                    String priority = event.getPriority();

                    Marker marker = markerManager.addMarkerWithPriority(eventLocation, title, priority);

                    marker.setTag(event);

                }
            }
            @Override
            public void onDataLoadFailed(Exception e) {
                Toast.makeText(MainActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });

        mMap.setOnMarkerClickListener(marker -> {
            Event event = (Event) marker.getTag();

            if (event != null) {
                showEventDetailsDialog(event);
            }
            return true;
        });

        // обновление маркеров
        eventManager.subscribeToEventChanges(new EventManager.OnEventsUpdateListener() {
            @Override
            public void onMarkersUpdate() {
                updateMarkers();
            }
            @Override
            public void onDatabaseLoadFailed(Exception e) {
                Toast.makeText(MainActivity.this, "Ошибка при загрузке обновлений", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Вы здесь"));
                } else {
                    Toast.makeText(this, "Местоположение не найдено", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener( e -> {
                Toast.makeText(this, "Ошибка получения местоположения" + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Разрешение на доступ к местоположению не предоставлено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            openLoginActivity();
        }
    }

    private void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void showAddEventDialog(LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить событие");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        EditText titleInput = view.findViewById(R.id.eventTitle);
        EditText descriptionInput = view.findViewById(R.id.eventDescription);
        Spinner prioritySpinner = view.findViewById(R.id.eventPrioritySpinner);
        EditText dateInput = view.findViewById(R.id.eventDate);
        EditText timeInput = view.findViewById(R.id.eventTime);
        EditText endDateTimeInput = view.findViewById(R.id.eventEndDateTime);

        // current date and time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        dateInput.setText(dateFormat.format(calendar.getTime()));
        timeInput.setText(timeFormat.format(calendar.getTime()));

        // DatePicker
        dateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MainActivity.this, (view1, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        dateInput.setText(dateFormat.format(calendar.getTime()));
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // TimePicker
        timeInput.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    MainActivity.this, (view12, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        timeInput.setText(timeFormat.format(calendar.getTime()));
            },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });
        endDateTimeInput.setOnClickListener(v -> showDateTimePicker(endDateTimeInput));

        builder.setView(view);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String title = titleInput.getText().toString();
            String description = descriptionInput.getText().toString();
            String selectedPriority = prioritySpinner.getSelectedItem().toString();
            String eventDate = dateInput.getText().toString();
            String eventTime = timeInput.getText().toString();
            String endDateTime = endDateTimeInput.getText().toString();

            eventManager.addEvent(title, description, latLng, selectedPriority, eventDate,
                    eventTime, endDateTime, new EventManager.OnEventAddedListener() {
                @Override
                public void onEventAdded(Event event) {
                    Toast.makeText(MainActivity.this, "Событие добавлено", Toast.LENGTH_SHORT).show();

                    //mMap.addMarker(new MarkerOptions().position(latLng).title(event.getTitle()));
                    markerManager.addMarkerWithPriority(latLng, event.getTitle(), event.getPriority() );
                }

                @Override
                public void onEventAddFailed() {
                    Toast.makeText(MainActivity.this, "Ошибка при добавлении события", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();


    }

    private void showDateTimePicker(EditText endDateTimeInput) {
        // Текущая дата
        final Calendar currentDate = Calendar.getInstance();
        final Calendar date = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            date.set(year, month, dayOfMonth);
            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);

                // Устанавливаем выбранную дату и время в поле
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                endDateTimeInput.setText(dateTimeFormat.format(date.getTime()));

            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showEventDetailsDialog(Event event) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.event_details, null);
        builder.setView(dialogView);

        EditText eventDescription = dialogView.findViewById(R.id.eventDescription);
        Spinner spinnerPriority = dialogView.findViewById(R.id.eventPriority);
        EditText eventDateTime = dialogView.findViewById(R.id.eventDateTime);
        EditText eventEndDateTime = dialogView.findViewById(R.id.eventEndDateTime);
        Button btnDeleteEvent = dialogView.findViewById(R.id.btnDeleteEvent);
        Button btnSaveChanges = dialogView.findViewById(R.id.btnSaveChanges);

        eventDescription.setText(event.getDescription());
        eventDateTime.setText(event.getDate() + " " + event.getTime());
        eventEndDateTime.setText(event.getEndDateTime());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.event_priorities,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item); // -> fix may be
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);
        int priorityPosition = adapter.getPosition(event.getPriority());
        spinnerPriority.setSelection(priorityPosition);

        if (!event.getAuthorId().equals(currentUserId)) {
            eventDescription.setEnabled(false);
            spinnerPriority.setEnabled(false);
            eventDateTime.setEnabled(false);
            eventEndDateTime.setEnabled(false);
        } else {
            btnDeleteEvent.setVisibility(View.VISIBLE);
            btnSaveChanges.setVisibility(View.VISIBLE);

            btnDeleteEvent.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Удаление события")
                        .setMessage("Вы уверены что хотите удалить это событие?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            eventManager.deleteEvent(event.getId(), new EventManager.OnEventDeletedListener() {
                                @Override
                                public void onEventDeleted() {
                                    Toast.makeText(MainActivity.this, "Событие удалено", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    // updateMarkers();
                                }

                                @Override
                                public void onEventDeletedFailed(Exception e) {
                                    Toast.makeText(MainActivity.this, "Ошбика при удалении", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            });
            btnSaveChanges.setOnClickListener(v -> {
                String newDescription = eventDescription.getText().toString();
                String newPriority = spinnerPriority.getSelectedItem().toString();
                String newStartDateTime = eventDateTime.getText().toString();
                String newEndDateTime = eventEndDateTime.getText().toString();

                event.setDescription(newDescription);
                event.setPriority(newPriority);
                event.setDate(newStartDateTime.split(" ")[0]);
                event.setTime(newStartDateTime.split(" ")[1]);
                event.setEndDateTime(newEndDateTime);

                eventManager.updateEvent(event, new EventManager.OnEventUpdateListener() {
                    @Override
                    public void onEventUpdate() {
                        Toast.makeText(MainActivity.this, "Событие обновлено", Toast.LENGTH_SHORT).show();
                        // updateMarkers();
                    }

                    @Override
                    public void onEventUpdateFailed(Exception e) {
                        Toast.makeText(MainActivity.this, "Ошибка при обновлении", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateMarkers() {
        eventManager.getAllEvents(new EventManager.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<Event> eventList) {
                mMap.clear();

                for (Event event : eventList) {
                    Marker marker = markerManager.addMarkerWithPriority(new LatLng(event.getLatitude(), event.getLongitude()),
                            event.getTitle(), event.getPriority());

                    marker.setTag(event);
                }
            }

            @Override
            public void onDataLoadFailed(Exception e) {
                Toast.makeText(MainActivity.this, "Ошибка при загрузке событий", Toast.LENGTH_SHORT).show();

            }
        });
        mMap.setOnMarkerClickListener(marker -> {
            Event event = (Event) marker.getTag();

            if (event != null) {
                showEventDetailsDialog(event);
            }
            return true;
        });
    }
}