package com.example.encuentratumedianaranja;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
            addUserToFirestore(currentUser);
        } else {
            bottomNavigationView.setVisibility(View.GONE);
        }

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.signInFragment) {
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });

        if (savedInstanceState == null) {
            navController.navigate(R.id.HomeFragment);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(R.id.signOutFragment);
        } else {
            addUserToFirestore(currentUser);
        }
    }

    private void addUserToFirestore(FirebaseUser user) {
        if (user != null) {
            String uid = user.getUid();
            String name = user.getDisplayName() != null ? user.getDisplayName() : "User";
            String profileImageUrl = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : "";

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("uid", uid);
            userMap.put("name", name);
            userMap.put("profileImageUrl", profileImageUrl);

            db.collection("users").document(uid).set(userMap, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User added successfully to Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error adding user to Firestore", e));
        }
    }
}
