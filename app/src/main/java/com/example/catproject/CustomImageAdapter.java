package com.example.catproject;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CustomImageAdapter extends RecyclerView.Adapter<CustomImageAdapter.ViewHolder> {
    private OnItemClickListener mListener = null;
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
            image = itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition() ;
                if( pos != RecyclerView.NO_POSITION ){
                    if( mListener != null ) {
                        mListener.onItemClick(v, pos);
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
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

}
