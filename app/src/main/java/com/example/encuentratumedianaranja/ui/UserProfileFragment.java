package com.example.encuentratumedianaranja.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.encuentratumedianaranja.R;
import com.example.encuentratumedianaranja.model.User;

// UserProfileFragment.java
public class UserProfileFragment extends Fragment {
    private ImageView profileImage;
    private TextView profileName;
    private TextView profileDescription;

    private User selectedUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        profileImage = view.findViewById(R.id.profileImage);
        profileName = view.findViewById(R.id.profileName);
        profileDescription = view.findViewById(R.id.profileDescription);

        // Obtén el usuario seleccionado (esto puede ser pasado como un argumento del fragmento)
        if (getArguments() != null) {
            selectedUser = (User) getArguments().getSerializable("selectedUser");
            loadUserProfile();
        }

        return view;
    }

    private void loadUserProfile() {
        if (selectedUser != null) {
            // Carga la información del usuario seleccionado en los views
            profileName.setText(selectedUser.getName());
            profileDescription.setText(selectedUser.getDescription());
            // Carga la imagen de perfil usando Glide o cualquier otra librería de carga de imágenes
            Glide.with(this).load(selectedUser.getProfileImageUrl()).into(profileImage);
        }
    }
}
