package com.example.encuentratumedianaranja.model;

public class Dislike {
    private String userId;

    public Dislike() {
        // Constructor vacío requerido
    }

    public Dislike(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
