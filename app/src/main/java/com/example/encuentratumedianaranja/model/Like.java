package com.example.encuentratumedianaranja.model;

import com.google.firebase.firestore.FieldValue;

public class Like {
    private String likedUserId;
    private Object timestamp;

    public Like() {
        // Constructor vacío requerido por Firestore
    }

    public Like(String likedUserId) {
        this.likedUserId = likedUserId;
        this.timestamp = FieldValue.serverTimestamp();
    }

    public String getLikedUserId() {
        return likedUserId;
    }

    public void setLikedUserId(String likedUserId) {
        this.likedUserId = likedUserId;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}