package com.brainy.brainy.data;

/**
 * Created by Shephard on 7/15/2017.
 */

public class Answer {

    private String posted_date;
    private String post_answer;
    private String sender_image;
    private String sender_name;
    private String sender_uid;

    public Answer() {

    }

    public Answer( String posted_date, String post_answer, String sender_image, String sender_name, String sender_uid) {
        this.posted_date = posted_date;
        this.post_answer = post_answer;
        this.sender_image = sender_image;
        this.sender_name = sender_name;
        this.sender_uid = sender_uid;
    }

    public String getSender_uid() {
        return sender_uid;
    }

    public void setSender_uid(String sender_uid) {
        this.sender_uid = sender_uid;
    }

    public String getSender_name() {
        return sender_name;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }

    public String getSender_image() {
        return sender_image;
    }

    public void setSender_image(String sender_image) {
        this.sender_image = sender_image;
    }



    public String getPosted_date() {
        return posted_date;
    }

    public void setPosted_date(String posted_date) {
        this.posted_date = posted_date;
    }

    public String getPost_answer() {
        return post_answer;
    }

    public void setPost_answer(String post_answer) {
        this.post_answer = post_answer;
    }
}
