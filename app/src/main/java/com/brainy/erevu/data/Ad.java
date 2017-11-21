package com.brainy.erevu.data;

/**
 * Created by Shephard on 7/15/2017.
 */

public class Ad {

    private String ad_image;
    private String ad_name;
    private String sender_uid;

    public Ad() {

    }

    public Ad( String ad_image,String ad_name, String sender_uid) {
        this.ad_image = ad_image;
        this.ad_name = ad_name;
        this.sender_uid = sender_uid;
    }

    public String getAd_image() {
        return ad_image;
    }

    public void setAd_image(String ad_image) {
        this.ad_image = ad_image;
    }

    public String getAd_name() {
        return ad_name;
    }

    public void setAd_name(String ad_name) {
        this.ad_name = ad_name;
    }

    public String getSender_uid() {
        return sender_uid;
    }

    public void setSender_uid(String sender_uid) {
        this.sender_uid = sender_uid;
    }

}
