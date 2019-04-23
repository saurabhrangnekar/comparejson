import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;


class MyMapComparator implements Comparator<Map<String, Object>>
{
	private String field;
	private String field2;
	
	public MyMapComparator(String field)
	{
		super();
		this.field = field;
	}
	
	public MyMapComparator(String field, String field2)
	{
		super();
		this.field = field;
		this.field2 = field2;
	}

	private String nvl(String val, String defaultVal)
	{
		return (val==null)?defaultVal:val;
	}
	
	public int compare(Map<String, Object> m1, Map<String, Object> m2) 
	{
		Object obj1 =  m1.get(field);
		Object obj2 =  m2.get(field);
		
		if(obj1 instanceof Integer)
		{
			Integer val1 = (Integer) obj1;
			Integer val2 = (Integer) obj2;
			return val1-val2;
		}
		
		String val1 = (String) obj1;
		String val2 = (String) obj2;
		val1 = nvl(val1,"");
		val2 = nvl(val2,"");
		
		if(val1.equalsIgnoreCase("") &&  val2.equalsIgnoreCase(""))
		{
			String val3 = (String) m1.get(field2);
			String val4 = (String) m2.get(field2);
			val3 = nvl(val3,"");
			val4 = nvl(val4,"");
			return val3.compareTo(val4) ;
		}
		
		return val1.compareTo(val2) ;
	}
}

public class GenericParser
{
	Log log = LogFactory.getLog(GenericParser.class);
	private Set<String> ignoreFields;
	private Map<String, Comparator> listCompareMap;

	public GenericParser()
	{
		ignoreFields = new HashSet<>();
		listCompareMap = new HashMap<>();
	}

	public void addFieldToIgnore(String field)
	{
		ignoreFields.add(field);
	}
	
	public void addFieldForSorting(String listKey, String sortKey)
	{
		listCompareMap.put(listKey, new MyMapComparator(sortKey));
	}
	
	public void addFieldForSorting(String listKey, String sortKey, String sortKey2)
	{
		listCompareMap.put(listKey, new MyMapComparator(sortKey,sortKey2));
	}
	
	private boolean compareNumbers(Object  obj1, Object obj2)
	{
		if(!(obj2 instanceof Number))
			return false;
		
		Number num1 = (Number)obj1;
		Number num2 = (Number)obj2;
		
		// This block assumes that all numerical values to be long. 
		// This is incorrect assumption. The values can be float/double too
		
//		if(obj1 instanceof Long && obj2 instanceof Long)
			return num1.longValue() == num2.longValue();
//		else if (obj1 instanceof Double && obj2 instanceof Double)
//			return num1.doubleValue() == num2.doubleValue();
//		else 
//			return false;
	}

	private boolean compareBoolean(Object  obj1, Object obj2)
	{
		if(!(obj2 instanceof Boolean))
			return false;
		
		boolean bool1 = (Boolean)obj1;
		boolean bool2 = (Boolean)obj2;
		return bool1==bool2;
	}
	
	private boolean compareStrings(Object  obj1, Object obj2)
	{
		if(!(obj2 instanceof String))
			return false;
		
		String str1 = (String)obj1;
		String str2 = (String)obj2;
		
		return str1.toString().compareTo(str2.toString())==0;			
	}

	private boolean compareValues(Object  obj1, Object obj2)
	{
		try
		{
			if(obj1 instanceof String)
			{
				return compareStrings((String) obj1, (String)obj2);
			}
			else if(obj1 instanceof Number)
			{
				return compareNumbers((Number) obj1, (Number)obj2);
			}
			else if(obj1 instanceof Boolean)
			{
				return compareBoolean((Boolean) obj1, (Boolean)obj2);
			}		
		}catch(ClassCastException e)
		{
			System.out.println("ERROR !!! Type mistmatch");
			return false;
		}
		System.out.println("ERROR !!! Uknown Type");
		return false;
	}

	public boolean compare(String mysqlStr, String mongoStr) throws Exception
	{
		ObjectMapper mapper = new ObjectMapper();
		
		Map<String, Object> mysqlMap = mapper.readValue(mysqlStr, new TypeReference<Map<String, Object>>(){});
		Map<String, Object> mongoMap = mapper.readValue(mongoStr, new TypeReference<Map<String, Object>>(){});
				
		System.out.println(mysqlMap);
		System.out.println(mongoMap);
		System.out.println("===============================");

		long start = System.currentTimeMillis();
		boolean result =compareMap(mysqlMap, mongoMap, null);
		long end= System.currentTimeMillis();
		System.out.println("Time taken for comparison="+(end-start)+" ms");
		
		return result;
	}
	
