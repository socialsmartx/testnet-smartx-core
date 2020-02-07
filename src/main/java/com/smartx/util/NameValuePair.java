package com.smartx.util;
/**
 @author Huayong Shen */
public class NameValuePair {
    private String name;
    private String value;
    public String getName() {
        return name;
    }
    public NameValuePair() {
    }
    public NameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
