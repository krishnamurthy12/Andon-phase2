
package com.vvtech.andonphase2.apiresponses.allerrors;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ErrorList {

    @SerializedName("action")
    @Expose
    private String action;
    @SerializedName("createddate")
    @Expose
    private String createddate;
    @SerializedName("errorid")
    @Expose
    private Integer errorid;
    @SerializedName("errorname")
    @Expose
    private String errorname;
    @SerializedName("linename")
    @Expose
    private String linename;
    @SerializedName("people")
    @Expose
    private String people;
    @SerializedName("repairtime")
    @Expose
    private String repairtime;
    @SerializedName("station")
    @Expose
    private String station;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCreateddate() {
        return createddate;
    }

    public void setCreateddate(String createddate) {
        this.createddate = createddate;
    }

    public String getErrorid()
    {
        return errorid.toString();
    }

    public void setErrorid(Integer errorid) {
        this.errorid = errorid;
    }

    public String getErrorname() {
        return errorname;
    }

    public void setErrorname(String errorname) {
        this.errorname = errorname;
    }

    public String getLinename() {
        return linename;
    }

    public void setLinename(String linename) {
        this.linename = linename;
    }

    public String getPeople() {
        return people;
    }

    public void setPeople(String people) {
        this.people = people;
    }

    public String getRepairtime() {
        return repairtime;
    }

    public void setRepairtime(String repairtime) {
        this.repairtime = repairtime;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

}
