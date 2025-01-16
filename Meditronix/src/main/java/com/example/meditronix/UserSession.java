package com.example.meditronix;
public class UserSession {
    private static UserSession instance;
    private String username; // Store the logged-in username
    private String role;     // Optional: Store user role if needed

    // Private constructor to prevent instantiation
    private UserSession() {}

    // Static method to get the singleton instance
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // Getter and Setter for Username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Optional: Getter and Setter for Role
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Clear session (useful for logout functionality)
    public void clearSession() {
        username = null;
        role = null;
    }
}
