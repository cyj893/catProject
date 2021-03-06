package com.example.catproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private FirebaseFirestore mDatabase;
    private int clickedcnt = 0;
    private String clickedname = "?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showmap);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if( mapFragment != null ){
            mapFragment.getMapAsync(this);
        }
        mDatabase = FirebaseFirestore.getInstance();
    } // End onCreate()

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        LatLng PNU = new LatLng(35.233903, 129.079871);

        setMarkersFromDB();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PNU, 15.5f));
        mMap.getUiSettings().setCompassEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        mMap.setOnMarkerClickListener(this);
    } // End onMapReady();

    /*
    ?????? 2??? ????????? ?????? ?????? catInfo??? ?????????
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("Marker", "marker clicked");

        if( clickedcnt == 0 ){
            clickedname = marker.getTitle();
            clickedcnt++;
        }
        else if( clickedname.equals(marker.getTitle()) ){
            clickedname = "?";
            clickedcnt = 0;
            Intent intent = new Intent(getApplicationContext(), showCatInfo.class);
            intent.putExtra("catName", marker.getTitle());
            Log.d("Marker", "send intent");
            startActivity(intent);
        }
        else{
            clickedname = marker.getTitle();
        }

        return false;
    } // End onMarkerClick();

    /*
    DB?????? ?????? ?????? ?????? ?????? ????????????
     */
    public void setMarkersFromDB(){
        mDatabase.collection("catinfo")
                .get()
                .addOnCompleteListener(task -> {
                    if( task.isSuccessful() ){
                        String catName = "?"; String type = "?";
                        double latitude = 0.0; double longitude = 0.0;
                        for(QueryDocumentSnapshot document : task.getResult()){
                            Map<String, Object> getDB = document.getData();
                            Object ob;
                            if( (ob = getDB.get("name")) != null ){
                                catName = ob.toString();
                            }
                            if( (ob = getDB.get("type")) != null ){
                                type = ob.toString();
                            }
                            if( (ob = getDB.get("latitude")) != null ){
                                latitude = Double.parseDouble(ob.toString());
                            }
                            if( (ob = getDB.get("longitude")) != null ){
                                longitude = Double.parseDouble(ob.toString());
                            }
                            Log.d("GETDB", catName);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(latitude, longitude))
                                    .title(catName)
                                    .snippet("????????????")
                                    .icon(BitmapDescriptorFactory.fromResource(getResources().getIdentifier(type,"drawable",getPackageName())));
                            mMap.addMarker(markerOptions);
                        }
                    }
                    else{
                        Log.d("SHOW", "Error show DB", task.getException());
                    }
                });
    } // End setMarkersFromDB();

}
