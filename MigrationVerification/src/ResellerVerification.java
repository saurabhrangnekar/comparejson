import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResellerVerification 
{
	Log log = LogFactory.getLog(ResellerVerification.class);
	private String sql_uri;
	private String mongo_uri;

	public ResellerVerification(String sql_uri, String mongo_uri) 
	{
		this.sql_uri = sql_uri;
		this.mongo_uri = mongo_uri;
	}

	public String callAPI(String uri, Map<String, String> requestProperties) throws IOException 
	{
		long start = System.currentTimeMillis();
		URL obj = new URL(uri);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("GET");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json");

		for(Map.Entry<String, String> entry : requestProperties.entrySet())
		{
			con.setRequestProperty(entry.getKey(), entry.getValue());			
		}
		
		con.connect();
		System.out.println("uri="+uri);
						
		String result = new BufferedReader(new InputStreamReader(con.getInputStream())).lines().collect(Collectors.joining("\n"));
		con.getInputStream().close();
		long end= System.currentTimeMillis();
		System.out.println("New Response="+result);
		System.out.println("Time taken for service call="+(end-start)+" ms");
		
		return result;
	}

	public void compareForUser(List<Map<String,String>> data, GenericParser searchApiCompare, String serviceString) throws Exception 
	{
		Map<String, String> requestProperties= new HashMap<>();
		requestProperties.put("X-WSM-ROLE", "RESELLER");

		for(Map<String,String> user : data)		
		{
			String userId = user.get("user_id");
			System.out.println("=========================Start of processing for userid="+userId+"===============================");

			try
			{
				requestProperties.put("X-WSM-USERID", userId);				
				String sqlResponse =   callAPI((sql_uri + serviceString),  requestProperties);
				String mongoResponse = callAPI((mongo_uri + serviceString),requestProperties);
				
				boolean result=searchApiCompare.compare(sqlResponse, mongoResponse);	
				
				if(result)
					System.out.println("********* Final Result: Yesss !!! Data matches for userid="+userId+" *********");
				else
					System.out.println("********* Final Result: Noooo :-( Data doesn't match for userid="+userId+" *********");
			}
			catch(java.io.IOException e)
			{
				System.out.println("********* Final Result: Noooo :-( Error in hiting service for userid="+userId+" *********");
			}
			System.out.println("=========================End of processing for userid="+userId+"===============================");
			System.out.println("");
		}
	}			
	
	public void compareForUser(List<Map<String,String>> data, String serviceString) throws Exception 
	{
		compareForUser(data,  new GenericParser(), serviceString);
	}
	
	
	public void compareForAdmins(List<Map<String,String>> data, GenericParser searchApiCompare, String serviceString) throws Exception 
	{
		Map<String, String> requestProperties= new HashMap<>();
		requestProperties.put("X-WSM-ROLE", "SAMSUNG_ADMIN");

		for(Map<String,String> user : data)		
		{
			String userId = user.get("user_id");
			System.out.println("=========================Start of processing for userid="+userId+"===============================");

			try
			{
				requestProperties.put("X-WSM-USERID", userId);
				String sqlResponse =   callAPI((sql_uri + serviceString),  requestProperties);				
//				String sqlResponse =   callAPI((mongo_uri + serviceString),  requestProperties);
				String mongoResponse = callAPI((mongo_uri + serviceString),requestProperties);
				
				boolean result=searchApiCompare.compare(sqlResponse, mongoResponse);	
				
				if(result)
					System.out.println("********* Final Result: Yesss !!! Data matches for userid="+userId+" *********");
				else
					System.out.println("********* Final Result: Noooo :-( Data doesn't match for userid="+userId+" *********");
			}
			catch(java.io.IOException e)
			{
				System.out.println("********* Final Result: Noooo :-( Error in hiting service for userid="+userId+" *********");
			}
			System.out.println("=========================End of processing for userid="+userId+"===============================");
			System.out.println("");
		}
	}		
	
	public void compareWithQueryParam(List<Map<String,String>> data, GenericParser searchApiCompare, String serviceString, String paramName)  throws Exception
	{		
		Map<String, String> requestProperties= new HashMap<>();
		requestProperties.put("X-WSM-ROLE", "RESELLER");

		for(Map<String,String> user : data)		
		{
			String userId = user.get("user_id");
			String paramVal = user.get(paramName);
			System.out.println("=========================Start of processing for userid="+userId+" and "+ paramName +"="+ paramVal +"===============================");

			try
			{
				requestProperties.put("X-WSM-USERID", user.get("user_id"));				
				String sqlResponse =   callAPI((sql_uri + serviceString+paramVal),  requestProperties);
				String mongoResponse = callAPI((mongo_uri + serviceString+paramVal),requestProperties);
				
				boolean result=searchApiCompare.compare(sqlResponse, mongoResponse);	
				
				if(result)
					System.out.println("********* Final Result: Yesss !!! Data matches for userid="+userId+" and "+ paramName +"="+ paramVal +" *********");
				else
					System.out.println("********* Final Result: Noooo :-( Data doesn't match for userid="+userId+" and "+ paramName +"="+ paramVal +" *********");
			}
			catch(java.io.IOException e)
			{
				System.out.println("********* Final Result: Noooo :-( Error in hiting service for userid="+userId+" and "+ paramName +"="+ paramVal +" *********");
			}
			System.out.println("=========================End of processing for userid="+userId+" and "+ paramName +"="+ paramVal +"===============================");
			System.out.println("");
		}
	}
	
	public void compareWithQueryParam(List<Map<String,String>> data, String serviceString, String paramName)  throws Exception
	{
		compareWithQueryParam(data,  new GenericParser(), serviceString,paramName);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void compareCustomerSearch(List<Map<String,String>> data) throws Exception 
	{
		String serviceString="/v1/customers/search?limit=100";

		GenericParser searchApiCompare = new GenericParser();
		searchApiCompare.addFieldForSorting("items", "userId");
		searchApiCompare.addFieldToIgnore("items.id");
		searchApiCompare.addFieldToIgnore("items.lastuploadtime"); //MySQL code is picking up last update time from wrong table. The Mongo logic is correct. We will ignore this difference			
		searchApiCompare.addFieldToIgnore("items.devicecount");	   //MTPS rejected devices. Ignore this difference.
		
		compareForUser(data,searchApiCompare,serviceString );
	}

	// GET "/customers?customer.id"
	public void compareCustomerID(List<Map<String,String>> data)  throws Exception
	{
		String serviceString="/v1/customers?customer.id=";

		GenericParser searchApiCompare = new GenericParser();
//		searchApiCompare.addFieldForSorting("items", "customername");
		searchApiCompare.addFieldToIgnore("customer.id");
		searchApiCompare.addFieldToIgnore("customer.city");
		searchApiCompare.addFieldToIgnore("customer.address");
		searchApiCompare.addFieldToIgnore("customer.zipcode");
		searchApiCompare.addFieldToIgnore("customer.country");
		searchApiCompare.addFieldToIgnore("customer.email");
		searchApiCompare.addFieldToIgnore("customer.phoneNumber");		
		searchApiCompare.addFieldToIgnore("customer.lastuploadtime"); //MySQL logic is incorrect. 
		
		compareWithQueryParam(data, searchApiCompare, serviceString,"cust_id");	
		
//		Map<String, String> requestProperties= new HashMap<>();
//		requestProperties.put("X-WSM-ROLE", "RESELLER");
//
//		for(Map<String,String> user : data)		
//		{
//			String userId = user.get("user_id");
//			String customer = user.get("cust_id");
//			System.out.println("=========================Start of processing for userid="+userId+" and customer="+ customer +"===============================");
//
//			try
//			{
//				requestProperties.put("X-WSM-USERID", user.get("user_id"));				
//				String sqlResponse =   callAPI((sql_uri + serviceString+customer),  requestProperties);
//				String mongoResponse = callAPI((mongo_uri + serviceString+customer),requestProperties);
//				
//				boolean result=searchApiCompare.compare(sqlResponse, mongoResponse);	
//				
//				if(result)
//					System.out.println("********* Final Result: Yesss !!! Data matches for userid="+userId+" and customer="+ customer +" *********");
//				else
//					System.out.println("********* Final Result: Noooo :-( Data doesn't match for userid="+userId+" and customer="+ customer +" *********");
//			}
//			catch(java.io.IOException e)
//			{
//				System.out.println("********* Final Result: Noooo :-( Error in hiting service for userid="+userId+" and customer="+ customer +" *********");
//			}
//			System.out.println("=========================End of processing for userid="+userId+" and customer="+ customer +"===============================");
//			System.out.println("");
//		}
	}
		
	public void compareUploads(List<Map<String,String>> data)  throws Exception
	{
		String serviceString="/v1/uploads?limit=10000&customer.id=";
//		String serviceString="/v1/uploads?customer.id=";

		GenericParser searchApiCompare = new GenericParser();
		searchApiCompare.addFieldForSorting("items", "id");
//		searchApiCompare.addFieldToIgnore("items.uploadtime"); //Commented as of now to test other fields. Should be uncommented
		searchApiCompare.addFieldToIgnore("items.orderid"); //This filed is no more Upload specific and hence has been moved
		searchApiCompare.addFieldToIgnore("items.ordertime");  //This filed is no more Upload specific and hence has been moved
		
		compareWithQueryParam(data, searchApiCompare, serviceString,"cust_id");
	}
	
	
	public void compareCustomerCountry(List<Map<String,String>> data) throws Exception 
	{
		String serviceString="/v1/customers/country";		
		compareForUser(data,serviceString );
	}	

	
	public void compareDevices(List<Map<String,String>> data)  throws Exception
	{
		String serviceString="/v1/devices?limit=100000&customer.id=";

		GenericParser searchApiCompare = new GenericParser();
		searchApiCompare.addFieldForSorting("items", "imei","serialnumber");
		searchApiCompare.addFieldToIgnore("items.id");
		searchApiCompare.addFieldToIgnore("items.tags");
//		searchApiCompare.addFieldToIgnore("next"); //Temp ignored. There is issue with this field. JIRA raised
//		searchApiCompare.addFieldToIgnore("totalpages"); //Temp ignored. There is issue with this field. JIRA raised
		
		Map<String, String> requestProperties= new HashMap<>();
		requestProperties.put("X-WSM-ROLE", "RESELLER");

		for(Map<String,String> user : data)		
		{
			String userId = user.get("user_id");
			String customer = user.get("cust_id");
			System.out.println("=========================Start of processing for userid="+userId+" and customer="+ customer +"===============================");

			try
			{
				requestProperties.put("X-WSM-USERID", user.get("user_id"));				
				String sqlResponse =   callAPI((sql_uri + serviceString+customer),  requestProperties);
				String mongoResponse = callAPI((mongo_uri + serviceString+customer),requestProperties);
				
				boolean result=searchApiCompare.compare(sqlResponse, mongoResponse);	
				
				if(result)
					System.out.println("********* Final Result: Yesss !!! Data matches for userid="+userId+" and customer="+ customer +" *********");
				else
					System.out.println("********* Final Result: Noooo :-( Data doesn't match for userid="+userId+" and customer="+ customer +" *********");
			}
			catch(java.io.IOException e)
			{
				System.out.println("********* Final Result: Noooo :-( Error in hiting service for userid="+userId+" and customer="+ customer +" *********");
			}
			System.out.println("=========================End of processing for userid="+userId+" and customer="+ customer +"===============================");
			System.out.println("");
		}
	}

	
	public void compareUploadedby(List<Map<String,String>> data)  throws Exception
	{
		String serviceString="/v1/uploads/uploadedby?customer.id=";
		compareWithQueryParam(data, serviceString,"cust_id");		
	}	

	
	public void compareApiKey(List<Map<String,String>> data) throws Exception 
	{
		String serviceString="/v1/resellers/apikey";
		compareForUser(data,serviceString );
	}		
	
	
	public void compareDeviceModels(List<Map<String,String>> data)  throws Exception
	{
		String serviceString="/v1/devices/model?customer.id=";

		GenericParser searchApiCompare = new GenericParser();
//		searchApiCompare.addFieldForSorting("items", "id");
//		searchApiCompare.addFieldToIgnore("href");	
		
		Map<String, String> requestProperties= new HashMap<>();
		requestProperties.put("X-WSM-ROLE", "RESELLER");

		for(Map<String,String> user : data)		
		{
			String userId = user.get("user_id");
			String customer = user.get("cust_id");
			System.out.println("=========================Start of processing for userid="+userId+" and customer="+ customer +"===============================");

			try
			{
				requestProperties.put("X-WSM-USERID", user.get("user_id"));				
				String sqlResponse =   callAPI((sql_uri + serviceString+customer),  requestProperties);
				String mongoResponse = callAPI((mongo_uri + serviceString+customer),requestProperties);
				
				boolean result=searchApiCompare.compare(sqlResponse, mongoResponse);	
				
				if(result)
					System.out.println("********* Final Result: Yesss !!! Data matches for userid="+userId+" and customer="+ customer +" *********");
				else
					System.out.println("********* Final Result: Noooo :-( Data doesn't match for userid="+userId+" and customer="+ customer +" *********");
			}
			catch(java.io.IOException e)
			{
				System.out.println("********* Final Result: Noooo :-( Error in hiting service for userid="+userId+" and customer="+ customer +" *********");
			}
			System.out.println("=========================End of processing for userid="+userId+" and customer="+ customer +"===============================");
			System.out.println("");
		}
	}	

	public void compareActivityCustomerId(List<Map<String,String>> data) throws Exception 
	{
		String serviceString="/v1/activities/customerid";
		compareForUser(data, serviceString);		
	}
	
	public void compareActivityStatus(List<Map<String,String>> data) throws Exception
	{
		String serviceString="/v1/activities/status";
		compareForUser(data, serviceString);
	}

	public void compareActivityCustomers(List<Map<String,String>> data) throws Exception
	{
		String serviceString="/v1/activities/customers";
		compareForUser(data, serviceString);
	}
	
	public void compareActivityEvents(List<Map<String,String>> data) throws Exception
	{
		String serviceString="/v1/activities/events";
		compareForUser(data, serviceString);
	}
	
	public void compareResellersInfo(List<Map<String,String>> data) throws Exception
	{
		String serviceString="/v1/resellers/information";
		GenericParser searchApiCompare = new GenericParser();		
		searchApiCompare.addFieldToIgnore("reseller.company");	//There might be different names in KC and KME. Known issue
		searchApiCompare.addFieldToIgnore("reseller.isPrimary");	//There are 11 differences. Those are due to MySQL data mismatch. We are going to ignore
		compareForUser(data,searchApiCompare, serviceString);
	}

	public void compareUploadError(List<Map<String,String>> data) throws Exception 
	{
		String serviceString="/v1/uploads/error?upload.id=";

		GenericParser searchApiCompare = new GenericParser();
		searchApiCompare.addFieldForSorting("uploaderrors", "error");	
		compareWithQueryParam(data, searchApiCompare, serviceString, "upload_id");
	}
		
	public void compareCustomerCustomerId(List<Map<String,String>> data) throws Exception 
	{
		String serviceString="/v1/customers/customerid?search.searchterm=";
		compareWithQueryParam(data, serviceString, "mei");
	}
			
	public void compareDeviceError(List<Map<String,String>> data) throws Exception 
	{
		String serviceString="/v1/devices/error?delete.id=";
		compareWithQueryParam(data, serviceString, "delete_request_id");
	}

	public void compareSearch(List<Map<String,String>> data) throws Exception 
	{
		String serviceString="/v1/search?search.searchterm=";
		GenericParser searchApiCompare = new GenericParser();
		searchApiCompare.addFieldToIgnore("items.id");
		searchApiCompare.addFieldToIgnore("items.tags");
		compareWithQueryParam(data, searchApiCompare, serviceString, "mei");
	}
	
	public void compareActivityPerformedby(List<Map<String,String>> data) throws Exception
	{
		String serviceString="/v1/activities/performedby";
		compareForUser(data, serviceString);
	}

	public void compareActivities(List<Map<String,String>> data) throws Exception
	{
		String serviceString="/v1/activities?limit=10000";
		GenericParser searchApiCompare = new GenericParser();
		searchApiCompare.addFieldForSorting("items", "id");
		searchApiCompare.addFieldToIgnore("items.customername");
		compareForUser(data, searchApiCompare, serviceString);
	}
	
	public void compareDevicesTag(List<Map<String,String>> data) throws Exception 
	{
		String serviceString="/v1/devices/tag?customer.id=";
		compareWithQueryParam(data, serviceString, "cust_id");
	}

	public void compareDevicesAlltag(List<Map<String,String>> data) throws Exception
	{
		String serviceString="/v1/devices/alltag";
		compareForUser(data, serviceString);
	}

	public void compareTopcustomer(List<Map<String,String>> data) throws Exception
	{
		String serviceString="/v1/resellers/topcustomers";
		GenericParser searchApiCompare = new GenericParser();
		searchApiCompare.addFieldToIgnore("items.id");
		searchApiCompare.addFieldForSorting("items","");
		compareForUser(data, searchApiCompare, serviceString);
	}
	
	public void compareTopdevices(List<Map<String,String>> data) throws Exception
	{
		String serviceString="/v1/resellers/topdevices";
		GenericParser searchApiCompare = new GenericParser();
		searchApiCompare.addFieldForSorting("items","");
		compareForUser(data, searchApiCompare, serviceString);
	}
	
	public void compareResellersSA(List<Map<String,String>> data) throws Exception
	{
		String serviceString="/v1/samsungadmin/resellers";
		GenericParser searchApiCompare = new GenericParser();
		searchApiCompare.addFieldForSorting("adminResellerResponses", "resellerTenantCustomerId");
		compareForAdmins(data, searchApiCompare, serviceString);
	}
	
	
	
//	public static void main(String args[]) throws Exception
//	{
//		ResellerVerification rv = new ResellerVerification("http://localhost:8080/rp-mysql", "http://localhost:8080/rp-mongo");
//		rv.compareCustomerSearch(Arrays.asList("0aynvrmcyh", "wo49llikhc", "7tk0kj4bzc", "y30gwfwmjb", "6cccsdb8bc", "q7vrj492sg", "teir1dwv9c", "mhmymzaaah", "7uwkxgnhcc"));
//	}


}

