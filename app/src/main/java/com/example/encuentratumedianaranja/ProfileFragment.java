package com.example.encuentratumedianaranja;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private CircleImageView imageViewProfile;
    private EditText editTextName, editTextDescripcion, editTextTipoDeRelacion, editTextEdad;
    private Button buttonSaveProfile, buttonChangePhoto, buttonExitProfile;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private String userId;
    private Uri imageUri;
    private boolean isOwnProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageViewProfile = view.findViewById(R.id.imageViewProfile);
        editTextName = view.findViewById(R.id.editTextName);
        editTextDescripcion = view.findViewById(R.id.editTextDescripcion);
        editTextTipoDeRelacion = view.findViewById(R.id.editText_tipo_de_relacion);
        editTextEdad = view.findViewById(R.id.editTextEdad);
        buttonSaveProfile = view.findViewById(R.id.buttonSaveProfile);
        buttonChangePhoto = view.findViewById(R.id.buttonChangePhoto);
        buttonExitProfile = view.findViewById(R.id.buttonExitProfile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        // Obtener el userId desde los argumentos o desde el usuario autenticado
        if (getArguments() != null && getArguments().getString("userId") != null) {
            userId = getArguments().getString("userId");
            isOwnProfile = false;
        } else {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getUid();
                isOwnProfile = true;
            } else {
                Toast.makeText(getActivity(), "No authenticated user found.", Toast.LENGTH_SHORT).show();
                return view;
            }
        }

        // Asegurarse de que userId no sea null
        if (userId != null) {
            loadUserProfile();
        } else {
            Toast.makeText(getActivity(), "User ID is null.", Toast.LENGTH_SHORT).show();
        }

        if (!isOwnProfile) {
            disableEditing();
            buttonExitProfile.setVisibility(View.VISIBLE);
            buttonExitProfile.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.action_profileFragment_to_homeFragment);
            });
        } else {
            buttonSaveProfile.setOnClickListener(v -> saveUserProfile());
            buttonChangePhoto.setOnClickListener(v -> openFileChooser());
        }

        return view;
    }

    private void disableEditing() {
        editTextName.setEnabled(false);
        editTextDescripcion.setEnabled(false);
        editTextTipoDeRelacion.setEnabled(false);
        editTextEdad.setEnabled(false);

        editTextName.setTextColor(getResources().getColor(android.R.color.white));
        editTextDescripcion.setTextColor(getResources().getColor(android.R.color.white));
        editTextTipoDeRelacion.setTextColor(getResources().getColor(android.R.color.white));
        editTextEdad.setTextColor(getResources().getColor(android.R.color.white));

        buttonSaveProfile.setVisibility(View.GONE);
        buttonChangePhoto.setVisibility(View.GONE);


    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewProfile.setImageURI(imageUri);
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference fileReference = storageReference.child(UUID.randomUUID().toString() + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        updateProfileImageUrl(imageUrl);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateProfileImageUrl(String imageUrl) {
        db.collection("users").document(userId)
                .update("profileImageUrl", imageUrl)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Profile image updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to update profile image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserProfile() {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            editTextName.setText(document.getString("name"));
                            editTextDescripcion.setText(document.getString("descripcion"));
                            editTextTipoDeRelacion.setText(document.getString("tipoDeRelacion"));
                            editTextEdad.setText(document.getString("edad"));
                            String profileImageUrl = document.getString("profileImageUrl");
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                // Cargar la imagen de perfil usando Glide o Picasso
                                Glide.with(this).load(profileImageUrl).into(imageViewProfile);
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserProfile() {
        String name = editTextName.getText().toString();
        String descripcion = editTextDescripcion.getText().toString();
        String tipoDeRelacion = editTextTipoDeRelacion.getText().toString();
        String edad = editTextEdad.getText().toString();

        db.collection("users").document(userId)
                .update("name", name, "descripcion", descripcion, "tipoDeRelacion", tipoDeRelacion, "edad", edad)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
