
package com.vvtech.andonphase2.apiresponses.allusers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AllAvailableUsersResponse {

    @SerializedName("employeeStatusList")
    @Expose
    private List<EmployeeStatusList> employeeStatusList = null;

    public List<EmployeeStatusList> getEmployeeStatusList() {
        return employeeStatusList;
    }

    public void setEmployeeStatusList(List<EmployeeStatusList> employeeStatusList) {
        this.employeeStatusList = employeeStatusList;
    }

}
