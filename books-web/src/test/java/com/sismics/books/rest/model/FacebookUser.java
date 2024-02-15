package com.sismics.books.rest.model;

/**
 * Facebook test user.
 * 
 * Management :
 * https://developers.facebook.com/apps/387037158089019/roles?role=test%20users
 * 
 * @author jtremeaux
 */
public class FacebookUser {
    private String id;

    private String email;

    private String fullName;

    private String accessToken;

    public FacebookUser(String id, String email, String fullName, String accessToken) {
        setId(id);
        setEmail(email);
        setFullName(fullName);
        setAccessToken(accessToken);
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    private void setId(String id) {
        this.id = id;
    }

    private void setEmail(String email) {
        this.email = email;
    }

    private void setFullName(String fullName) {
        this.fullName = fullName;
    }

    private void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
