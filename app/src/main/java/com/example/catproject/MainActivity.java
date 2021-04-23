package com.example.catproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore mDatabase;
    private ImageView imageView;
    private ArrayList<Bitmap> mArrayBM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //verifyEmailLink();
        mDatabase = FirebaseFirestore.getInstance();
        mArrayBM = new ArrayList<>();


        imageView = (ImageView)findViewById(R.id.imageView);
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

                Drawable img = getResources().getDrawable(R.drawable.cat1);
                Bitmap bitmap = ((BitmapDrawable)img).getBitmap();
                String simg = Info.BitmapToString(bitmap);

                //hashmap 만들기
                HashMap result = new HashMap<>();
                result.put("name", getCatName);
                result.put("type", getType);
                result.put("img", simg);

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



//                Drawable img = getResources().getDrawable(R.drawable.cat1);
//                Bitmap bitmap = ((BitmapDrawable)img).getBitmap();
//                String simg = BitmapToString(bitmap);
//
//                //hashmap 만들기
//                HashMap result = new HashMap<>();
//                result.put("img1", simg);
//
//                mDatabase.collection("catIMG")
//                        .document("치즈")
//                        .set(result, SetOptions.merge());
            }
        });

        Button btn_showDB = (Button)findViewById(R.id.btn_showDB);
        btn_showDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });


    }



    public Bitmap UriToBitmap(Uri imageuri){
        Bitmap bm = null;
        try{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                bm = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), imageuri));
            }
            else {
                bm = MediaStore.Images.Media.getBitmap(getContentResolver(), imageuri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bm;
    }


    public void getImgFromAlbum() {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
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
                    // adding imageuri in array
                    Uri imageuri = mClipData.getItemAt(i).getUri();
                    Bitmap bm = UriToBitmap(imageuri);
                    if( bm != null ) mArrayBM.add(bm);
                }
                imageView.setImageBitmap(mArrayBM.get(0));
            }
            else {
                Uri imageuri = data.getData();
                Bitmap bm = UriToBitmap(imageuri);
                if( bm != null ) mArrayBM.add(bm);
                imageView.setImageBitmap(mArrayBM.get(0));
            }
        }
        else{
            Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
        }

    }


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