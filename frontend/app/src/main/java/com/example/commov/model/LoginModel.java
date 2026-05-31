package com.example.commov.model;

public final class LoginModel {
    private final String email;
    private final String password;
    private final boolean passwordVisible;

    public LoginModel(String email, String password, boolean passwordVisible) {
        this.email = email;
        this.password = password;
        this.passwordVisible = passwordVisible;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isPasswordVisible() {
        return passwordVisible;
    }

    public LoginModel withEmail(String email) {
        return new LoginModel(email, password, passwordVisible);
    }

    public LoginModel withPassword(String password) {
        return new LoginModel(email, password, passwordVisible);
    }

    public LoginModel withPasswordVisible(boolean passwordVisible) {
        return new LoginModel(email, password, passwordVisible);
    }
}
