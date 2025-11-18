package com.learning.springboot.model;

public class ChangePasswordRequest {
    private String username;
    private String newPassword;
    private String oldPassword;

    public ChangePasswordRequest(String username, String newPassword, String oldPassword) {
        this.username = username;
        this.newPassword = newPassword;
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String password) {
        this.newPassword = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
}
