package model;

public class Employee {
    private int empId;
    private String name;
    private String nic;
    private String phone;
    private String address;
    private String designation;
    private String department;
    private double basicSalary;
    private String status;

    public Employee(int empId, String name, String nic, String phone, String address,
                    String designation, String department, double basicSalary, String status) {
        this.empId = empId;
        this.name = name;
        this.nic = nic;
        this.phone = phone;
        this.address = address;
        this.designation = designation;
        this.department = department;
        this.basicSalary = basicSalary;
        this.status = status;
    }

    // Getters
    public int getEmpId() { return empId; }
    public String getName() { return name; }
    public String getNic() { return nic; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getDesignation() { return designation; }
    public String getDepartment() { return department; }
    public double getBasicSalary() { return basicSalary; }
    public String getStatus() { return status; }
}