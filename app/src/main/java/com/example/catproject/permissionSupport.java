package com.example.catproject;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class permissionSupport {
    private Context context;
    private Activity activity;

    private String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private List<String> permissionList;

    private final int MULTIPLE_PERMISSIONS = 7490;

    public permissionSupport(Activity _activity, Context _context){
        this.activity = _activity;
        this.context = _context;
    }

    public boolean checkPermission(){
        permissionList = new ArrayList<>();

        for(String pm : permissions){
            if( ContextCompat.checkSelfPermission(context, pm)
                    != PackageManager.PERMISSION_GRANTED ){
                permissionList.add(pm);
            }
        }

        if( !permissionList.isEmpty() ){
            return false;
        }
        return true;
    }

    public void requestPermission(){
        ActivityCompat.requestPermissions(activity, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
    }

    public boolean permissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if( requestCode == MULTIPLE_PERMISSIONS && (grantResults.length > 0) ){
            for(int i = 0; i < grantResults.length; i++){
                if( grantResults[i] == -1 )
                    return false;
            }
        }
        return true;
    }













}
