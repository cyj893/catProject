package com.example.catproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class showMap extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showmap);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        String msg = intent.getStringExtra("msg");
        TextView textView = findViewById(R.id.show_msg);
        textView.setText(msg);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;

        LatLng PNU = new LatLng(35.233903, 129.079871);

        setMarkersFromDB();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PNU, 15));

        mMap.getUiSettings().setCompassEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMarkerClickListener(this);


    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current: "+location, Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("Marker", "marker clicked");
        Intent intent = new Intent(getApplicationContext(), showCatInfo.class);
        intent.putExtra("catName", marker.getTitle());
        Log.d("Marker", "send intent");
        startActivity(intent);
        return true;
    }

    public void setMarkersFromDB(){ // DB에서 정보 들고 와서 마커 보여주기
        FirebaseFirestore mDatabase;
        mDatabase = FirebaseFirestore.getInstance();
        mDatabase.collection("catinfo")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if( task.isSuccessful() ){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                Map<String, Object> getDB = document.getData();
                                String catName = getDB.get("name").toString();
                                String type = getDB.get("type").toString();
                                double latitude = Double.parseDouble(getDB.get("latitude").toString());
                                double longitude = Double.parseDouble(getDB.get("longitude").toString());
                                Log.d("GETDB", catName);

                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(new LatLng(latitude, longitude))
                                        .title(catName)
                                        .snippet("반가워요")
                                        .icon(BitmapDescriptorFactory.fromResource(getResources().getIdentifier(type,"drawable",getPackageName())));
                                mMap.addMarker(markerOptions);
                            }
                        }
                        else{
                            Log.d("SHOW", "Error show DB", task.getException());
                        }
                    }
                });
    }
}
