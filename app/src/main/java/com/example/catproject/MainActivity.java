package com.example.catproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyEmailLink();
        mDatabase = FirebaseFirestore.getInstance();

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
                String simg = BitmapToString(bitmap);

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
                mDatabase.collection("catinfo")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if( task.isSuccessful() ){
                                    for(QueryDocumentSnapshot document : task.getResult()){
                                        Log.d("SHOW", document.getId() + " => " + document.getData());
                                        Map<String, Object> getDB = document.getData();
                                        if( getDB.containsKey("img") ){
                                            String simg = getDB.get("img").toString();
                                            Bitmap bm = StringToBitmap(simg);
                                            ImageView imageView = (ImageView)findViewById(R.id.imageView);
                                            imageView.setImageBitmap(bm);
                                        }
                                    }
                                }
                                else{
                                    Log.d("SHOW", "Error show DB", task.getException());
                                }
                            }
                        });

            }
        });


        Button btn_auth = (Button)findViewById(R.id.btn_auth);
        btn_auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionCodeSettings actionCodeSettings =
                        ActionCodeSettings.newBuilder()
                                // URL you want to redirect back to. The domain (www.example.com) for this
                                // URL must be whitelisted in the Firebase Console.
                                .setUrl("https://catproject.page.link/63fF")
                                // This must be true
                                .setHandleCodeInApp(true)
                                .setAndroidPackageName(
                                        "com.example.catproject",
                                        false, /* installIfNotAvailable */
                                        "12"    /* minimumVersion */)
                                .build();
                String email = "cyj89317@naver.com";
                FirebaseAuth auth = FirebaseAuth.getInstance();
                Log.d("EMAIL", "getInstance.");
                auth.sendSignInLinkToEmail(email, actionCodeSettings)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("EMAIL", "Email sent.");
                                }
                                else{
                                    Objects.requireNonNull (task.getException ()). printStackTrace ();}
                            }
                        });

            }
        });

    }

    /*
     * String을 Bitmap으로 변환
     * */
    public static Bitmap StringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    /*
     * Bitmap을 String형으로 변환
     * */
    public static String BitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] bytes = baos.toByteArray();
        String temp = Base64.encodeToString(bytes, Base64.DEFAULT);
        return temp;
    }




    public void verifyEmailLink(){

        Log.d("DYNAMIC", "getDynamicLink start");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        String emailLink = "";
        if( intent.getData() != null ){
            emailLink = intent.getData().toString();
            Log.d("DYNAMIC", "INTENT: "+emailLink);
        }
        else{
            Log.d("DYNAMIC", "No INTENT");
            return;
        }

        // Confirm the link is a sign-in with email link.
        if (auth.isSignInWithEmailLink(emailLink)) {
            // Retrieve this from wherever you stored it
            String email = "cyj89317@naver.com";
            Log.d("DYNAMIC", "START confirm");

            // The client SDK will parse the code from the link for you.
            auth.signInWithEmailLink(email, emailLink)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("SIGNIN", "Successfully signed in with email link!");
                                AuthResult result = task.getResult();
                                // You can access the new user via result.getUser()
                                // Additional user info profile *not* available via:
                                // result.getAdditionalUserInfo().getProfile() == null
                                // You can check if the user is new or existing:
                                // result.getAdditionalUserInfo().isNewUser()
                            } else {
                                Log.e("SIGNIN", "Error signing in with email link", task.getException());
                            }
                        }
                    });
        }

        Log.d("DYNAMIC", "func end");
    }
}