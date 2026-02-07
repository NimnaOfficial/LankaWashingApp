package model;

public class Supplier {

    private int id;
    private String companyName;
    private String contactName;
    private String phone;
    private String email;

    public Supplier(int id, String companyName, String contactName, String phone, String email) {
        this.id = id;
        this.companyName = companyName;
        this.contactName = contactName;
        this.phone = phone;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getContactName() {
        return contactName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}
