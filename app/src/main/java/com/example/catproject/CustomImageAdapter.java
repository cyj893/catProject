package com.example.catproject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CustomImageAdapter extends RecyclerView.Adapter<CustomImageAdapter.ViewHolder> {
    private FirebaseFirestore mDatabase;
    ArrayList<Uri> mArrayUri;
    int itemLayout;
    Context mContext;

    public String TAG = "Gallery Adapter Example :: ";

    public CustomImageAdapter(int itemLayout, Context context){
        this.itemLayout = itemLayout;
        this.mContext = context;
        mArrayUri = new ArrayList<>();
    }

    @Override public int getItemCount() {
        return mArrayUri.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image;

        ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }

    @NonNull
    @Override
    public CustomImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomImageAdapter.ViewHolder holder, int position) {

        Glide.with(mContext).load(mArrayUri.get(position)).into(holder.image);

        Log.d("GRID", "send imgview");
    }

}
