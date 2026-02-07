package model;

public class User {
    private int userId;
    private int roleId;
    private String name;
    private String email;
    private String phone;
    private String password; // Kept for object completion, handle carefully!
    private String status;

    public User(int userId, int roleId, String name, String email, String phone, String password, String status) {
        this.userId = userId;
        this.roleId = roleId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.status = status;
    }

    // Standard Getters
    public int getUserId() { return userId; }
    public int getRoleId() { return roleId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getStatus() { return status; }
    // No getter for password for security
}