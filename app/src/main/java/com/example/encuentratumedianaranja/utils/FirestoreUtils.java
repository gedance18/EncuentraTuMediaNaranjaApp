package com.example.encuentratumedianaranja.utils;

import android.util.Log;

import com.example.encuentratumedianaranja.model.Message;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;

public class FirestoreUtils {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void createChat(String user1Id, String user2Id) {
        String chatId = db.collection("chats").document().getId();

        // Crear documento para el usuario 1
        db.collection("chats").document(user1Id)
                .collection("userChats").document(chatId)
                .set(new HashMap<>());

        // Crear documento para el usuario 2
        db.collection("chats").document(user2Id)
                .collection("userChats").document(chatId)
                .set(new HashMap<>());

        // Crear subcolección messages para ambos usuarios
        db.collection("chats").document(chatId)
                .collection("messages").document();
    }

    public static void createMessage(String chatId, String messageText, String senderId) {
        CollectionReference messagesRef = db.collection("chats").document(chatId).collection("messages");

        Message message = new Message(senderId, messageText, new Timestamp(new Date()));
        messagesRef.add(message)
                .addOnSuccessListener(documentReference -> Log.d("FirestoreUtils", "Message sent successfully: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e("FirestoreUtils", "Error sending message", e));
    }
}
