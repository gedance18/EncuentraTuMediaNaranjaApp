package com.example.encuentratumedianaranja.model;

import com.google.firebase.firestore.FieldValue;

public class Match {
    private String user1Id;
    private String user2Id;
    private Object timestamp;

    public Match() {
        // Constructor vacío requerido por Firestore
    }

    public Match(String user1Id, String user2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.timestamp = FieldValue.serverTimestamp();
    }

    public String getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(String user1Id) {
        this.user1Id = user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(String user2Id) {
        this.user2Id = user2Id;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}