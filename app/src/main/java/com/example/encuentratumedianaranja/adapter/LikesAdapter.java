package com.example.encuentratumedianaranja.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.encuentratumedianaranja.R;
import com.example.encuentratumedianaranja.model.User;

import java.util.List;

public class LikesAdapter extends RecyclerView.Adapter<LikesAdapter.ViewHolder> {
    private Context context;
    private List<User> likedUsers;

    public LikesAdapter(Context context, List<User> likedUsers) {
        this.context = context;
        this.likedUsers = likedUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_liked_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = likedUsers.get(position);
        holder.userName.setText(user.getName());
        Glide.with(context).load(user.getProfileImageUrl()).into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return likedUsers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        ImageView profileImage;

        ViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            profileImage = itemView.findViewById(R.id.profile_image);
        }
    }
}
