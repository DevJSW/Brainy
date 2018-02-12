package com.brainy.erevu.Pojos;

/**
 * Created by Shephard on 7/15/2017.
 */

public class Chat {


    private String message;
    private String sender_uid;
    private String receiver_uid;
    private String message_type;
    private String sender_image;
    private String sender_name;
    private String photo;
    private String post_id;
    private Long posted_date;
    private String sender_username;
    private String group_id;


    public Chat() {

    }


    public Chat(String message, String sender_uid, String receiver_uid, String message_type, String sender_image, String sender_name, String photo, String post_id, Long posted_date, String sender_username, String group_id) {
        this.message = message;
        this.sender_uid = sender_uid;
        this.receiver_uid = receiver_uid;
        this.message_type = message_type;
        this.sender_image = sender_image;
        this.sender_name = sender_name;
        this.photo = photo;
        this.post_id = post_id;
        this.posted_date = posted_date;
        this.sender_username = sender_username;
        this.group_id = group_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender_uid() {
        return sender_uid;
    }

    public void setSender_uid(String sender_uid) {
        this.sender_uid = sender_uid;
    }

    public String getReceiver_uid() {
        return receiver_uid;
    }

    public void setReceiver_uid(String receiver_uid) {
        this.receiver_uid = receiver_uid;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public String getSender_image() {
        return sender_image;
    }

    public void setSender_image(String sender_image) {
        this.sender_image = sender_image;
    }

    public String getSender_name() {
        return sender_name;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public Long getPosted_date() {
        return posted_date;
    }

    public void setPosted_date(Long posted_date) {
        this.posted_date = posted_date;
    }

    public String getSender_username() {
        return sender_username;
    }

    public void setSender_username(String sender_username) {
        this.sender_username = sender_username;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }


}
