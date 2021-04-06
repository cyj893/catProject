package com.example.catproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseFirestore.getInstance();

        Button btn_map = (Button)findViewById(R.id.btn_map);
        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), showMap.class);
                intent.putExtra("msg", "I'll show");
                startActivity(intent);
            }
        });

        EditText et_info_name,et_info_type;
        et_info_name = findViewById(R.id.et_info_name);
        et_info_type = findViewById(R.id.et_info_type);


        Button btn_save = (Button)findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getCatName = et_info_name.getText().toString();
                String getType = et_info_type.getText().toString();

                //hashmap 만들기
                HashMap result = new HashMap<>();
                result.put("name", getCatName);
                result.put("type", getType);

                mDatabase.collection("catinfo")
                        .add(result)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("ADD","Document added ID: "+documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("ADD","Error adding: ",e);
                            }
                        });

            }
        });

        Button btn_showDB = (Button)findViewById(R.id.btn_showDB);
        btn_showDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.collection("catinfo")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if( task.isSuccessful() ){
                                    for(QueryDocumentSnapshot document : task.getResult()){
                                        Log.d("SHOW", document.getId() + " => " + document.getData());
                                    }
                                }
                                else{
                                    Log.d("SHOW", "Error show DB", task.getException());
                                }
                            }
                        });

            }
        });
    }

}