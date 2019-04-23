package com.wsm.profile;

import java.util.List;


public class BulkEnrollMdmApkInfo {


    public String mdmApkUri;
    public String packageName;
    public String version;
    public List<String> signatures;

    public BulkEnrollMdmApkInfo(String mdmApkUri) {
        this.mdmApkUri = mdmApkUri;
    }

}
