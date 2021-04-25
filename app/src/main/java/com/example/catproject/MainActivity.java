package com.example.catproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore mDatabase;
    private ArrayList<Uri> mArrayUri;
    long num = 0;
    String[] catNames;
    String allNames = "";

    Spinner spinner;
    String selected;
    Spinner spinner2;
    String selected2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("LOGIN", "signInAnonymously:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                    } else {
                        Log.w("LOGIN", "signInAnonymously:failure", task.getException());
                    }
                });

        //verifyEmailLink();
        mDatabase = FirebaseFirestore.getInstance();


        Button btn_map = findViewById(R.id.btn_map);
        btn_map.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), showMap.class);
            startActivity(intent);
        });

        getAllNames();


        spinner = findViewById(R.id.spinner);


        EditText editText_name = findViewById(R.id.editText_name);
        EditText editText_features = findViewById(R.id.editText_features);
        spinner2 = findViewById(R.id.spinner2);
        String[] types = {"black1", "black2", "cheese", "godeung", "chaos", "samsaek"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected2 = types[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selected2 = types[0];
            }
        });
        Button btn_uploadNewCat = findViewById(R.id.btn_uploadNewCat);
        btn_uploadNewCat.setOnClickListener(v -> {
            String getCatName = editText_name.getText().toString();
            String getFeature = editText_features.getText().toString();

            HashMap result = new HashMap<>();
            result.put("name", getCatName);
            result.put("type", selected2);
            int pm = 1; int pm2 = 1;
            if( Math.random() < 0.5 ) pm = -1;;
            if( Math.random() < 0.5 ) pm2 = -1;
            result.put("latitude", 35.233 + pm * Math.random()*0.005);
            result.put("longitude", 129.08 + pm2 * Math.random()*0.005);
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

            Map<String, Object> data = new HashMap<>();
            data.put("names", getCatName);
            data.put("features", getFeature);
            data.put("num", 0);
            mDatabase.collection("catIMG").document(getCatName)
                    .set(data);
            data = new HashMap<>();
            data.put(getCatName, getCatName);
            mDatabase.collection("catImgNum").document("names")
                    .set(data, SetOptions.merge());
            data = new HashMap<>();
            data.put(getCatName, 0);
            mDatabase.collection("catImgNum").document("num")
                    .set(data, SetOptions.merge());

            mDatabase.document("catImgNum/names")
                    .get()
                    .addOnCompleteListener(task -> {
                        if( task.isSuccessful() ){
                            Map<String, Object> getDB = task.getResult().getData();
                            if( getDB == null ){
                                Log.d("DB Error", "Error get DB no data", task.getException());
                                return;
                            }
                            Object ob;
                            if( (ob = getDB.get("allNames")) != null ){
                                allNames = ob.toString() + "," + getCatName;
                                Log.d("AllNames", "allnames " + allNames);
                                mDatabase.document("catImgNum/names").update("allNames", allNames);
                                getAllNames();
                            }
                            else{
                                Log.d("AllNames", "Error");
                            }
                        }
                        else{
                            Log.d("SHOW", "Error show DB", task.getException());
                        }
                    });
            editText_name.setText(null);
            editText_features.setText(null);

        });



        Button btn_goToAlbum = findViewById(R.id.btn_goToAlbum);
        btn_goToAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), showAlbum.class);
                startActivity(intent);
            }
        });

        Button btn_uploadImages = findViewById(R.id.btn_uploadImages);
        btn_uploadImages.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            getImgFromAlbum();
        });


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
            uploadFile(selected);
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
                                            double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
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


    public void getAllNames(){
        mDatabase.document("catImgNum/names")
                .get()
                .addOnCompleteListener(task -> {
                    if( task.isSuccessful() ){
                        Map<String, Object> getDB = task.getResult().getData();
                        if( getDB == null ){
                            Log.d("DB Error", "Error get DB no data", task.getException());
                            return;
                        }
                        Object ob;
                        if( (ob = getDB.get("allNames")) != null ){
                            catNames = ob.toString().split(",");
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,catNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selected = catNames[position];
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                selected = catNames[0];
                            }
                        });
                    }
                    else{
                        Log.d("SHOW", "Error show DB", task.getException());
                    }
                });
    } // End getAllNames()

//        Button btn_auth = (Button)findViewById(R.id.btn_auth);
//        btn_auth.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ActionCodeSettings actionCodeSettings =
//                        ActionCodeSettings.newBuilder()
//                                // URL you want to redirect back to. The domain (www.example.com) for this
//                                // URL must be whitelisted in the Firebase Console.
//                                .setUrl("https://catproject.page.link/63fF")
//                                // This must be true
//                                .setHandleCodeInApp(true)
//                                .setAndroidPackageName(
//                                        "com.example.catproject",
//                                        false, /* installIfNotAvailable */
//                                        "12"    /* minimumVersion */)
//                                .build();
//                String email = "cyj89317@naver.com";
//                FirebaseAuth auth = FirebaseAuth.getInstance();
//                Log.d("EMAIL", "getInstance.");
//                auth.sendSignInLinkToEmail(email, actionCodeSettings)
//                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if (task.isSuccessful()) {
//                                    Log.d("EMAIL", "Email sent.");
//                                }
//                                else{
//                                    Objects.requireNonNull (task.getException ()). printStackTrace ();}
//                            }
//                        });
//
//            }
//        });


//    public void verifyEmailLink(){
//
//        Log.d("DYNAMIC", "getDynamicLink start");
//
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        Intent intent = getIntent();
//        String emailLink = "";
//        if( intent.getData() != null ){
//            emailLink = intent.getData().toString();
//            Log.d("DYNAMIC", "INTENT: "+emailLink);
//        }
//        else{
//            Log.d("DYNAMIC", "No INTENT");
//            return;
//        }
//
//        // Confirm the link is a sign-in with email link.
//        if (auth.isSignInWithEmailLink(emailLink)) {
//            // Retrieve this from wherever you stored it
//            String email = "cyj89317@naver.com";
//            Log.d("DYNAMIC", "START confirm");
//
//            // The client SDK will parse the code from the link for you.
//            auth.signInWithEmailLink(email, emailLink)
//                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            if (task.isSuccessful()) {
//                                Log.d("SIGNIN", "Successfully signed in with email link!");
//                                AuthResult result = task.getResult();
//                                // You can access the new user via result.getUser()
//                                // Additional user info profile *not* available via:
//                                // result.getAdditionalUserInfo().getProfile() == null
//                                // You can check if the user is new or existing:
//                                // result.getAdditionalUserInfo().isNewUser()
//                            } else {
//                                Log.e("SIGNIN", "Error signing in with email link", task.getException());
//                            }
//                        }
//                    });
//        }
//
//        Log.d("DYNAMIC", "func end");
//    }
}