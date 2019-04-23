
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MigrationVerificationRP {

	public static Log log = LogFactory.getLog(MigrationVerificationRP.class);
	
	Properties properties;
	ResellerVerification rv ;
	
	public MigrationVerificationRP() throws Exception
	{
		properties = new Properties();
		InputStream input = new FileInputStream("config/application.properties");
		properties.load(input);			
		rv = new ResellerVerification(properties.get("sqlUri").toString(),properties.get("mongoUri").toString());
	}
	
	public List<Map<String, String>> getDataFromDB(String query) throws Exception 
	{
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection(properties.get("dbUrl").toString(), properties.get("user").toString(), properties.get("password").toString());
		Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		
		ResultSet rs = stmt.executeQuery(query);

		ResultSetMetaData meta = rs.getMetaData();
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		while(rs.next()) 
		{
	         Map<String, String> map = new HashMap<>();
	         for (int i = 1; i <= meta.getColumnCount(); i++) 
	         {
	             String key = meta.getColumnName(i);
	             String value = rs.getString(key);
	             map.put(key, value);
	         }
	         list.add(map);
		}
		con.close();
		return list;
	}

	public List<Map<String, String>> getUsers()  throws Exception
	{
		String query="select ...";
		
		return getDataFromDB(query);
	}
	
	public List<Map<String, String>> getResellers()  throws Exception
	{
		String query = "select ...";
		return getDataFromDB(query);
	}
	

	public List<Map<String, String>> getUploads()  throws Exception
	{
		String query="select ... " ;		
		return getDataFromDB(query);
	}
		
	
	public void testCustomerSearch() throws Exception
	{
		rv.compareCustomerSearch(getUsers());		
	}

	public void testCustomerID() throws Exception
	{
		rv.compareCustomerID(getResellers());			
	}	

	public void testResellersSA() throws Exception
	{
		Map<String, String> m = new HashMap<>();
		m.put("user_id","aa4yq1xj0h");
		rv.compareResellersSA(Arrays.asList(m));			
	}
	
	public static void main(String[] args) throws Exception
	{				
		MigrationVerificationRP mrp = new MigrationVerificationRP();
//		mrp.testCustomerSearch(); 	//Done
//		mrp.testCustomerID();		//Failed
//		mrp.testResellersSA();		
	}
	
}

