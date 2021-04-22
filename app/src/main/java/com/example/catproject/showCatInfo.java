package com.example.catproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class showCatInfo extends AppCompatActivity {

    private FirebaseFirestore mDatabase;
    TextView textViewName;
    TextView textViewFeatures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_catinfo);


        Log.d("Marker", "get intent");
        Intent intent = getIntent();
        String catName = intent.getStringExtra("catName");

        textViewName = findViewById(R.id.show_name);
        textViewFeatures = findViewById(R.id.show_features);

        textViewName.setText(catName);

        mDatabase = FirebaseFirestore.getInstance();
        String docPath = "catIMG/" + catName;
        mDatabase.document(docPath)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if( task.isSuccessful() ){
                            Map<String, Object> getDB = task.getResult().getData();
                            String features = getDB.get("features").toString();
                            Log.d("SHOW", catName + " => " + features);
                            textViewFeatures.setText(features);
                            int num = Integer.parseInt(getDB.get("num").toString());

                            for(int i = 1; i < num+1; i++){
                                String key = "img" + i;
                                String simg = getDB.get(key).toString();
                                Bitmap bm = Info.StringToBitmap(simg);
                                ImageView imageView = (ImageView)findViewById(R.id.imageView);
                                imageView.setImageBitmap(bm);
                            }

                        }
                        else{
                            Log.d("SHOW", "Error show DB", task.getException());
                        }
                    }
                });


    }




}
