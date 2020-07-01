package com.innovati.botregister.Adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.innovati.botregister.R;

import java.util.List;

public class CameraAdapter extends RecyclerView.Adapter<CameraAdapter.ViewHolder> {

    private List<String> list;
    private Context mContext;
    private LinearLayout lyFoto;

    public CameraAdapter(List<String> list, Context context, LinearLayout lyFoto) {
        this.list = list;
        this.mContext = context;
        this.lyFoto = lyFoto;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Glide.with(mContext).asBitmap().load(list.get(position)).into(holder.image);
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (list.size() > 0) {
                    list.remove(position);
                    CameraAdapter.this.notifyDataSetChanged();
                    if (list.size() <= 0) {
                        lyFoto.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton button;
        ImageView image;
        LinearLayout layout;

        ViewHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.btn_image);
            image = itemView.findViewById(R.id.image_view);
            layout = itemView.findViewById(R.id.layout_view);
        }
    }
}