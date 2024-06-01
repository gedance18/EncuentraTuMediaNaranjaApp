package com.example.encuentratumedianaranja;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class list_chats_fragment extends Fragment {
    private FirebaseFirestore db;
    private String currentUserId;
    private ProfileAdapter adapter;
    private List<Profile> profiles;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_chats, container, false);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view_profiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        profiles = new ArrayList<>();
        adapter = new ProfileAdapter(profiles);
        recyclerView.setAdapter(adapter);

        loadChats();

        return rootView;
    }

    private void loadChats() {
        db.collection("chats").document(currentUserId).collection("userChats")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("list_chats_fragment", "Error loading chats", e);
                        return;
                    }
                    profiles.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String matchedUserId = document.getId();
                        loadUserProfile(matchedUserId);
                    }
                    Log.d("list_chats_fragment", "Chats loaded: " + profiles.size());
                });
    }

    private void loadUserProfile(String userId) {
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String name = task.getResult().getString("name");
                String photoUrl = task.getResult().getString("profileImageUrl");
                String chatId = userId; // Asumimos que el userId es el chatId

                profiles.add(new Profile(photoUrl, name, chatId));
                adapter.notifyDataSetChanged();
                Log.d("list_chats_fragment", "User profile loaded: " + name + ", chatId: " + chatId);
            } else {
                Log.e("list_chats_fragment", "Error loading user profile", task.getException());
            }
        });
    }



    public static class Profile {
        private final String photoUrl;
        private final String name;
        private final String chatId;

        public Profile(String photoUrl, String name, String chatId) {
            this.photoUrl = photoUrl;
            this.name = name;
            this.chatId = chatId;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public String getName() {
            return name;
        }

        public String getChatId() {
            return chatId;
        }
    }


    public static class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {
        private final List<Profile> profiles;

        public ProfileAdapter(List<Profile> profiles) {
            this.profiles = profiles;
        }

        @NonNull
        @Override
        public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_chats, parent, false);
            return new ProfileViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
            Profile profile = profiles.get(position);
            holder.bind(profile);
        }

        @Override
        public int getItemCount() {
            return profiles.size();
        }

        public static class ProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final CircleImageView fotoPerfil;
            private final TextView nombre;
            private Profile profile;

            public ProfileViewHolder(@NonNull View itemView) {
                super(itemView);
                fotoPerfil = itemView.findViewById(R.id.foto_perfil);
                nombre = itemView.findViewById(R.id.nombre);
                itemView.setOnClickListener(this);
            }

            public void bind(Profile profile) {
                this.profile = profile;
                // Cargar imagen usando Glide
                Glide.with(itemView.getContext())
                        .load(profile.getPhotoUrl())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(fotoPerfil);
                nombre.setText(profile.getName());
                Log.d("ProfileViewHolder", "Profile name (chatId): " + profile.getName() + ", chatId: " + profile.getChatId());
            }

            @Override
            public void onClick(View view) {
                // Implementar navegación a ChatFragment
                Bundle bundle = new Bundle();
                String chatId = profile.getChatId(); // Asegúrate de pasar el chatId correcto
                Log.d("ProfileViewHolder", "Navigating to ChatFragment with chatId: " + chatId);
                bundle.putString("CHAT_ID", chatId);
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_list_chats_fragment_to_chatFragment, bundle);
            }
        }
    }
}




