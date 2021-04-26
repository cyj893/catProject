package com.example.catproject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
    ImageButton btn_download;

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
        btn_download = findViewById(R.id.btn_download);

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
        btn_download.setOnClickListener(v -> {
            Glide.with(getApplicationContext()).asBitmap().load(mArrayUri.get(nowPos))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            try {
                                Date currentTime = Calendar.getInstance().getTime();
                                String date_text = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault()).format(currentTime);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    saveBitmap(getApplicationContext(), resource, Bitmap.CompressFormat.JPEG, "image/jpeg", "Cat-"+date_text+".jpg");
                                }
                                else{
                                    saveBitmapToJpeg(resource, date_text);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void saveBitmap(@NonNull final Context context, @NonNull final Bitmap bitmap,
                           @NonNull final Bitmap.CompressFormat format,
                           @NonNull final String mimeType,
                           @NonNull final String displayName) throws IOException {
        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
        final ContentResolver resolver = context.getContentResolver();
        Uri uri = null;
        try{
            final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uri = resolver.insert(contentUri, values);
            if( uri == null )
                throw new IOException("Failed to create new MediaStore record.");
            try( final OutputStream stream = resolver.openOutputStream(uri) ){
                if (stream == null)
                    throw new IOException("Failed to open output stream.");
                if (!bitmap.compress(format, 100, stream))
                    throw new IOException("Failed to save bitmap.");
            }
            Toast.makeText(getApplicationContext(), "파일을 저장했습니다", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            if( uri != null ){
                resolver.delete(uri, null, null);
            }
            throw e;
        }
    }

    public void saveBitmapToJpeg(Bitmap bitmap, String fileName) {
        File tempFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
        try {
            tempFile.createNewFile();
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            Toast.makeText(getApplicationContext(), "파일을 저장했습니다", Toast.LENGTH_SHORT).show();
        } catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch(IOException exception){
            Log.e("IOException", exception.getMessage());
        }
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
