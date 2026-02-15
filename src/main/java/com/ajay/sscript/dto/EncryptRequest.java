package com.ajay.sscript.dto;

public class EncryptRequest {

    private String text;
    private int key;

    public EncryptRequest() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
