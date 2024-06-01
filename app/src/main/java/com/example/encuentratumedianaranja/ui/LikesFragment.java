package com.example.encuentratumedianaranja.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.encuentratumedianaranja.R;
import com.example.encuentratumedianaranja.adapter.LikesAdapter;
import com.example.encuentratumedianaranja.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LikesFragment extends Fragment {
    private RecyclerView recyclerView;
    private LikesAdapter adapter;
    private List<User> likedUsers;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public LikesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        likedUsers = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_likes, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_likes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LikesAdapter(getContext(), likedUsers);
        recyclerView.setAdapter(adapter);
        loadLikedUsers();
        return view;
    }

    private void loadLikedUsers() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("Likes").document(currentUserId).collection("GivenLikes").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String likedUserId = document.getId();
                            db.collection("users").document(likedUserId).get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            String name = userDoc.getString("name");
                                            String profileImageUrl = userDoc.getString("profileImageUrl");
                                            likedUsers.add(new User(likedUserId, name, profileImageUrl));
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    } else {
                        Log.e("LikesFragment", "Error getting liked users: ", task.getException());
                    }
                });
    }
}
