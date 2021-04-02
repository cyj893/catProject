package com.example.catproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;

        LatLng PNU = new LatLng(35.233903, 129.079871);

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(new LatLng(35.231561, 129.082566))
                    .title("cat1")
                    .snippet("반가워요")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.cat1));
        mMap.addMarker(markerOptions);

        markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(35.233903, 129.079871))
                .title("cat2")
                .snippet("반가워요")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.cat1));
        mMap.addMarker(markerOptions);

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



    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current: "+location, Toast.LENGTH_LONG).show();

    }
}