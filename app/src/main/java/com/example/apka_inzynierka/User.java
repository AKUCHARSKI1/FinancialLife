package com.example.apka_inzynierka;

public class User {
    private String username;
    private String hashedPin;
    private String photo;

    public User(String username, String hashedPin, String photo) {
        this.username = username;
        this.hashedPin = hashedPin;
        this.photo = photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedPin() {
        return hashedPin;
    }

    public void setHashedPin(String hashedPin) {
        this.hashedPin = hashedPin;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}