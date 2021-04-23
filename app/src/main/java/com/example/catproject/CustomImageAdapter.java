package com.example.catproject;

import android.content.Context;
import android.database.DataSetObservable;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

public class CustomImageAdapter extends BaseAdapter {
    private FirebaseFirestore mDatabase;
    ArrayList<Bitmap> mArrayBM;
    int num = 0;
    Context mContext;

    public String TAG = "Gallery Adapter Example :: ";

    public CustomImageAdapter(Context context){
        this.mContext = context;

        mArrayBM = new ArrayList<>();
    }

    @Override
    public int getCount() {

        Log.d("GRID", "num => "+num);return num;
    }

    @Override
    public Object getItem(int position) {
        return mArrayBM.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        ImageView imageView = null;
        if( view == null ){

            Log.d("GRID", "null imgview");
            imageView = new ImageView(mContext);
        }
        else{
            imageView = (ImageView) view;

            Log.d("GRID", "not null imgview");
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        imageView.setLayoutParams(new GridView.LayoutParams(params));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageBitmap(mArrayBM.get(index));

        Log.d("GRID", "send imgview");

        return imageView;
    }

}
