package com.brainy.erevu.data;

/**
 * Created by John on 31-Oct-16.
 */
public class Question {

    private Long posted_date;
    private String posted_reason;
    private String question_body;
    private String question_title;
    private String sender_image;
    private String question_photo;
    private String sender_name;
    private String sender_uid;
    private String post_id;
    private String ads_image;

    public Question() {

    }

    public Question(Long posted_date, String  question_photo, String ads_image, String posted_reason, String question_body, String question_title, String sender_image, String sender_name, String sender_uid, String post_id) {
        this.posted_date = posted_date;
        this.posted_reason = posted_reason;
        this.question_body = question_body;
        this.question_title = question_title;
        this.sender_image = sender_image;
        this.question_photo = question_photo;
        this.sender_name = sender_name;
        this.sender_uid = sender_uid;
        this.post_id = post_id;
    }

    public Question(String s) {
    }


    public Long getPosted_date() {
        return posted_date;
    }

    public void setPosted_date(Long posted_date) {
        this.posted_date = posted_date;
    }

    public String getQuestion_body() {
        return question_body;
    }

    public void setQuestion_body(String question_body) {
        this.question_body = question_body;
    }

    public String getQuestion_title() {
        return question_title;
    }

    public void setQuestion_title(String question_title) {
        this.question_title = question_title;
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

    public String getSender_uid() {
        return sender_uid;
    }

    public void setSender_uid(String sender_uid) {
        this.sender_uid = sender_uid;
    }

    public String getPosted_reason() {
        return posted_reason;
    }

    public void setPosted_reason(String posted_reason) {
        this.posted_reason = posted_reason;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getAds_image() {
        return ads_image;
    }

    public void setAds_image(String ads_image) {
        this.ads_image = ads_image;
    }

    public String getQuestion_photo() {
        return question_photo;
    }

    public void setQuestion_photo(String question_photo) {
        this.question_photo = question_photo;
    }
}
