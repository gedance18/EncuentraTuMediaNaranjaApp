package com.example.encuentratumedianaranja.model;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String name;
    private String description;
    private String relationshipType;
    private String age;
    private String profileImageUrl;

    // Constructor vacío requerido por Firestore
    public User() {
    }

    // Constructor con todos los campos
    public User(String uid, String name, String description, String relationshipType, String age, String profileImageUrl) {
        this.uid = uid;
        this.name = name;
        this.description = description;
        this.relationshipType = relationshipType;
        this.age = age;
        this.profileImageUrl = profileImageUrl;
    }

    // Constructor simplificado
    public User(String uid, String name, String profileImageUrl) {
        this.uid = uid;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters y Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
