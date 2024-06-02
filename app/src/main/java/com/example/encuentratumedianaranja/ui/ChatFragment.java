package com.example.encuentratumedianaranja.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.encuentratumedianaranja.R;
import com.example.encuentratumedianaranja.adapter.ChatAdapter;
import com.example.encuentratumedianaranja.model.Message;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.encuentratumedianaranja.utils.FirestoreUtils;

public class ChatFragment extends Fragment {

    private FirebaseFirestore db;
    private CollectionReference messagesRef;
    private RecyclerView recyclerViewMessages;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private EditText editTextMessage;
    private Button buttonSend;
    private String currentUserId;
    private String chatId;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        db = FirebaseFirestore.getInstance();

        // Obtén el ID del chat y el ID del usuario actual desde los argumentos
        if (getArguments() != null) {
            chatId = getArguments().getString("CHAT_ID");
        }

        if (chatId == null) {
            throw new IllegalArgumentException("chatId must not be null");
        }

        Log.d("ChatFragment", "Chat ID received: " + chatId);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        messagesRef = db.collection("chats").document(chatId).collection("messages");

        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext(), messageList);
        recyclerViewMessages.setAdapter(chatAdapter);

        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        ImageButton botonAtras = view.findViewById(R.id.boton_atras_chat);
        if (botonAtras != null) {
            botonAtras.setOnClickListener(v -> {
                Log.d("ChatFragment", "Navigating back to list_chats_fragment");
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.list_chats_fragment);
            });
        }

        loadMessages();

        return view;
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            FirestoreUtils.createMessage(chatId, messageText, currentUserId);
            editTextMessage.setText("");
        }
    }

    private void loadMessages() {
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("ChatFragment", "Error loading messages", e);
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            messageList.clear();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Message message = document.toObject(Message.class);
                                messageList.add(message);
                            }
                            chatAdapter.notifyDataSetChanged();
                            recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                            Log.d("ChatFragment", "Messages loaded: " + messageList.size());
                        }
                    }
                });
    }
}
