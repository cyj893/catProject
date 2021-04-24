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
    int itemLayout;
    Context mContext;
    ArrayList<Uri> mArrayUri;

    public CustomImageAdapter(int itemLayout, Context context, ArrayList<Uri> mArrayUri){
        this.itemLayout = itemLayout;
        this.mContext = context;
        this.mArrayUri = mArrayUri;
    }

    public void setArrayUri(ArrayList<Uri> mArrayUri){
        this.mArrayUri = mArrayUri;
    }

    @Override public int getItemCount() {
        return mArrayUri.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image;

        ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition() ;
                    if( pos != RecyclerView.NO_POSITION ){
                        if( mListener != null ) {
                            mListener.onItemClick(v, pos);
                        }
                    }
                }
            });
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


    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

}