	private boolean compareMap(Map<String, Object> mysqlMap, Map<String, Object> mongoMap, String parentKey)
	{
		boolean result=true;
		
		mysqlMap.values().removeAll(Arrays.asList(""," "));
//		mysqlMap.values().removeIf(entries-> entries.get );		
		
		if(mysqlMap.size() != mongoMap.size())
		{
			System.out.println("Warning !!! Number of keys do not match");
//			result =false;
		}
		
		for(Map.Entry<String, Object> entry : mysqlMap.entrySet())
		{
			String originalkey = entry.getKey();
			String key = (parentKey==null)?originalkey:(parentKey+"."+originalkey);

			if(ignoreFields.contains(key))
			{
				System.out.println("Field " +key+ " has been ignored");
				continue;
			}
			
			Object mysqlValue = entry.getValue();
			Object mongoValue = mongoMap.get(originalkey);
						
			if(!mongoMap.containsKey(originalkey))
		 	{
		 		System.out.println("ERROR-000 !!! Key "+ key +" exists in MySQL but does not exist in Mongo");
		 		result =false;
		 		continue;
		 	}
			
			//This if is for Devices API call. For larger values printing the objects slows down the processing significantly
//			if(!key.equalsIgnoreCase("items"))
			{
				System.out.println("Key="+key+"\t\tMySQL="+mysqlValue+"\t\tMongo="+mongoValue);	
			}
	 		
			if(mysqlValue == null && mongoValue == null)
			{
				//Both fields are null. No issues.
			}
			else if(mysqlValue  == null || mongoValue == null)
			{
				//One of the fields are null whereas other is not null. Issue
				System.out.println("ERROR-111 !!! Value for key "+ key +" does not match");
				result = false;
			}		
			else if(mysqlValue instanceof String || mysqlValue instanceof Number || mysqlValue instanceof Boolean)
			{
		 		if(! compareValues(mysqlValue, mongoValue))
				{
					result = false;
					System.out.println("ERROR-222 !!! Value for key "+ key +" does not match");
				}						
			}
			else if(mysqlValue instanceof Map)
			{
				System.out.println("Key="+key+" is a Map. Checking Recursively");
				result = compareMap((Map<String, Object>)mysqlValue, (Map<String, Object>)mongoValue, key) && result;
			}			
			else if(mysqlValue instanceof List)
			{
				System.out.println("Key="+key+" is a List. Checking Iteratively");
				result = compareList((List<Object>)mysqlValue, (List<Object>)mongoValue, key) && result;
			}
			else
			{
				System.out.println("Key="+key+" is a Unknown. ********************** Need to compare it **********************");
			}
			
//			System.out.println("Processed Key="+key + "\t\tResult="+result);		
		}
		
		return result;
	}
	
	private void sortLists(List<Object> mysqlList, List<Object> mongoList, String key)
	{
		if(listCompareMap.containsKey(key) )
		{
			List<Map<String, Object>> lst1 = (List<Map<String, Object>>) (Object)mysqlList;		
			List<Map<String, Object>> lst2 = (List<Map<String, Object>>) (Object)mongoList;		
			Collections.sort(lst1, listCompareMap.get(key));			
			Collections.sort(lst2, listCompareMap.get(key));
		}
		else
		{
			// There is no key defined. The list is assumed to be of String.
			// This is incorrect assumption. It can be number too 
			List<String> lst1 = (List<String>) (Object)mysqlList;		
			List<String> lst2 = (List<String>) (Object)mongoList;	
			lst1.remove(null);
			lst2.remove(null);
			Collections.sort(lst1);			
			Collections.sort(lst2);
		}
	}
	
