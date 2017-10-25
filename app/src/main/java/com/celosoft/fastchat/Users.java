package com.celosoft.fastchat;

/**
 * Created by Marcelo on 20/06/2017.
 */

public class Users {

    public String name;
    public String status;
    public String image;
    public String thumb_image;
    private String req_type;

    public Users(){

    }

    public Users(String name, String status, String image) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.thumb_image = thumb_image;
    }

    public String getReq_type() {
        return req_type;
    }

    public void setReq_type(String req_type) {
        this.req_type = req_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}
