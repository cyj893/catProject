package com.example.catproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore mDatabase;
    private ImageView imageView;
    private ArrayList<Uri> mArrayUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("LOGIN", "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("LOGIN", "signInAnonymously:failure", task.getException());
                        }
                    }
                });

        //verifyEmailLink();
        mDatabase = FirebaseFirestore.getInstance();


        imageView = (ImageView)findViewById(R.id.imageView);
        Button btn_map = (Button)findViewById(R.id.btn_map);
        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), showMap.class);
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
            uploadFile();
        }
        else{
            Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
        }

    }


    //upload the file
    private void uploadFile() {
        if (mArrayUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();

            final int[] num = {0};
            String catName = "blackcat";
            String docPath = "catIMG/" + catName;
            mDatabase.document(docPath)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if( task.isSuccessful() ){
                                Map<String, Object> getDB = task.getResult().getData();
                                num[0] = Integer.parseInt(getDB.get("num").toString());

                                for(int i = 0; i < mArrayUri.size(); i++){
                                    Uri filePath = mArrayUri.get(i);
                                    String filename = (++num[0]) + ".jpg";
                                    StorageReference storageRef = storage.getReferenceFromUrl("gs://catproj.appspot.com/").child( catName + "/" + filename);
                                    storageRef.putFile(filePath)
                                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                                    @SuppressWarnings("VisibleForTests")
                                                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                                }
                                            });
                                }
                                mDatabase.document(docPath).update("num", num[0]);
                            }
                            else{
                                Log.d("SHOW", "Error show DB", task.getException());
                            }
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
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