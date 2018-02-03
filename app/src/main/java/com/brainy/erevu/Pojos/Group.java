package com.brainy.erevu.Pojos;

/**
 * Created by Shephard on 1/31/2018.
 */

public class Group {

    public Group() {
    }

    private String created_date;
    private String post_id;
    private String message;
    private String group_image;
    private String group_id;
    private String group_name;

    public Group(String created_date, String post_id, String message, String group_image, String group_id, String group_name) {
        this.created_date = created_date;
        this.post_id = post_id;
        this.message = message;
        this.group_image = group_image;
        this.group_id = group_id;
        this.group_name = group_name;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getGroup_image() {
        return group_image;
    }

    public void setGroup_image(String group_image) {
        this.group_image = group_image;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }
}
