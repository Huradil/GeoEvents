package com.example.geoevents.manage;

import android.content.Intent;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geoevents.MainActivity;
import com.example.geoevents.R;
import com.example.geoevents.database.Event;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private LatLng userLocation;

    public EventAdapter(List<Event> events, LatLng userLocation) {
        this.eventList = events;
        this.userLocation = userLocation;
    }

    public void updateUserLocation(LatLng userLocation) {
        this.userLocation = userLocation;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvTitle.setText(event.getTitle());
        holder.tvDescription.setText(event.getDescription());

        if (userLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(userLocation.latitude, userLocation.longitude,
                    event.getLatitude(), event.getLongitude(), results);
            float distance = results[0] / 1000;
            holder.tvDistance.setText(String.format("Расстояние: %.2f км", distance));
        } else {
            holder.tvDistance.setText("Расстояние: - ");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent mapIntent = new Intent(v.getContext(), MainActivity.class);
            mapIntent.putExtra("eventLat", event.getLatitude());
            mapIntent.putExtra("eventLng", event.getLongitude());
            v.getContext().startActivity(mapIntent);
        });
    }


    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDistance;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDescription = itemView.findViewById(R.id.tvEventDescription);
            tvDistance = itemView.findViewById(R.id.tvEventDistance);
        }
    }
}
