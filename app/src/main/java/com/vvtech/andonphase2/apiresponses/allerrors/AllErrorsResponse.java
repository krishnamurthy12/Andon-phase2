
package com.vvtech.andonphase2.apiresponses.allerrors;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AllErrorsResponse {

    @SerializedName("errorList")
    @Expose
    private List<ErrorList> errorList = null;

    public List<ErrorList> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<ErrorList> errorList) {
        this.errorList = errorList;
    }

}
