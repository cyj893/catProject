package com.example.catproject;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class editInfo extends Activity {

    private FirebaseFirestore mDatabase;
    private ArrayList<Uri> mArrayUri;

    LinearLayout namesSpace;
    LinearLayout featuresSpace;
    LinearLayout imageSpace;
    Button btn_addName;
    Button btn_addFeature;
    Button btn_addImg;
    TextView addImgInfo;
    Button btn_cancel;
    Button btn_submit;

    String[] names;
    String[] features;
    long num = 0;
    int changed = 0;
    int prevsize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_edit_info);


        Intent intent = getIntent();
        names = intent.getStringExtra("names").split("\\(endline\\)");
        features = intent.getStringExtra("features").split("\\(endline\\)");

        mArrayUri = new ArrayList<>();
        namesSpace = findViewById(R.id.namesSpace);
        featuresSpace = findViewById(R.id.featuresSpace);
        imageSpace = findViewById(R.id.imageSpace);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_submit = findViewById(R.id.btn_submit);

        for (String name : names) {
            createTextView(name + ", ", namesSpace);
        }

        for (String feature : features) {
            createTextView("#" + feature + ", ", featuresSpace);
        }

        mDatabase = FirebaseFirestore.getInstance();

        btn_addName = findViewById(R.id.btn_addName);
        btn_addFeature = findViewById(R.id.btn_addFeature);
        btn_addImg = findViewById(R.id.btn_addImg);
        addImgInfo = findViewById(R.id.addImgInfo);

        btn_addName.setOnClickListener(v -> createEditView(namesSpace, 0));
        btn_addFeature.setOnClickListener(v -> createEditView(featuresSpace, 1));
        btn_addImg.setOnClickListener(v -> getImgFromAlbum() );
        btn_cancel.setOnClickListener(v -> onBackPressed() );
        btn_submit.setOnClickListener(v -> {
            if( mArrayUri.size() <= 0 && changed <= 0 ) return;
            if( mArrayUri.size() > 0 ){
                uploadFile(names[0]);
            }
            if( changed != 0 ){
                TextView tv;
                String namesString = names[0];
                Map<String, Object> data = new HashMap<>();
                Map<String,Object> deletes = new HashMap<>();
                int count = namesSpace.getChildCount();
                for(int i = 1; i < count; i++) {
                    tv = (TextView)namesSpace.getChildAt(i);
                    String name = tv.getText().toString().replace(", ", "");
                    if( tv.getCurrentTextColor() == Color.parseColor("#9F9F9F") ){
                        namesString = namesString + "(endline)" + name;
                        data.put(name, names[0]);
                    }
                    else{
                        deletes.put(name, FieldValue.delete());
                    }
                }
                Log.d("CHANGEDSTRING", namesString);
                mDatabase.document("catIMG/" + names[0]).update("names", namesString);
                mDatabase.document("catImgNum/names").set(data, SetOptions.merge());
                mDatabase.document("catImgNum/names").update(deletes);

                String featuresString = "";
                count = featuresSpace.getChildCount();
                if( count >= 1 ){
                    tv = (TextView)featuresSpace.getChildAt(0);
                    featuresString = tv.getText().toString();
                }
                for(int i = 1; i < count; i++) {
                    tv = (TextView)featuresSpace.getChildAt(i);
                    if( tv.getCurrentTextColor() == Color.parseColor("#9F9F9F") ) {
                        featuresString = featuresString + "(endline)" + tv.getText().toString();
                    }
                }
                featuresString = featuresString.replace(", ", "").replace("#", "");
                Log.d("CHANGEDSTRING", featuresString);
                mDatabase.document("catIMG/" + names[0]).update("features", featuresString);
            }
            onBackPressed();
        });

    }

    public void createTextView(String value, LinearLayout linearLayout){
        TextView textView = new TextView(getApplicationContext());
        textView.setText(value);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(param);
        textView.setTextColor(Color.parseColor("#9F9F9F"));
        textView.setTextSize(15);

        textView.setOnLongClickListener(v -> {
            if( textView.getText().toString().equals(names[0] + ", ") ) return true;
            if( textView.getCurrentTextColor() == Color.parseColor("#D6D6D6") ){
                textView.setTextColor(Color.parseColor("#9F9F9F"));
                changed--;
            }
            else{
                textView.setTextColor(Color.parseColor("#D6D6D6"));
                changed++;
            }
            return true;
        });

        linearLayout.addView(textView);
    }

    public void createEditView(LinearLayout linearLayout, int hashTag){
        EditText editText = new EditText(getApplicationContext());
        editText.setHint("입력하세요");
        editText.setEms(10);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(param);
        editText.setTextSize(15);

        editText.setOnKeyListener((v, keyCode, event) -> {
            if( keyCode == KeyEvent.KEYCODE_ENTER ){
                if( hashTag == 1 ){
                    createTextView("#" + editText.getText().toString() + ", ", linearLayout);
                }
                else{
                    createTextView(editText.getText().toString() + ", ", linearLayout);
                }
                editText.setVisibility(View.GONE);
                changed++;
                return true;
            }
            return false;
        });

        linearLayout.addView(editText);
    }

    public void createImageView(){
        for(int i = prevsize; i < mArrayUri.size(); i++){
            ImageView imageView = new ImageView(getApplicationContext());
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(convertDPtoPX(100), convertDPtoPX(100));
            imageView.setLayoutParams(param);
            imageView.setTag( "iv" + i );
            imageView.setOnLongClickListener(v -> {
                int index = Integer.parseInt(imageView.getTag().toString().replaceAll("[^0-9]", ""));
                mArrayUri.set(index, null);
                imageView.setVisibility(View.GONE);
                return true;
            });
            Glide.with(getApplicationContext()).load(mArrayUri.get(i)).transform(new CenterCrop()).into(imageView);
            imageSpace.addView(imageView);
        }
        prevsize = mArrayUri.size();
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
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
                                if( mArrayUri.get(i) == null ) continue;
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