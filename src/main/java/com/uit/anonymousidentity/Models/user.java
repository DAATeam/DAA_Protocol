package com.uit.anonymousidentity.Models;

/**
 * Created by DK on 10/17/16.
 */
public class user {
    private int id;
    private String name;

    public user() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public user(int id, String name) {
        this.id = id;

        this.name = name;
    }
}
