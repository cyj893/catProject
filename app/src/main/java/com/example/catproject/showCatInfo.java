package com.example.catproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_catinfo);

        Intent intent = getIntent();
        String catName = intent.getStringExtra("catName");
        TextView textView = findViewById(R.id.show_name);
        textView.setText(catName);
        String docPath = "catinfo/" + catName;

        mDatabase = FirebaseFirestore.getInstance();

        mDatabase.document(docPath)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if( task.isSuccessful() ){
                            Map<String, Object> getDB = task.getResult().getData();
                            String old = getDB.get("old").toString();
                            String features = getDB.get("features").toString();
                            if( getDB.containsKey("img") ){
                                String simg = getDB.get("img").toString();
                                Bitmap bm = Info.StringToBitmap(simg);
                                ImageView imageView = (ImageView)findViewById(R.id.imageView);
                                imageView.setImageBitmap(bm);
                            }
                            else{
                                ;
                            }
                        }
                        else{
                            Log.d("SHOW", "Error show DB", task.getException());
                        }
                    }
                });


    }

}
