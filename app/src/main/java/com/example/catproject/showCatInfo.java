package com.example.catproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;


public class showCatInfo extends AppCompatActivity {

    private FirebaseFirestore mDatabase;
    private StorageReference storageRef;
    private CustomImageAdapter mCustomImageAdapter;
    private StaggeredGridLayoutManager manager;

    TextView textViewName;
    TextView textViewFeatures;
    Button btn_goMain;
    Button btn_edit;
    RecyclerView mRecyclerView;
    View noInfo;
    View layout1;
    View layout2;
    View btns;
    PhotoView photoView;
    Button btn_left;
    Button btn_right;

    ArrayList<Uri> mArrayUri;
    long num;
    String names;
    String features;
    int nowPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_catinfo);

        Log.d("CatInfo", "get intent");
        Intent intent = getIntent();
        String catName = intent.getStringExtra("catName");

        num = 0;
        nowPos = 0;
        setLayout1(catName);
        setLayout2();

        mDatabase = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://catproj.appspot.com/");
        storageRef = storage.getReference();

        showInfoFromDB(catName);

    } // End onCreate();

    public void setLayout1(String catName){
        mRecyclerView = findViewById(R.id.recyclerView);
        noInfo = findViewById(R.id.noInfo);
        textViewName = findViewById(R.id.show_name);
        textViewFeatures = findViewById(R.id.show_features);
        btn_goMain = findViewById(R.id.btn_goMain);
        btn_edit = findViewById(R.id.btn_edit);
        photoView = findViewById(R.id.photoView);
        layout1 = findViewById(R.id.layout1);

        textViewName.setText(catName);
        textViewName.setMovementMethod(new ScrollingMovementMethod());
        textViewFeatures.setMovementMethod(new ScrollingMovementMethod());
        noInfo.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);

        mArrayUri = new ArrayList<>();
        manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mCustomImageAdapter = new CustomImageAdapter(R.layout.row, getApplicationContext(), mArrayUri);
        mRecyclerView.setAdapter(mCustomImageAdapter);

        btn_goMain.setOnClickListener(v -> onBackPressed());
        btn_edit.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(), showMap.class);
            startActivity(intent1);
        });

        mCustomImageAdapter.setOnItemClickListener((view, position) -> {
            Log.d("CLICKED", "clicked");
            nowPos = position;
            Glide.with(getApplicationContext()).load(mArrayUri.get(nowPos)).diskCacheStrategy(DiskCacheStrategy.ALL).into(photoView);
            layout1.setVisibility(View.INVISIBLE);
            layout2.setVisibility(View.VISIBLE);
            btn_left.setVisibility(View.VISIBLE);
            btn_right.setVisibility(View.VISIBLE);
            if( nowPos == 0 ){
                btn_left.setVisibility(View.INVISIBLE);
            }
            else if( nowPos == mArrayUri.size() - 1 ){
                btn_right.setVisibility(View.INVISIBLE);
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int[] lastItems = new int[2];
                int totalItemCount = manager.getItemCount();
                manager.findLastCompletelyVisibleItemPositions(lastItems);
                int lastVisible = Math.max(lastItems[0], lastItems[1]);
                if( lastVisible >= totalItemCount - 1 ){
                    Log.d("Recycler", "lastVisibled " + lastVisible);
                    manager.invalidateSpanAssignments();
                }
            }
        });

    }

    public void setLayout2(){
        layout2 = findViewById(R.id.layout2);
        btns = findViewById(R.id.btns);
        btn_left = findViewById(R.id.btn_left);
        btn_right = findViewById(R.id.btn_right);

        btn_left.setOnClickListener(v -> {
            if( layout2.getVisibility() == View.VISIBLE && nowPos > 0 ){
                nowPos--;
                Glide.with(getApplicationContext()).load(mArrayUri.get(nowPos)).diskCacheStrategy(DiskCacheStrategy.ALL).into(photoView);
                if( nowPos == 0 ){
                    btn_left.setVisibility(View.INVISIBLE);
                }
                else if( nowPos == mArrayUri.size() - 2 ){
                    btn_right.setVisibility(View.VISIBLE);
                }
            }
        });
        btn_right.setOnClickListener(v -> {
            if( layout2.getVisibility() == View.VISIBLE && nowPos < mArrayUri.size() - 1 ){
                nowPos++;
                Glide.with(getApplicationContext()).load(mArrayUri.get(nowPos)).diskCacheStrategy(DiskCacheStrategy.ALL).into(photoView);
                if( nowPos == mArrayUri.size() - 1 ){
                    btn_right.setVisibility(View.INVISIBLE);
                }
                else if( nowPos == 1 ){
                    btn_left.setVisibility(View.VISIBLE);
                }
            }
        });
        photoView.setOnClickListener(v -> {
            if( btns.getVisibility() == View.VISIBLE ){
                btns.setVisibility(View.INVISIBLE);
            }
            else{
                btns.setVisibility(View.VISIBLE);
            }
        });
    }

    /*
    DB에서 정보 들고 와서 인포 보여주기
     */
    public void showInfoFromDB(String catName){
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
                        if( (ob = getDB.get("names")) != null ){
                            names = ob.toString().replace("(endline)", ", ");
                            textViewName.setText(names);
                        }
                        if( (ob = getDB.get("features")) != null ){
                            features = ob.toString().replace("(endline)", "\n");
                            textViewFeatures.setText(features);
                        }
                        if( (ob = getDB.get("num")) != null ){
                            num = (Long)ob;
                        }
                        Log.d("SHOW", catName + " => " + features + " " + num);

                        if( num > 0 ){
                            noInfo.setVisibility(View.INVISIBLE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                        }
                        for(int i = 1; i < num + 1; i++){
                            String filename = i + ".jpg";
                            storageRef.child(catName + "/" + filename).getDownloadUrl().addOnCompleteListener(task1 -> {
                                if( task1.isSuccessful() ){
                                    Log.d("GETURI", catName + "/" + filename + " Success ");
                                    mArrayUri.add(task1.getResult());
                                    manager.invalidateSpanAssignments();
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


    } // End showInfoFromDB();

    @Override
    public void onBackPressed(){
        if( layout2.getVisibility() == View.VISIBLE ){
            layout1.setVisibility(View.VISIBLE);
            layout2.setVisibility(View.INVISIBLE);
        }
        else{
            super.onBackPressed();
        }
    }





}
