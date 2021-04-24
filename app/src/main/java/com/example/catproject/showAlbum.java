package com.example.catproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;

public class showAlbum extends AppCompatActivity {

    private FirebaseFirestore mDatabase;
    private StorageReference storageRef;
    private CustomImageAdapter mCustomImageAdapter;

    EditText editText;
    Button btn_search;
    View noInfo;
    RecyclerView mRecyclerView;
    ArrayList<Uri> mArrayUri;
    ArrayList<String> catNames;
    Object[] IndexArray;
    int cnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_album);

        mRecyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.editText);
        btn_search = findViewById(R.id.btn_search);
        noInfo = findViewById(R.id.noInfo);
        noInfo.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);

        cnt = 0;
        catNames = new ArrayList<>();
        catNames.add("blackcat");
        catNames.add("hurricane");
        catNames.add("치즈");
        catNames.add("카오스");

        mArrayUri = new ArrayList<>();
        IndexArray = new Object[catNames.size()];

        mDatabase = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://catproj.appspot.com/");
        storageRef = storage.getReference();

        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mCustomImageAdapter = new CustomImageAdapter(R.layout.row, getApplicationContext(), mArrayUri);
        mRecyclerView.setAdapter(mCustomImageAdapter);

        showRecyclerView();

        editText.setOnKeyListener((v, keyCode, event) -> {
            if( keyCode == KeyEvent.KEYCODE_ENTER ){
                searchName();
                return true;
            }
            return false;
        });

        btn_search.setOnClickListener(v -> searchName());

        mCustomImageAdapter.setOnItemClickListener((view, position) -> {
            Log.d("CLICKED", "clicked " + IndexArray[position]);
            Intent intent1 = new Intent(getApplicationContext(), showCatInfo.class);
            intent1.putExtra("catName", IndexArray[position].toString());
            startActivity(intent1);
        });
    } // End onCreate();

    /*
    DB에서 대표 이미지 들고 와서 리사이클러뷰 보여주기
     */
    public void showRecyclerView(){
        String docPath = "catImgNum/num";
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
                        for(int i = 0; i < catNames.size(); i++){
                            String catName = catNames.get(i);
                            long num = 0;
                            if( (ob = getDB.get(catName)) != null ){
                                num = (Long)ob;
                            }
                            Log.d("GETURI", catName + "/" + num);
                            storageRef.child(catName + "/" + num + ".jpg").getDownloadUrl().addOnCompleteListener(task1 -> {
                                if( task1.isSuccessful() ){
                                    IndexArray[cnt] = catName;
                                    cnt++;
                                    mArrayUri.add(task1.getResult());
                                    Log.d("GETURI!!", "Success");
                                    mCustomImageAdapter.notifyDataSetChanged();
                                }
                                else{
                                    Log.d("GETURI!!", "Fail");
                                }
                            });
                        }
                    }
                    else{
                        Log.d("SHOW", "Error show DB", task.getException());
                    }
                });
    } // End showRecyclerView();

    /*
    DB에 별명이 있는지 검색해서 결과 보여줌
     */
    public void searchName(){
        noInfo.setVisibility(View.INVISIBLE);
        String searchName = editText.getText().toString();
        if( searchName.equals("") ){
            mCustomImageAdapter.setArrayUri(mArrayUri);
            mCustomImageAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            return;
        }
        String docPath = "catImgNum/names";
        mDatabase.document(docPath)
                .get()
                .addOnCompleteListener(task -> {
                    if( task.isSuccessful() && task.getResult().getData() != null ){
                        Map<String, Object> getDB = task.getResult().getData();
                        Object ob;
                        if( (ob = getDB.get(searchName)) != null ){
                            String catName = ob.toString();
                            ArrayList<Uri> catNameUri = new ArrayList<>();
                            for(int i = 0; i < catNames.size(); i++){
                                if( IndexArray[i].toString().equals(catName) ){
                                    catNameUri.add(mArrayUri.get(i));
                                    break;
                                }
                            }
                            mCustomImageAdapter.setArrayUri(catNameUri);
                            mCustomImageAdapter.notifyDataSetChanged();
                            mRecyclerView.setVisibility(View.VISIBLE);
                        }
                        else{
                            noInfo.setVisibility(View.VISIBLE);
                            mRecyclerView.setVisibility(View.INVISIBLE);
                        }
                    }
                    else{
                        Log.d("SHOW", "Error show DB", task.getException());
                    }
                });
    } // End searchName();

}
