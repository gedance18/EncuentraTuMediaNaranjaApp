package com.example.encuentratumedianaranja;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.encuentratumedianaranja.adapter.UserCardsAdapter;
import com.example.encuentratumedianaranja.model.Dislike;
import com.example.encuentratumedianaranja.model.Like;
import com.example.encuentratumedianaranja.model.Match;
import com.example.encuentratumedianaranja.model.Message;
import com.example.encuentratumedianaranja.model.User;
import com.example.encuentratumedianaranja.utils.FirestoreUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class HomeFragment extends Fragment {
    private Toolbar toolbar;
    private SwipeFlingAdapterView swipeFlingAdapterView;
    private UserCardsAdapter adapter;
    private List<User> users;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView likeIndicator;
    private TextView dislikeIndicator;
    private HashSet<String> dislikedUserIds;
    private HashSet<String> matchedUserIds;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        users = new ArrayList<>();
        dislikedUserIds = new HashSet<>();
        matchedUserIds = new HashSet<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        toolbar = view.findViewById(R.id.toolbar_home);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");

        toolbar.setNavigationOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            if (navController != null) {
                navController.navigate(R.id.signOutFragment);
            } else {
                Log.e("NavigationError", "NavController is null");
            }
        });

        ImageButton botonAtras = view.findViewById(R.id.boton_atras);
        if (botonAtras != null) {
            botonAtras.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.signOutFragment);
            });
        }

        swipeFlingAdapterView = view.findViewById(R.id.frame);
        adapter = new UserCardsAdapter(getContext(), users);
        swipeFlingAdapterView.setAdapter(adapter);

        likeIndicator = view.findViewById(R.id.like_indicator);
        dislikeIndicator = view.findViewById(R.id.dislike_indicator);

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
                showDialog("Dislike", "You have disliked " + user.getName());
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                User user = (User) dataObject;
                handleLike(user.getUid());
                showDialog("Like", "You have liked " + user.getName());
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Load more users if needed
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                if (scrollProgressPercent > 0) {
                    likeIndicator.setVisibility(View.VISIBLE);
                    dislikeIndicator.setVisibility(View.GONE);
                } else if (scrollProgressPercent < 0) {
                    likeIndicator.setVisibility(View.GONE);
                    dislikeIndicator.setVisibility(View.VISIBLE);
                } else {
                    likeIndicator.setVisibility(View.GONE);
                    dislikeIndicator.setVisibility(View.GONE);
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Navegar a la pantalla de inicio de sesión si no hay usuario autenticado
            Navigation.findNavController(requireView()).navigate(R.id.signOutFragment);
        } else {
            loadDislikesAndMatches(currentUser.getUid());
        }
    }

    private void loadDislikesAndMatches(String currentUserId) {
        // Cargar dislikes
        db.collection("Dislikes").document(currentUserId).collection("GivenDislikes").get()
                .addOnCompleteListener(dislikeTask -> {
                    if (dislikeTask.isSuccessful()) {
                        for (QueryDocumentSnapshot document : dislikeTask.getResult()) {
                            dislikedUserIds.add(document.getId());
                        }

                        // Cargar matches donde el usuario actual es user1
                        db.collection("Matches")
                                .whereEqualTo("user1Id", currentUserId)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                        matchedUserIds.add(document.getString("user2Id"));
                                    }
                                    // Cargar matches donde el usuario actual es user2
                                    db.collection("Matches")
                                            .whereEqualTo("user2Id", currentUserId)
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                                for (QueryDocumentSnapshot document : queryDocumentSnapshots2) {
                                                    matchedUserIds.add(document.getString("user1Id"));
                                                }
                                                loadUsers(currentUserId);
                                            });
                                });
                    } else {
                        Log.e("HomeFragment", "Error al obtener los dislikes: ", dislikeTask.getException());
                    }
                });
    }

    private void loadUsers(String currentUserId) {
        db.collection("users").get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        users.clear(); // Limpiar la lista antes de añadir nuevos usuarios
                        for (QueryDocumentSnapshot document : userTask.getResult()) {
                            String userId = document.getString("uid");
                            String name = document.getString("name");
                            String profileImageUrl = document.getString("profileImageUrl");

                            // Verificación de null antes de proceder
                            if (userId != null && name != null && profileImageUrl != null
                                    && !userId.equals(currentUserId)
                                    && !dislikedUserIds.contains(userId)
                                    && !matchedUserIds.contains(userId)
                                    && !name.isEmpty()) { // Verificación adicional para omitir el documento de prueba
                                users.add(new User(userId, name, profileImageUrl));
                                Log.d("HomeFragment", "Usuario añadido: " + name);
                            } else {
                                Log.e("HomeFragment", "Faltan datos para el usuario, está en la lista de dislikes o ya hay un match: " + userId);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (users.isEmpty()) {
                            Log.e("HomeFragment", "No se encontraron usuarios.");
                        }
                    } else {
                        Log.e("HomeFragment", "Error al obtener los usuarios: ", userTask.getException());
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
                    .addOnFailureListener(e -> Log.e("HomeFragment", "Error al dar like", e));
        }
    }

    private void handleDislike(String dislikedUserId) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("Dislikes").document(currentUserId)
                .collection("GivenDislikes").document(dislikedUserId)
                .set(new Dislike(dislikedUserId))
                .addOnSuccessListener(aVoid -> Log.d("HomeFragment", "User disliked"))
                .addOnFailureListener(e -> Log.e("HomeFragment", "Error disliking user", e));
    }

    private void checkForMatch(String currentUserId, String likedUserId) {
        db.collection("Likes").document(likedUserId)
                .collection("GivenLikes").document(currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        showMatchPopUp();
                        registerMatch(currentUserId, likedUserId);
                    }
                });
    }

    private void registerMatch(String user1, String user2) {
        db.collection("Matches").add(new Match(user1, user2))
                .addOnSuccessListener(documentReference -> {
                    Log.d("HomeFragment", "Match registrado con ID: " + documentReference.getId());
                    createChat(user1, user2);
                })
                .addOnFailureListener(e -> Log.e("HomeFragment", "Error al registrar el match", e));
    }

    private void showMatchPopUp() {
        new AlertDialog.Builder(getContext())
                .setTitle("¡Es un Match!")
                .setMessage("Has hecho match con un usuario.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void createChat(String user1Id, String user2Id) {
        FirestoreUtils.createChat(user1Id, user2Id);
    }

    private void createMessage(String chatId, String messageText, String senderId) {
        FirestoreUtils.createMessage(chatId, messageText, senderId);
    }

    private void showDialog(String title, String message) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : "";
    }
}