	private boolean compareList(List<Object> mysqlList, List<Object> mongoList, String parentKey)
	{
		boolean result=true;
		String key = parentKey;

		sortLists(mysqlList,mongoList, key);
		
		if(mysqlList.size()!=mongoList.size())
		{
			System.out.println("ERROR-555 !!! List size for key "+ key +" does not match");
			return false;
		}
		
		for(int i=0; i< mysqlList.size(); i++)
		{
			Object mysqlValue = mysqlList.get(i);
			Object mongoValue = mongoList.get(i);

			if(mysqlValue instanceof String || mysqlValue instanceof Number || mysqlValue instanceof Boolean)
			{
		 		if(! compareValues(mysqlValue, mongoValue))
				{
					System.out.println("ERROR-333 !!! Value for key "+ key +" does not match");
					result = false;
				}						
			}
			else if(mysqlValue instanceof Map)
			{
				System.out.println("Key="+key+" is a Map. Checking Recursively");
				result =  compareMap((Map<String, Object>)mysqlValue, (Map<String, Object>)mongoValue, key) && result;
			}	
			else
			{
				System.out.println("ERROR-444 !!! Uknown Type");
				result = false;
			}
		}
		return result;
	}
	
//	public static void main(String[] args) throws Exception
//	{
////		String str1 = "{\"id\":389,\"name\":\"Samsung\",\"email\":\"sarahkim@yopmail.com\",\"phoneNumber\":null,\"state\":\"ACTIVE\",\"description\":null,\"address\":\"645 Clyde Avenue\",\"address2\":null,\"city\":\"Mountain View\",\"province\":null,\"zipCode\":\"94043\",\"country\":\"United States of America\",\"users\":null}";
////		String str2 = "{\"id\":\"64e87c2ac35f11e7b071f374\",\"name\":\"Samsung\",\"email\":\"sarahkim@yopmail.com\",\"phoneNumber\":null,\"state\":null,\"description\":null,\"address\":\"645 Clyde Avenue\",\"address2\":null,\"city\":\"Mountain View\",\"province\":null,\"zipCode\":\"94043\",\"country\":\"United States of America\",\"users\":null}";		
//
////		String str1= "{\"_id\":{\"$oid\":\"5dd1966dc4de11e79932    \"},\"kme\":{\"loginId\":\"vantest1@test.com\",\"getShowOnboarding\":false,\"state\":\"Active\",\"showNewFeatures\":true},\"loginId\":\"vantest1@test.com\",\"modifier\":\"be_prod_samadmin@vandevlab.com\",\"roles\":[\"RESELLER\",\"BULK_ENROLL_RESELLER\"],\"eula\":{\"accepted\":false},\"source\":\"CLOUD\",\"userState\":\"Active\",\"contact\":{\"emailAddress\":\"vantest1@test.com\",\"officeNumber\":1111111},\"kc\":{\"creator\":\"be_prod_samadmin@vandevlab.com\",\"createTime\":{\"$date\":\"2016-04-19T21:04:28.000Z\"},\"modifier\":\"be_prod_samadmin@vandevlab.com\",\"isGlobal\":false,\"updateTime\":{\"$date\":\"2017-07-13T00:03:11.000Z\"},\"eula\":{\"accepted\":false},\"permission\":{\"sendInvitation\":false},\"state\":\"Active\",\"userType\":\"0\"},\"verification\":{\"phoneNumberVerified\":false,\"addressVerified\":false},\"creator\":\"be_prod_samadmin@vandevlab.com\",\"address\":{\"zipCode\":\"123-123\",\"country\":\"Canada\",\"city\":\"city1\",\"street1\":\"Test Street\"},\"updateTime\":{\"$date\":\"2017-07-13T00:03:11.000Z\"},\"services\":[\"KC\",\"KME\"],\"createTime\":{\"$date\":\"2016-04-19T21:04:28.000Z\"},\"tenantId\":\"1c4e9791-bb5b-11e7-bd05-24f5aad906e2\",\"name\":{\"firstName\":\"Test\",\"lastName\":\"Test\",\"fullName\":\"Test Test\"},\"rp\":{\"state\":\"Active\",\"roleType\":\"RESELLER\"}}";
////		String str2= "{\"_id\":{\"$oid\":\"5dd1966dc4de11e7993224f5\"},\"kme\":{\"loginId\":\"vantest1@test.com\",\"getShowOnboarding\":false,\"state\":\"Active\",\"showNewFeatures\":true},\"loginId\":\"vantest1@test.com\",\"modifier\":\"be_prod_samadmin@vandevlab.com\",\"roles\":[\"RESELLER\",\"BULK_ENROLL_RESELLER\"],\"eula\":{\"accepted\":false},\"source\":\"CLOUD\",\"userState\":\"Active\",\"contact\":{\"emailAddress\":\"vantest1@test.com\",\"officeNumber\":11},\"kc\":{\"creator\":\"be_prod_samadmin@vandevlab.com\",\"createTime\":{\"$date\":\"2016-04-19T21:04:28.000Z\"},\"modifier\":\"be_prod_samadmin@vandevlab.com\",\"isGlobal\":false,\"updateTime\":{\"$date\":\"2017-07-13T00:03:11.000Z\"},\"eula\":{\"accepted\":false},\"permission\":{\"sendInvitation\":false},\"state\":\"Active\",\"userType\":\"0\"},\"verification\":{\"phoneNumberVerified\":false,\"addressVerified\":false},\"creator\":\"be_prod_samadmin@vandevlab.com\",\"address\":{\"zipCode\":\"123-123\",\"country\":\"Canada\",\"city\":\"city1\",\"street1\":\"Test Street\"},\"updateTime\":{\"$date\":\"2017-07-13T00:03:11.000Z\"},\"services\":[\"KC\",\"KME\"],\"createTime\":{\"$date\":\"2016-04-19T21:04:28.000Z\"},\"tenantId\":\"1c4e9791-bb5b-11e7-bd05-24f5aad906e2\",\"name\":{\"firstName\":\"Test\",\"lastName\":\"Test\",\"fullName\":\"Test Test\"},\"rp\":{\"state\":\"Active\",\"roleType\":\"RESELLER\"}}";
//
////		String str1 = "{\"totalCount\":7,\"inviteList\":[{\"id\":64,\"firstName\":\"Ray\",\"lastName\":\"De La Cruz\",\"emailAddress\":\"raydel@cdw.com\",\"state\":\"Active\",\"primaryUserId\":467,\"secondaryUserId\":1165,\"loginId\":\"q7vrj492sg\",\"createTime\":1502214180000,\"updateTime\":1502215159000,\"creator\":\"7tk0kj4bzc\",\"modifier\":\"7tk0kj4bzc\",\"invitedByName\":\"7tk0kj4bzc\",\"primaryAdmin\":false,\"invitationState\":\"Confirm\"},{\"id\":45,\"firstName\":\"Terry\",\"lastName\":\"Mccabe\",\"emailAddress\":\"terry.mccabe@cdw.com\",\"state\":\"Pending\",\"primaryUserId\":467,\"createTime\":1500478292000,\"updateTime\":1500478292000,\"creator\":\"7tk0kj4bzc\",\"modifier\":\"7tk0kj4bzc\",\"invitedByName\":\"7tk0kj4bzc\",\"primaryAdmin\":false,\"invitationState\":\"Pending\"},{\"id\":43,\"firstName\":\"Marc\",\"lastName\":\"Klein\",\"emailAddress\":\"marklei@cdw.com\",\"state\":\"Active\",\"primaryUserId\":467,\"secondaryUserId\":883,\"loginId\":\"teir1dwv9c\",\"createTime\":1500478263000,\"updateTime\":1500479968000,\"creator\":\"7tk0kj4bzc\",\"modifier\":\"7tk0kj4bzc\",\"invitedByName\":\"7tk0kj4bzc\",\"primaryAdmin\":false,\"invitationState\":\"Confirm\"},{\"id\":42,\"firstName\":\"Joseph\",\"lastName\":\"Shema\",\"emailAddress\":\"joeshem@cdw.com\",\"state\":\"Pending\",\"primaryUserId\":467,\"createTime\":1500478249000,\"updateTime\":1500478249000,\"creator\":\"7tk0kj4bzc\",\"modifier\":\"7tk0kj4bzc\",\"invitedByName\":\"7tk0kj4bzc\",\"primaryAdmin\":false,\"invitationState\":\"Pending\"},{\"id\":16,\"firstName\":\"Matthew\",\"lastName\":\"Stoldal\",\"emailAddress\":\"Mattsto@cdw.com\",\"state\":\"Active\",\"primaryUserId\":5,\"secondaryUserId\":467,\"loginId\":\"7tk0kj4bzc\",\"createTime\":1497456385000,\"updateTime\":1497456816000,\"creator\":\"wo49llikhc\",\"modifier\":\"wo49llikhc\",\"invitedByName\":\"wo49llikhc\",\"primaryAdmin\":true,\"invitationState\":\"Confirm\"},{\"id\":15,\"firstName\":\"Max\",\"lastName\":\"Monroe\",\"emailAddress\":\"maximon@cdw.com\",\"state\":\"Pending\",\"primaryUserId\":5,\"createTime\":1497456350000,\"updateTime\":1497456350000,\"creator\":\"wo49llikhc\",\"modifier\":\"wo49llikhc\",\"invitedByName\":\"wo49llikhc\",\"primaryAdmin\":false,\"invitationState\":\"Pending\"},{\"id\":14,\"firstName\":\"Alex\",\"lastName\":\"Kopczynski\",\"emailAddress\":\"alexkop@cdw.com\",\"state\":\"Active\",\"primaryUserId\":5,\"secondaryUserId\":489,\"loginId\":\"y30gwfwmjb\",\"createTime\":1497456306000,\"updateTime\":1497542962000,\"creator\":\"wo49llikhc\",\"modifier\":\"wo49llikhc\",\"invitedByName\":\"wo49llikhc\",\"primaryAdmin\":false,\"invitationState\":\"Confirm\"}]}";
//////		String str2 = "{\"totalCount\":7,\"inviteList\":[{\"id\":64,\"firstName\":\"Ray\",\"lastName\":\"De La Cruz\",\"emailAddress\":\"raydel@cdw.com\",\"state\":\"Active\",\"primaryUserId\":467,\"secondaryUserId\":1165,\"loginId\":\"q7vrj492sg\",\"createTime\":1502214180000,\"updateTime\":1502215159000,\"creator\":\"7tk0kj4bzc\",\"modifier\":\"7tk0kj4bzc\",\"invitedByName\":\"7tk0kj4bzc\",\"primaryAdmin\":false,\"invitationState\":\"Confirm\"},{\"id\":45,\"firstName\":\"Terry\",\"lastName\":\"Mccabe\",\"emailAddress\":\"terry.mccabe@cdw.com\",\"state\":\"Pending\",\"primaryUserId\":467,\"createTime\":1500478292000,\"updateTime\":1500478292000,\"creator\":\"7tk0kj4bzc\",\"modifier\":\"7tk0kj4bzc\",\"invitedByName\":\"7tk0kj4bzc\",\"primaryAdmin\":false,\"invitationState\":\"Pending\"},{\"id\":43,\"firstName\":\"Marc\",\"lastName\":\"Klein\",\"emailAddress\":\"marklei@cdw.com\",\"state\":\"Active\",\"primaryUserId\":467,\"secondaryUserId\":883,\"loginId\":\"teir1dwv9c\",\"createTime\":1500478263000,\"updateTime\":1500479968000,\"creator\":\"7tk0kj4bzc\",\"modifier\":\"7tk0kj4bzc\",\"invitedByName\":\"7tk0kj4bzc\",\"primaryAdmin\":false,\"invitationState\":\"Confirm\"},{\"id\":42,\"firstName\":\"Joseph\",\"lastName\":\"Shema\",\"emailAddress\":\"joeshem@cdw.com\",\"state\":\"Pending\",\"primaryUserId\":467,\"createTime\":1500478249000,\"updateTime\":1500478249000,\"creator\":\"7tk0kj4bzc\",\"modifier\":\"7tk0kj4bzc\",\"invitedByName\":\"7tk0kj4bzc\",\"primaryAdmin\":false,\"invitationState\":\"Pending\"},{\"id\":16,\"firstName\":\"Matthew\",\"lastName\":\"Stoldal\",\"emailAddress\":\"Mattsto@cdw.com\",\"state\":\"Active\",\"primaryUserId\":5,\"secondaryUserId\":467,\"loginId\":\"7tk0kj4bzc\",\"createTime\":1497456385000,\"updateTime\":1497456816000,\"creator\":\"wo49llikhc\",\"modifier\":\"wo49llikhc\",\"invitedByName\":\"wo49llikhc\",\"primaryAdmin\":true,\"invitationState\":\"Confirm\"},{\"id\":15,\"firstName\":\"Max\",\"lastName\":\"Monroe\",\"emailAddress\":\"maximon@cdw.com\",\"state\":\"Pending\",\"primaryUserId\":5,\"createTime\":1497456350000,\"updateTime\":1497456350000,\"creator\":\"wo49llikhc\",\"modifier\":\"wo49llikhc\",\"invitedByName\":\"wo49llikhc\",\"primaryAdmin\":false,\"invitationState\":\"Pending\"},{\"id\":14,\"firstName\":\"Alex\",\"lastName\":\"Kopczynski\",\"emailAddress\":\"alexkop@cdw.com\",\"state\":\"Active\",\"primaryUserId\":5,\"secondaryUserId\":489,\"loginId\":\"y30gwfwmjb\",\"createTime\":1497456306000,\"updateTime\":1497542962000,\"creator\":\"wo49llikhc\",\"modifier\":\"wo49llikhc\",\"invitedByName\":\"wo49llikhc\",\"primaryAdmin\":false,\"invitationState\":\"Confirm\"}]}";
////		String str2 = "{\"totalCount\":7,\"inviteList\":[{\"id\":\"16\",\"firstName\":\"Matthew\",\"lastName\":\"Stoldal\",\"emailAddress\":\"mattsto@cdw.com\",\"state\":\"Active\",\"primaryUserId\":\"da029234c35f11e7b071f374\",\"secondaryUserId\":\"da029ab8c35f11e7b071f374\",\"loginId\":\"7tk0kj4bzc\",\"createTime\":1497456385000,\"updateTime\":1497456816000,\"invitedByName\":\"wo49llikhc\",\"primaryAdmin\":true,\"invitationState\":\"Confirm\"},{\"id\":\"14\",\"firstName\":\"Alex\",\"lastName\":\"Kopczynski\",\"emailAddress\":\"alexkop@cdw.com\",\"state\":\"Active\",\"primaryUserId\":\"da029234c35f11e7b071f374\",\"secondaryUserId\":\"da02a486c35f11e7b071f374\",\"loginId\":\"y30gwfwmjb\",\"createTime\":1497456306000,\"updateTime\":1497542962000,\"invitedByName\":\"wo49llikhc\",\"primaryAdmin\":false,\"invitationState\":\"Confirm\"},{\"id\":\"43\",\"firstName\":\"Marc\",\"lastName\":\"Klein\",\"emailAddress\":\"marklei@cdw.com\",\"state\":\"Active\",\"primaryUserId\":\"da029ab8c35f11e7b071f374\",\"secondaryUserId\":\"da02bcd2c35f11e7b071f374\",\"loginId\":\"teir1dwv9c\",\"createTime\":1500478263000,\"updateTime\":1500479968000,\"invitedByName\":\"7tk0kj4bzc\",\"primaryAdmin\":false,\"invitationState\":\"Confirm\"},{\"id\":\"64\",\"firstName\":\"Ray\",\"lastName\":\"De La Cruz\",\"emailAddress\":\"raydel@cdw.com\",\"state\":\"Active\",\"primaryUserId\":\"da029ab8c35f11e7b071f374\",\"secondaryUserId\":\"da02cc40c35f11e7b071f374\",\"loginId\":\"q7vrj492sg\",\"createTime\":1502214180000,\"updateTime\":1502215159000,\"invitedByName\":\"7tk0kj4bzc\",\"primaryAdmin\":false,\"invitationState\":\"Confirm\"},{\"id\":\"15\",\"firstName\":\"Max\",\"lastName\":\"Monroe\",\"emailAddress\":\"maximon@cdw.com\",\"state\":\"Pending\",\"primaryUserId\":\"da029234c35f11e7b071f374\",\"createTime\":1497456350000,\"updateTime\":1497456350000,\"invitedByName\":\"wo49llikhc\",\"primaryAdmin\":false,\"invitationState\":\"Pending\"},{\"id\":\"42\",\"firstName\":\"Joseph\",\"lastName\":\"Shema\",\"emailAddress\":\"joeshem@cdw.com\",\"state\":\"Pending\",\"primaryUserId\":\"da029ab8c35f11e7b071f374\",\"createTime\":1500478249000,\"updateTime\":1500478249000,\"invitedByName\":\"7tk0kj4bzc\",\"primaryAdmin\":false,\"invitationState\":\"Pending\"},{\"id\":\"45\",\"firstName\":\"Terry\",\"lastName\":\"Mccabe\",\"emailAddress\":\"terry.mccabe@cdw.com\",\"state\":\"Pending\",\"primaryUserId\":\"da029ab8c35f11e7b071f374\",\"createTime\":1500478292000,\"updateTime\":1500478292000,\"invitedByName\":\"7tk0kj4bzc\",\"primaryAdmin\":false,\"invitationState\":\"Pending\"}]}";
//		
//		String str1 = new String(Files.readAllBytes(Paths.get("C:\\Saurabh\\Work\\API Verification\\temp\\mysql.json")));
//		String str2 = new String(Files.readAllBytes(Paths.get("C:\\Saurabh\\Work\\API Verification\\temp\\mongo.json")));
//				
//		GenericParser searchApiCompare = new GenericParser();
//		searchApiCompare.addFieldToIgnore("items.id");
//		searchApiCompare.addFieldForSorting("items", "customername");
//		
//		boolean result=searchApiCompare.compare(str1, str2);
//		
//		if(result)
//			System.out.println("Success !!!");
//		else
//			System.out.println("Failed :-(");		
//	}

}
