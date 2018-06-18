package com.gostreamyourself.android;

import java.security.PrivateKey;

public class User {
    private String fullName;
    private PrivateKey privateKey;

    public User(String fullName, PrivateKey privateKey) {
        this.fullName = fullName;
        this.privateKey = privateKey;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }
}
