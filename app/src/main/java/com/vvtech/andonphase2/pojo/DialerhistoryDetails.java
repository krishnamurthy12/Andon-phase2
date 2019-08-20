package com.vvtech.andonphase2.pojo;

public class DialerhistoryDetails {
    String name,phone,calldate;
    int id;

    public DialerhistoryDetails(String name, String phone, String calldate, int id) {
        this.name = name;
        this.phone = phone;
        this.calldate = calldate;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCalldate() {
        return calldate;
    }

    public void setCalldate(String calldate) {
        this.calldate = calldate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
