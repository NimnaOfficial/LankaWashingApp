package model;

public class Role {
    private int roleId;
    private String roleName;
    private String permission;

    public Role(int roleId, String roleName, String permission) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.permission = permission;
    }

    public int getRoleId() { return roleId; }
    public String getRoleName() { return roleName; }
    public String getPermission() { return permission; }
}