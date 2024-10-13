package com.example.geoevents.manage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.example.geoevents.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerManager {
    private GoogleMap mMap;
    private Context mContext;

    public MarkerManager(GoogleMap mMap, Context context) {
        this.mMap = mMap;
        this.mContext = context;
    }

    public Marker addMarkerWithPriority(LatLng latLng, String title, String priority) {
        BitmapDescriptor icon = getMarkerIconByPriority(priority);

        return mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(icon)
        );
    }

    private BitmapDescriptor getMarkerIconByPriority(String priority) {
        int drawableId;

        switch (priority) {
            case "Высокий":
                drawableId = R.drawable.high_marker;
                break;
            case "Средний":
                drawableId = R.drawable.midle_marker;
                break;
            case "Низкий":
                drawableId = R.drawable.low_marker;
                break;
            default:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        }

        // Получение ресурсов через контекст
        Drawable vectorDrawable = VectorDrawableCompat.create(mContext.getResources(), drawableId, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

//    private BitmapDescriptor getMarkerIconByPriority(String priority) {
//        switch (priority) {
//            case "Высокий":
//                return BitmapDescriptorFactory.fromResource(R.drawable.high_marker);
//            case "Средний":
//                return BitmapDescriptorFactory.fromResource(R.drawable.midle_marker);
//            case "Низкий":
//                return BitmapDescriptorFactory.fromResource(R.drawable.low_marker);
//            default:
//                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
//        }
//    }
}


