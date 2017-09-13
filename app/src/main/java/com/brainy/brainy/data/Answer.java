package com.brainy.brainy.data;

/**
 * Created by Shephard on 7/15/2017.
 */

public class Answer {

    private String posted_date;
    private String posted_answer;
    private String posted_quiz_title;
    private String posted_reason;
    private String sender_image;
    private String sender_name;
    private String sender_uid;

    public Answer() {

    }

    public Answer(String posted_date, String post_answered, String posted_quiz_title, String posted_reason, String sender_image, String sender_name, String sender_uid) {
        this.posted_date = posted_date;
        this.posted_answer = post_answered;
        this.posted_quiz_title = posted_quiz_title;
        this.posted_reason = posted_reason;
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


    public String getPosted_answer() {
        return posted_answer;
    }

    public void setPosted_answer(String posted_answer) {
        this.posted_answer = posted_answer;
    }

    public String getPosted_reason() {
        return posted_reason;
    }

    public void setPosted_reason(String posted_reason) {
        this.posted_reason = posted_reason;
    }

    public String getPosted_quiz_title() {
        return posted_quiz_title;
    }

    public void setPosted_quiz_title(String posted_quiz_title) {
        this.posted_quiz_title = posted_quiz_title;
    }
}
