package com.example.catproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
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
    Button btn_goMain;
    Button btn_edit;
    public GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_catinfo);

        Log.d("Marker", "get intent");
        Intent intent = getIntent();
        String catName = intent.getStringExtra("catName");

        mGridView = (GridView)findViewById(R.id.gridView);
        textViewName = findViewById(R.id.show_name);
        textViewName.setMovementMethod(new ScrollingMovementMethod());
        textViewFeatures = findViewById(R.id.show_features);
        textViewFeatures.setMovementMethod(new ScrollingMovementMethod());
        btn_goMain = findViewById(R.id.btn_goMain);
        btn_edit = findViewById(R.id.btn_edit);




        CustomImageAdapter mCustomImageAdapter = new CustomImageAdapter(this);


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
                            mCustomImageAdapter.num = Integer.parseInt(getDB.get("num").toString());

                            Log.d("SHOW", catName + " => " + features + " " + mCustomImageAdapter.num);
                            textViewFeatures.setText(features);
                            for(int i = 1; i < mCustomImageAdapter.num+1; i++){
                                String key = "img" + i;
                                String simg = getDB.get(key).toString();
                                Bitmap bm = Info.StringToBitmap(simg);
                                if( bm != null ) mCustomImageAdapter.mArrayBM.add(bm);
                            }

                            mGridView.setAdapter(mCustomImageAdapter);
                        }
                        else{
                            Log.d("SHOW", "Error show DB", task.getException());
                        }
                    }
                });


        btn_goMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), showMap.class);
                startActivity(intent);
            }
        });

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), showMap.class);
                startActivity(intent);
            }
        });


//        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getApplicationContext(), mCustomImageAdapter.getItemPath(position), Toast.LENGTH_LONG).show();
//            }
//        });



    }




}
