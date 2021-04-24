package com.example.catproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;

public class showCatInfo extends AppCompatActivity {

    private FirebaseFirestore mDatabase;
    TextView textViewName;
    TextView textViewFeatures;
    Button btn_goMain;
    Button btn_edit;
    public RecyclerView mRecyclerView;
    View noInfo;
    ArrayList<Uri> mArrayUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_catinfo);

        Log.d("CatInfo", "get intent");
        Intent intent = getIntent();
        String catName = intent.getStringExtra("catName");


        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        noInfo = (LinearLayout)findViewById(R.id.noInfo);
        textViewName = findViewById(R.id.show_name);
        textViewFeatures = findViewById(R.id.show_features);
        btn_goMain = findViewById(R.id.btn_goMain);
        btn_edit = findViewById(R.id.btn_edit);

        textViewName.setMovementMethod(new ScrollingMovementMethod());
        textViewFeatures.setMovementMethod(new ScrollingMovementMethod());
        noInfo.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);

        mArrayUri = new ArrayList<>();
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);

        CustomImageAdapter mCustomImageAdapter = new CustomImageAdapter(R.layout.row, getApplicationContext(), mArrayUri);

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
                            String features = getDB.get("features").toString().replace("(endline)", "\n");
                            int num = Integer.parseInt(getDB.get("num").toString());
                            Log.d("SHOW", catName + " => " + features + " " + num);
                            textViewFeatures.setText(features);

                            FirebaseStorage storage = FirebaseStorage.getInstance("gs://catproj.appspot.com/");
                            StorageReference storageRef = storage.getReference();
                            mRecyclerView.setAdapter(mCustomImageAdapter);

                            for(int i = 1; i < num + 1; i++){
                                String filename = i + ".jpg";
                                Log.d("GETURI", catName + "/" + filename);
                                storageRef.child(catName + "/" + filename).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if(task.isSuccessful()){
                                            noInfo.setVisibility(View.INVISIBLE);
                                            mRecyclerView.setVisibility(View.VISIBLE);
                                            mArrayUri.add(task.getResult());
                                            Log.d("GETURI!!", "Success");
                                            mCustomImageAdapter.notifyDataSetChanged();
                                        }
                                        else{
                                            Log.d("GETURI!!", "Fail");
                                        }
                                    }
                                });
                            }
                        }
                        else{
                            Log.d("SHOW", "Error show DB", task.getException());
                        }
                    }
                });


        btn_goMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), showMap.class);
                startActivity(intent);
            }
        });


        mCustomImageAdapter.setOnItemClickListener(new CustomImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d("CLICKED", "clicked");
                mArrayUri.get(position);
            }
        });



    }








}
