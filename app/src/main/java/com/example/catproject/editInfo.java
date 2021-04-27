package com.example.catproject;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;

public class editInfo extends Activity {

    private FirebaseFirestore mDatabase;
    private ArrayList<Uri> mArrayUri;
    //private ArrayList<Integer> indexes;

    LinearLayout namesSpace;
    LinearLayout featuresSpace;
    LinearLayout imageSpace;
    Button btn_addImg;
    TextView addImgInfo;
    Button btn_cancel;
    Button btn_submit;

    String[] names;
    String[] features;
    long num = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_edit_info);


        Intent intent = getIntent();
        names = intent.getStringExtra("names").split("\\(endline\\)");
        features = intent.getStringExtra("features").split("\\(endline\\)");

        mArrayUri = new ArrayList<>();
        //indexes = new ArrayList<>();
        namesSpace = findViewById(R.id.namesSpace);
        featuresSpace = findViewById(R.id.featuresSpace);
        imageSpace = findViewById(R.id.imageSpace);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_submit = findViewById(R.id.btn_submit);

        for(int i = 0; i < names.length - 1; i++){
            createTextView(names[i] + ", ", namesSpace);
        }
        createTextView(names[names.length-1], namesSpace);

        for(int i = 0; i < features.length - 1; i++){
            createTextView("#" + features[i] + ", ", featuresSpace);
        }
        createTextView("#" + features[features.length-1], featuresSpace);

        mDatabase = FirebaseFirestore.getInstance();

        btn_addImg = findViewById(R.id.btn_addImg);
        addImgInfo = findViewById(R.id.addImgInfo);

        btn_addImg.setOnClickListener(v -> getImgFromAlbum() );
        btn_cancel.setOnClickListener(v -> onBackPressed() );
        btn_submit.setOnClickListener(v -> {
            if( mArrayUri.size() > 0 ){
                uploadFile(names[0]);
            }
        });

    }

    public void createTextView(String value, LinearLayout linearLayout){
        TextView textView = new TextView(getApplicationContext());
        textView.setText(value);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(param);

//        textView.setOnClickListener(v -> {
//            ;
//        });

        linearLayout.addView(textView);
    }

    public void createImageView(){
        for(int i = 0; i < mArrayUri.size(); i++){
            ImageView imageView = new ImageView(getApplicationContext());
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(convertDPtoPX(100), convertDPtoPX(100));
            imageView.setBackgroundColor(Color.parseColor("#000000"));
            imageView.setLayoutParams(param);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                int id = View.generateViewId();
//                Log.d("ID MaKE", String.valueOf(id));
//                imageView.setId(id);
//                indexes.add(imageView.getId());
//            }
//            imageView.setOnClickListener(v -> {
//                int id = imageView.getId();
//                for(int j = 0; j < indexes.size(); j++){
//                    if( indexes.get(j) == id ){
//                        mArrayUri.remove(j);
//                        imageView.setVisibility(View.GONE);
//                        break;
//                    }
//                }
//            });
            Glide.with(getApplicationContext()).load(mArrayUri.get(i)).transform(new CenterCrop()).into(imageView);
            imageSpace.addView(imageView);
        }
        addImgInfo.setVisibility(View.GONE);
    }

    public int convertDPtoPX(int dp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public void getImgFromAlbum() {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mArrayUri = new ArrayList<>();

        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            // Get the Image from data
            if (data.getClipData() != null) {
                ClipData mClipData = data.getClipData();
                int cnt = mClipData.getItemCount();
                for (int i = 0; i < cnt; i++) {
                    Uri imageuri = mClipData.getItemAt(i).getUri();
                    mArrayUri.add(imageuri);
                }
            }
            else {
                Uri imageuri = data.getData();
                mArrayUri.add(imageuri);
            }
            Log.d("IMG", "GOT IMG");
            createImageView();
        }
        else{
            Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
        }

    }

    //upload the file
    private void uploadFile(String catName) {
        if (mArrayUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();

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
                            if( (ob = getDB.get("num")) != null ){
                                num = (Long)ob;
                            }
                            for(int i = 0; i < mArrayUri.size(); i++){
                                Uri filePath = mArrayUri.get(i);
                                String filename = (++num) + ".jpg";
                                StorageReference storageRef = storage.getReferenceFromUrl("gs://catproj.appspot.com/").child( catName + "/" + filename);
                                storageRef.putFile(filePath)
                                        .addOnSuccessListener(taskSnapshot -> Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show())
                                        .addOnProgressListener(taskSnapshot -> {
                                            @SuppressWarnings("VisibleForTests")
                                            double progress = (100f * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                        });
                            }
                            mDatabase.document(docPath).update("num", num);
                            mDatabase.document("catImgNum/num").update(catName, num);
                            onBackPressed();
                        }
                        else{
                            Log.d("SHOW", "Error show DB", task.getException());
                        }
                    });
            num = 0;
        } else {
            Toast.makeText(getApplicationContext(), "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
        }
    } // End uploadFile()


}