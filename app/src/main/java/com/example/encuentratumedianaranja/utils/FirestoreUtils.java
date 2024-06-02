package com.example.encuentratumedianaranja.utils;

import android.util.Log;

import com.example.encuentratumedianaranja.model.Message;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;

public class FirestoreUtils {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void createChat(String user1Id, String user2Id) {
        String chatId = getChatId(user1Id, user2Id);

        // Crear documento para el usuario 1 con el ID del usuario 2
        HashMap<String, Object> chatData = new HashMap<>();
        chatData.put("chatId", chatId);
        chatData.put("userId", user2Id);

        db.collection("chats").document(user1Id)
                .collection("userChats").document(user2Id)
                .set(chatData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("FirestoreUtils", "Chat created for user1Id with user2Id"))
                .addOnFailureListener(e -> Log.e("FirestoreUtils", "Error creating chat for user1Id with user2Id", e));

        // Crear documento para el usuario 2 con el ID del usuario 1
        chatData.put("userId", user1Id);  // Cambiamos el userId al user1Id
        db.collection("chats").document(user2Id)
                .collection("userChats").document(user1Id)
                .set(chatData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("FirestoreUtils", "Chat created for user2Id with user1Id"))
                .addOnFailureListener(e -> Log.e("FirestoreUtils", "Error creating chat for user2Id with user1Id", e));

        // Crear subcolección messages en el documento del chat
        db.collection("chats").document(chatId)
                .collection("messages")
                .document();  // Creando un documento vacío para inicializar la subcolección
    }

    public static String getChatId(String user1Id, String user2Id) {
        // Generar un ID de chat único para ambos usuarios
        return user1Id.compareTo(user2Id) < 0 ? user1Id + "_" + user2Id : user2Id + "_" + user1Id;
    }

    public static void createMessage(String chatId, String messageText, String senderId) {
        CollectionReference messagesRef = db.collection("chats").document(chatId).collection("messages");

        Message message = new Message(senderId, messageText, new com.google.firebase.Timestamp(new Date()));
        messagesRef.add(message)
                .addOnSuccessListener(documentReference -> Log.d("FirestoreUtils", "Message sent successfully: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e("FirestoreUtils", "Error sending message", e));
    }
}
