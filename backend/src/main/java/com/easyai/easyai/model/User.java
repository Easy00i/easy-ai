package com.easyai.easyai.model;

public class User {
    private String username;
    private String email;
    private String password;
    private String role = "user";
    private boolean premium = false;
    private int coins = 50;

    public User() {}

    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isPremium() { return premium; }
    public void setPremium(boolean premium) { this.premium = premium; }
    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }
}
