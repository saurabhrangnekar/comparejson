package com.wsm.profile;


import java.io.Serializable;
import java.util.List;

public class GetBulkEnrollProfileListResponse implements Serializable {

    public List<BulkEnrollProfileResponse> profileList;

    public Long totalCount;
}
