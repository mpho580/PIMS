package model;

public class User {
    private int userId;
    private String username;
    private String password; // hashed
    private String fullName;
    private String role;     // "ADMIN" or "CASHIER"
    private boolean active;
 
    public User() {}
 
    public User(int userId, String username, String fullName, String role, boolean active) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }
 
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
 
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
 
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
 
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
 
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
 
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
 
    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
 
    @Override
    public String toString() { return fullName + " (" + username + ")"; }
}
