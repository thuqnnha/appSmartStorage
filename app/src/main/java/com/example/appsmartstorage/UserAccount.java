package com.example.appsmartstorage;

public class UserAccount {
    private String username;
    private String password;
    private String phone;
    private String email;
    private int idRole;


    public UserAccount(String username, String password, String phone, String email, int idRole) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.idRole = idRole;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public int getIdRole() {
        return idRole;
    }
}



