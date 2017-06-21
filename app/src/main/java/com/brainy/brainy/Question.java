package com.brainy.brainy;

/**
 * Created by John on 31-Oct-16.
 */
public class Question {

    private String posted_date;
    private String question_body;
    private String question_title;
    private String sender_image;
    private String sender_name;
    private String sender_uid;

    public Question() {

    }

    public Question( String posted_date, String question_body, String question_title, String sender_image, String sender_name, String sender_uid) {
        this.posted_date = posted_date;
        this.question_body = question_body;
        this.question_title = question_title;
        this.sender_image = sender_image;
        this.sender_name = sender_name;
        this.sender_uid = sender_uid;
    }


    public String getPosted_date() {
        return posted_date;
    }

    public void setPosted_date(String posted_date) {
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
}
