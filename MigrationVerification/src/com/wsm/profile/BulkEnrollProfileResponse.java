package com.wsm.profile;

import java.util.Date;
import java.util.List;

/**
 * Created by p1.yang on 11/1/17.
 */
public class BulkEnrollProfileResponse {

	/**
	 * serialVersionUID
	 */
	public static final long serialVersionUID = 8108135293970432321L;

	public String id;

	public String name;

	public String nameLower;

	public String description;

	public String mdmUri;

	public BulkEnrollMdmApkInfo mdmApkInfo;


	public List<BulkEnrollMdmApkInfo> secondaryMdmApkInfoList;

	public AuthenticationScheme authType;


	public String mdmProfileCustomData;

	public List<BulkEnrollEulaResponse> eulas;

	public String domainName;

	public String domainGroup;

	public boolean supportCapability;

	public boolean supportEnrollment;

	public boolean supportPolicyManagement;

	public String knoxLicense;

	public boolean supportDefaultEula;

	public AuthenticationScheme segAuthType;

	public CertificateUploadResponse policySigningCertificate;

	public Long updateTime;

	public Long deviceCount;

	public Long profileAssignedDeviceCount;

	public Long profileAssignmentFailedCount;

	public Long enrolledDeviceCount;

	public Long enrollFailedDeviceCount;

	public Long cancelledByUserDeviceCount;

	public Long notRejectedCount;

	public boolean doEnabled;
	public boolean systemAppEnabled;
	public String supportedMDM;

	public boolean skipSetupWizard;

}
