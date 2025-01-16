package com.example.meditronix;

public class UserSession {
    private static UserSession instance;
    private String username; // Store the logged-in username
    private String role;     // Optional: Store user role if needed
    private int signedIn;    // 1 if signed in, 0 if not

    // Private constructor to prevent instantiation
    private UserSession() {
        this.signedIn = 0; // Default to 0 (not signed in)
    }

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

    // Getter and Setter for SignedIn
    public int getSignedIn() {
        return signedIn;
    }

    public void setSignedIn(int signedIn) {
        if (signedIn == 0 || signedIn == 1) {
            this.signedIn = signedIn;
        } else {
            throw new IllegalArgumentException("signedIn can only be 0 or 1.");
        }
    }

    // Clear session (useful for logout functionality)
    public void clearSession() {
        username = null;
        role = null;
        signedIn = 0; // Reset signed-in status
    }
}
