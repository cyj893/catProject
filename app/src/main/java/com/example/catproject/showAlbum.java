package com.example.catproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

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

public class showAlbum extends AppCompatActivity {

    private FirebaseFirestore mDatabase;
    EditText editText;
    Button btn_search;
    View noInfo;
    public RecyclerView mRecyclerView;
    CustomImageAdapter mCustomImageAdapter;
    ArrayList<Uri> mArrayUri;
    ArrayList<String> catNames;
    Object[] IndexArray;
    static int cnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_album);

        Log.d("Album", "get intent");
        Intent intent = getIntent();

        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        editText = findViewById(R.id.editText);
        btn_search = findViewById(R.id.btn_search);
        noInfo = (LinearLayout)findViewById(R.id.noInfo);
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
        StorageReference storageRef = storage.getReference();

        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mCustomImageAdapter = new CustomImageAdapter(R.layout.row, getApplicationContext(), mArrayUri);
        mRecyclerView.setAdapter(mCustomImageAdapter);


        String docPath = "catImgNum/num";
        mDatabase.document(docPath)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if( task.isSuccessful() ){
                            Map<String, Object> getDB = task.getResult().getData();
                            for(int i = 0; i < catNames.size(); i++){
                                String catName = catNames.get(i);
                                int num = Integer.parseInt(getDB.get(catName).toString());
                                Log.d("GETURI", catName + "/" + num);
                                storageRef.child(catName + "/" + num + ".jpg").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if( task.isSuccessful() ){
                                            IndexArray[cnt] = catName;
                                            cnt++;
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

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_ENTER ){
                    Log.d("Enter", "getEnter");
                    Log.d("Enter", "Do searchName");
                    searchName();
                    return true;
                }
                return false;
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchName();
            }
        });


    }

    public void searchName(){
        noInfo.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        String searchName = editText.getText().toString();
        if( searchName.equals("") ){
            mCustomImageAdapter.setArrayUri(mArrayUri);
            mCustomImageAdapter.notifyDataSetChanged();
            return;
        }
        String docPath = "catImgNum/names";
        mDatabase.document(docPath)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if( task.isSuccessful() ){
                            Map<String, Object> getDB = task.getResult().getData();
                            if( getDB.containsKey(searchName) ){
                                String catName = getDB.get(searchName).toString();
                                ArrayList<Uri> catNameUri = new ArrayList<>();
                                for(int i = 0; i < catNames.size(); i++){
                                    if( IndexArray[i].toString().equals(catName) ){
                                        catNameUri.add(mArrayUri.get(i));
                                        break;
                                    }
                                }
                                mCustomImageAdapter.setArrayUri(catNameUri);
                                mCustomImageAdapter.notifyDataSetChanged();
                            }
                            else{
                                noInfo.setVisibility(View.VISIBLE);
                                mRecyclerView.setVisibility(View.INVISIBLE);
                            }
                        }
                        else{
                            Log.d("SHOW", "Error show DB", task.getException());
                        }
                    }
                });
    }

}
