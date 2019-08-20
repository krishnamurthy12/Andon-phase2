package com.vvtech.andonphase2.pojo;

import android.graphics.Bitmap;

public class EmployeeDetails {

    String empName;
    int image;

    public EmployeeDetails(String empName, int image) {
        this.empName = empName;
        this.image = image;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
