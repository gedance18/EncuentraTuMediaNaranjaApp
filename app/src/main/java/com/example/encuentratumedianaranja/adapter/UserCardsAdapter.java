package com.example.encuentratumedianaranja.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.encuentratumedianaranja.R;
import com.example.encuentratumedianaranja.model.User;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UserCardsAdapter extends BaseAdapter {
    private Context context;
    private List<User> users;

    public UserCardsAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_user_card, parent, false);
        }

        TextView userName = convertView.findViewById(R.id.user_name);
        ImageView profileImage = convertView.findViewById(R.id.profile_image);
        Button buttonViewProfile = convertView.findViewById(R.id.buttonViewProfile);
        User user = users.get(position);

        userName.setText(user.getName());
        Glide.with(context).load(user.getProfileImageUrl())
                .apply(RequestOptions.circleCropTransform())
                .into(profileImage);

        buttonViewProfile.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("userId", user.getUid());
            Navigation.findNavController(v).navigate(R.id.action_HomeFragment_to_profileFragment, bundle);
        });

        return convertView;
    }
}
