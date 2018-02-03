package com.brainy.erevu.Pojos;

/**
 * Created by Shephard on 7/15/2017.
 */

public class Users {

    private Long posted_date;
    private String user_image;
    private String name;
    private String username;
    private String post_id;
    private String state;
    private String city;
    private String uid;
    public Users() {

    }

    public Users(Long posted_date, String user_image, String name,String username, String uid, String post_id, String city, String state) {
        this.posted_date = posted_date;
        this.user_image = user_image;
        this.name = name;
        this.username = username;
        this.uid = uid;
        this.post_id = post_id;
        this.city = city;
        this.state = state;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }



    public Long getPosted_date() {
        return posted_date;
    }

    public void setPosted_date(Long posted_date) {
        this.posted_date = posted_date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
