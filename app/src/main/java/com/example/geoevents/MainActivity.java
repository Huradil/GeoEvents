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
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.geoevents.database.Event;
import com.example.geoevents.database.EventManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAuth mAuth;
    private GoogleMap mMap;
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

        mMap.setOnMapLongClickListener(latLng -> showAddEventDialog(latLng));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

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

        builder.setView(view);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String title = titleInput.getText().toString();
            String description = descriptionInput.getText().toString();
            String selectedPriority = prioritySpinner.getSelectedItem().toString();
            String eventDate = dateInput.getText().toString();
            String eventTime = timeInput.getText().toString();

            eventManager.addEvent(title, description, latLng, selectedPriority, eventDate,
                    eventTime, new EventManager.OnEventAddedListener() {
                @Override
                public void onEventAdded(Event event) {
                    Toast.makeText(MainActivity.this, "Событие добавлено", Toast.LENGTH_SHORT).show();

                    mMap.addMarker(new MarkerOptions().position(latLng).title(event.getTitle()));
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
}