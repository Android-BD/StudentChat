package com.seef.chat.student.studentchat.models;

/**
 * Created by jcsalguero on 23/11/2016.
 */

public class User {
    private String id;
    private String username;
    private String photo;
    private String like;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    @SuppressWarnings("unused")
    public User() {

    }
}
