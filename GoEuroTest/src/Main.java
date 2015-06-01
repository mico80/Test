import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;


public class Main {
	public static void main(String[]args) {
		try {
			if(args!=null && args.length>0) {
				String cityName = "";
				for (int i = 0; i < args.length; i++) {
					cityName += args[i].replace(" ", "");
				}
				getJson(cityName);
			}
			else {
				System.out.println("Please insert a valid string. Try again...");
			}
		} catch (Exception e) {
			System.out.println("Error");
			e.printStackTrace();
		}
	}
	
	public static void getJson(String inputStr) throws Exception
	{
	    String s = "http://api.goeuro.com/api/v2/position/suggest/en/";
	    s += URLEncoder.encode(inputStr, "UTF-8");
	    URL url = new URL(s);
	 
	    // read from the URL
	    Scanner scan = null; 
	    
	    try {
	    	System.out.println("Opening url " + url);
	    	scan = new Scanner(url.openStream(), "UTF-8");
	    }
	    catch (IOException e) {
	    	System.out.println("ERROR: Cannot open url " + url);
	    	return;
	    }
	    	
	    String str = new String();
	    while (scan.hasNext())
	        str += scan.nextLine();
	    scan.close();
	    
	    // Inspect the JSON object
	    JSONArray res = new JSONArray(str);
	    
	    if (res==null || res.length()<1) {
	    	System.out.println("No data received.");
	    	return;
	    }
	    
	    List<Location> locations = new ArrayList<Location>();
	    
	    for(int i=0; i<res.length(); i++)
	    {
	    	JSONObject loc = res.getJSONObject(i);
	    	Integer _id = (loc.has("_id") && !loc.isNull("_id")) ? loc.getInt("_id") : null; 
	    	String name = (loc.has("name")) ? loc.getString("name") : null;
	    	String type = (loc.has("type")) ? loc.getString("type") : null; 
	    	
	    	JSONObject cc = (loc.has("geo_position")) ? loc.getJSONObject("geo_position") : null;
	    	Double latitude = (cc!=null && cc.has("latitude") && !cc.isNull("latitude")) ? cc.getDouble("latitude") : null;
	    	Double longitude = (cc!=null && cc.has("longitude") && !cc.isNull("longitude")) ? cc.getDouble("longitude") : null;
	    	
	    	locations.add(new Location(_id, name, type, latitude, longitude));
	    }
	    
	    File file = null;
	    
	    try {
	    	file = new File("locations.csv");
	    	StringBuffer buffer = new StringBuffer();
		    for(Location location : locations) {
		    	buffer.append(location);
		    }
		    System.out.println(buffer.toString());
		    FileUtils.writeStringToFile(file, buffer.toString());
		    System.out.println("Just wrote CSV file " + file.getAbsolutePath());
	    }
	    catch (Exception e) {
	    	System.out.println("Cannot write the CSV file");
	    }
	    
	}
}

class Location {
	public Integer _id; 
	public String name; 
	public String type; 
	public Double latitude; 
	public Double longitude;
	
	public Location(Integer _id, String name, String type, Double latitude, Double longitude) {
		this._id = _id;
		this.name = name; 
		this.type = type; 
		this.latitude = latitude; 
		this.longitude = longitude;
	}
	
	@Override
	public String toString() {
		final char COMMA = ',';
		StringBuffer buffer = new StringBuffer();
		if(_id!=null) buffer.append(_id);
		buffer.append(COMMA);
		if(name!=null) buffer.append(name);
		buffer.append(COMMA);
		if(type!=null) buffer.append(type);
		buffer.append(COMMA);
		if(latitude!=null) buffer.append(latitude);
		buffer.append(COMMA);
		if(longitude!=null) buffer.append(longitude);
		buffer.append(System.lineSeparator());
		return buffer.toString();
	}
}
