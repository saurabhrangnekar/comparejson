import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

public class TempTest
{
    private static List<String> intersectLists(List<String> lst1, List<String> lst2)
    {
        List<String> result = null;
        if(lst1 != null && lst2 != null)
        {
        	result = new ArrayList<String>(lst2) ;
        	result.retainAll(lst1);
        }
        else if(lst1 != null && lst2 == null)
        {
        	result = new ArrayList<String>(lst1) ; 
        }
        else if(lst1 == null && lst2 != null)
        {
        	result = new ArrayList<String>(lst2);
        }
        return result;
    }
    
	public static void main(String[] args) throws UnsupportedEncodingException
	{
		char[] c = new char[10];
		System.out.println("*"+ c[0] +"*");
		
//		List<String> lst1 = Arrays.asList("a","b","c");
//		List<String> lst2 = Arrays.asList("c","d","e");
//		
//		System.out.println(intersectLists(null,null));
//		System.out.println(intersectLists(null,lst2));
//		System.out.println(intersectLists(lst1,null));
//		System.out.println(intersectLists(lst1,lst2));
		
//		String searchText="हरी";
//		System.out.println("Original :- "+searchText);
//		searchText=StringEscapeUtils.escapeJava(searchText).trim();
//		System.out.println("Escaped :- "+searchText);
//		System.out.println(StringEscapeUtils.unescapeJava(searchText));			
//		System.out.println(URLDecoder.decode(searchText,"utf-8"));
//		System.out.println(URLEncoder.encode(searchText,"utf-8"));		
//		
//		System.out.println(new String(searchText.getBytes(), "utf-8"));		
		
//		String alpha = "    Pending    ,  try  ,        SEAP_Pending  ";
//		List<String> result = Arrays.asList(alpha.trim().split("\\s*,\\s*"));
//		System.out.println(result);
//		System.out.println("**"+result.get(0)+"**");
//		System.out.println("**"+result.get(1)+"**");
//		System.out.println("**"+result.get(2)+"**");
		
//		List<String> models = Arrays.asList("bcd", "abc", "mnp", null);
//		Collections.sort(list);
		
//		List<String> models = new ArrayList<>();
//		models.add("bcd");
//		models.add("abc");
//		models.add("mnp");
//		models.add(null);
		
		
//		String tags = ""; 
//		System.out.println("Start");
//		for(String str : tags.split(","))
//		{
//			System.out.println("**"+str+"**");			
//		}
//		System.out.println("End");
	}

}
