package model;

public class Payroll {
    private int payrollId;
    private int employeeId;
    private int structureId;
    private String month;
    private double basicSalary;
    private int otHours;
    private double otEarnings;
    private double allowances;
    private double deductions;
    private double netSalary;
    private String status;

    public Payroll(int payrollId, int employeeId, int structureId, String month, double basicSalary,
                   int otHours, double otEarnings, double allowances, double deductions,
                   double netSalary, String status) {
        this.payrollId = payrollId;
        this.employeeId = employeeId;
        this.structureId = structureId;
        this.month = month;
        this.basicSalary = basicSalary;
        this.otHours = otHours;
        this.otEarnings = otEarnings;
        this.allowances = allowances;
        this.deductions = deductions;
        this.netSalary = netSalary;
        this.status = status;
    }

    // Getters
    public int getPayrollId() { return payrollId; }
    public double getNetSalary() { return netSalary; }
    // ... add others if needed
}