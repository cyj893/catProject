package com.example.catproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;

public class showCatInfo extends AppCompatActivity {

    private FirebaseFirestore mDatabase;
    private StorageReference storageRef;
    private CustomImageAdapter mCustomImageAdapter;

    TextView textViewName;
    TextView textViewFeatures;
    Button btn_goMain;
    Button btn_edit;
    RecyclerView mRecyclerView;
    View noInfo;

    ArrayList<Uri> mArrayUri;
    long num = 0;
    String features = "?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_catinfo);

        Log.d("CatInfo", "get intent");
        Intent intent = getIntent();
        String catName = intent.getStringExtra("catName");

        mRecyclerView = findViewById(R.id.recyclerView);
        noInfo = findViewById(R.id.noInfo);
        textViewName = findViewById(R.id.show_name);
        textViewFeatures = findViewById(R.id.show_features);
        btn_goMain = findViewById(R.id.btn_goMain);
        btn_edit = findViewById(R.id.btn_edit);

        textViewName.setText(catName);
        textViewName.setMovementMethod(new ScrollingMovementMethod());
        textViewFeatures.setMovementMethod(new ScrollingMovementMethod());
        noInfo.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);

        mArrayUri = new ArrayList<>();
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mCustomImageAdapter = new CustomImageAdapter(R.layout.row, getApplicationContext(), mArrayUri);
        mRecyclerView.setAdapter(mCustomImageAdapter);

        mDatabase = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://catproj.appspot.com/");
        storageRef = storage.getReference();

        showRecyclerView(catName);

        btn_goMain.setOnClickListener(v -> onBackPressed());

        btn_edit.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(), showMap.class);
            startActivity(intent1);
        });

        mCustomImageAdapter.setOnItemClickListener((view, position) -> {
            Log.d("CLICKED", "clicked");
            //mArrayUri.get(position);
        });

    } // End onCreate();

    /*
    DB에서 정보 들고 와서 리사이클러뷰 보여주기
     */
    public void showRecyclerView(String catName){
        String docPath = "catIMG/" + catName;
        mDatabase.document(docPath)
                .get()
                .addOnCompleteListener(task -> {
                    if( task.isSuccessful() ){
                        Map<String, Object> getDB = task.getResult().getData();
                        if( getDB == null ){
                            Log.d("DB Error", "Error get DB no data", task.getException());
                            return;
                        }
                        Object ob;
                        if( (ob = getDB.get("features")) != null ){
                            features = ob.toString().replace("(endline)", "\n");
                        }
                        if( (ob = getDB.get("num")) != null ){
                            num = (Long)ob;
                        }
                        textViewFeatures.setText(features);
                        Log.d("SHOW", catName + " => " + features + " " + num);

                        for(int i = 1; i < num + 1; i++){
                            String filename = i + ".jpg";
                            storageRef.child(catName + "/" + filename).getDownloadUrl().addOnCompleteListener(task1 -> {
                                if( task1.isSuccessful() ){
                                    Log.d("GETURI", catName + "/" + filename + " Success");
                                    noInfo.setVisibility(View.INVISIBLE);
                                    mRecyclerView.setVisibility(View.VISIBLE);
                                    mArrayUri.add(task1.getResult());
                                    mCustomImageAdapter.notifyDataSetChanged();
                                }
                                else{
                                    Log.d("GETURI", catName + "/" + filename + " Fail");
                                }
                            });
                        } // End for
                    }
                    else{
                        Log.d("SHOW", "Error show DB", task.getException());
                    }
                });
    } // End showRecyclerView();







}
