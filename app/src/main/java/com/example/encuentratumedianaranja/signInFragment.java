package com.example.encuentratumedianaranja;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class signInFragment extends Fragment {
    private static final String TAG = "signInFragment";
    private NavController navController;
    private LinearLayout signInForm;
    private FirebaseAuth mAuth;
    private SignInButton googleSignInButton;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private FirebaseFirestore db;

    public signInFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in_fragmet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        signInForm = view.findViewById(R.id.signInForm);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        googleSignInButton = view.findViewById(R.id.googleSignInButton);

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            try {
                                firebaseAuthWithGoogle(GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class));
                            } catch (ApiException e) {
                                Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
                            }
                        }
                    }
                });

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accederConGoogle();
            }
        });
    }

    private void accederConGoogle() {
        GoogleSignInClient googleSignInClient =
                GoogleSignIn.getClient(requireActivity(), new
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build());
        activityResultLauncher.launch(googleSignInClient.getSignInIntent());
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        if (acct == null || signInForm == null) return;
        signInForm.setVisibility(View.GONE);
        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(acct.getIdToken(), null))
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.e(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            addUserToFirestore(user, acct);
                        } else {
                            Log.e(TAG, "signInWithCredential:failure", task.getException());
                            if (signInForm != null) {
                                signInForm.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }

    private void addUserToFirestore(FirebaseUser user, GoogleSignInAccount acct) {
        if (user != null) {
            String uid = user.getUid();
            String name = acct.getDisplayName() != null ? acct.getDisplayName() : "User";
            String profileImageUrl = (acct.getPhotoUrl() != null) ? acct.getPhotoUrl().toString() : "";

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("uid", uid);
            userMap.put("name", name);
            userMap.put("profileImageUrl", profileImageUrl);

            db.collection("users").document(uid).set(userMap, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User added successfully to Firestore");
                        actualizarUI(user, name, Uri.parse(profileImageUrl));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding user to Firestore", e);
                        if (signInForm != null) {
                            signInForm.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    private void actualizarUI(FirebaseUser currentUser, String personName, Uri personPhoto) {
        if (currentUser != null) {
            Bundle bundle = new Bundle();
            bundle.putString("personName", personName);
            bundle.putString("personPhotoUrl", personPhoto != null ? personPhoto.toString() : "");
            navController.navigate(R.id.HomeFragment, bundle);
        }
    }
}
