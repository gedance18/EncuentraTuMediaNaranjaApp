package com.example.encuentratumedianaranja.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.encuentratumedianaranja.R;
import com.example.encuentratumedianaranja.adapter.UserCardsAdapter;
import com.example.encuentratumedianaranja.model.Dislike;
import com.example.encuentratumedianaranja.model.Like;
import com.example.encuentratumedianaranja.model.Match;
import com.example.encuentratumedianaranja.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserCardsFragment extends Fragment {
    private SwipeFlingAdapterView swipeFlingAdapterView;
    private UserCardsAdapter adapter;
    private List<User> users;
    private FirebaseFirestore db;

    public UserCardsFragment() {
        // Constructor vacío requerido
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        users = new ArrayList<>();
        loadUsers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_cards, container, false);
        swipeFlingAdapterView = view.findViewById(R.id.frame);
        adapter = new UserCardsAdapter(getContext(), users);
        swipeFlingAdapterView.setAdapter(adapter);

        swipeFlingAdapterView.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                users.remove(0);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                User user = (User) dataObject;
                handleDislike(user.getUid());
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                User user = (User) dataObject;
                handleLike(user.getUid());
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Load more users if needed
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                // Handle scroll
            }
        });

        return view;
    }

    private void loadUsers() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getString("uid");
                            if (!userId.equals(currentUserId)) {
                                String name = document.getString("name");
                                String profileImageUrl = document.getString("profileImageUrl");
                                users.add(new User(userId, name, profileImageUrl));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("UserCardsFragment", "Error getting users: ", task.getException());
                    }
                });
    }

    private void handleLike(String likedUserId) {
        String currentUserId = getCurrentUserId();
        if (!likedUserId.isEmpty() && !currentUserId.isEmpty()) {
            db.collection("Likes").document(currentUserId)
                    .collection("GivenLikes").document(likedUserId)
                    .set(new Like(likedUserId))
                    .addOnSuccessListener(aVoid -> checkForMatch(currentUserId, likedUserId))
                    .addOnFailureListener(e -> Log.e("UserCardsFragment", "Error liking user", e));
        }
    }

    private void handleDislike(String dislikedUserId) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Dislikes").document(currentUserId)
                .collection("GivenDislikes").document(dislikedUserId)
                .set(new Dislike(dislikedUserId))
                .addOnSuccessListener(aVoid -> Log.d("UserCardsFragment", "User disliked"))
                .addOnFailureListener(e -> Log.e("UserCardsFragment", "Error disliking user", e));
    }

    private void checkForMatch(String currentUserId, String likedUserId) {
        db.collection("Likes").document(likedUserId)
                .collection("GivenLikes").document(currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        registerMatch(currentUserId, likedUserId);
                    }
                });
    }

    private void registerMatch(String user1, String user2) {
        db.collection("Matches").add(new Match(user1, user2))
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Match registered!", Toast.LENGTH_SHORT).show();
                    // Actualizar la lista de usuarios para que no se vuelvan a ver
                    removeMatchedUsersFromList(user1, user2);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("UserCardsFragment", "Error registering match", e));
    }

    private void removeMatchedUsersFromList(String user1Id, String user2Id) {
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getUid().equals(user1Id) || user.getUid().equals(user2Id)) {
                iterator.remove();
            }
        }
    }

    private String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : "";
    }
}
