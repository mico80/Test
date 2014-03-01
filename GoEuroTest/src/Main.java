import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;


public class Main {
	public static void main(String[]args) {
		try {
			if(args!=null && args.length>0) {
				getJson(args[0]);
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
	    String s = "https://api.goeuro.com/api/v1/suggest/position/en/name/";
	    s += URLEncoder.encode(inputStr, "UTF-8");
	    URL url = new URL(s);
	 
	    // read from the URL
	    Scanner scan = null; 
	    
	    try {
	    	scan = new Scanner(url.openStream());
	    }
	    catch (SSLHandshakeException e) {
	    	System.out.println("Cannot validate the certificate. Bypassing SSL...");
	    	disableCertificateValidation();
	    	System.out.println("OK");
	    	scan = new Scanner(url.openStream());
	    }
	    	
	    String str = new String();
	    while (scan.hasNext())
	        str += scan.nextLine();
	    scan.close();
	    
	    //str = "{\"results\":[{\"_type\":\"Position\",\"_id\":410978,\"name\":\"Potsdam, USA\",\"type\":\"location\",\"geo_position\":{\"latitude\":44.66978,\"longitude\":-74.98131}},{\"_type\":\"Position\",\"_id\":377078,\"name\":\"Potsdam, Deutschland\",\"type\":\"location\",\"geo_position\":{\"latitude\":52.39886,\"longitude\":13.06566}}]}";
	 
	    // Inspect the JSON object
	    JSONObject obj = new JSONObject(str);
	    
	    if (obj==null || !obj.has("results")) {
	    	System.out.println("No data received.");
	    	return;
	    }
	    
	    JSONArray res = obj.getJSONArray("results"); //.getJSONObject(0)
	    if(res.length()<1) {
	    	System.out.println("Empty data received.");
	    	return;
	    }
	    
	    List<Location> locations = new ArrayList<Location>();
	    
	    for(int i=0; i<res.length(); i++)
	    {
	    	JSONObject loc = res.getJSONObject(i);
	    	String _type = (loc.has("_type")) ? loc.getString("_type") : null;
	    	Integer _id = (loc.has("_id") && !loc.isNull("_id")) ? loc.getInt("_id") : null; 
	    	String name = (loc.has("name")) ? loc.getString("name") : null;
	    	String type = (loc.has("type")) ? loc.getString("type") : null; 
	    	
	    	JSONObject cc = (loc.has("geo_position")) ? loc.getJSONObject("geo_position") : null;
	    	Double latitude = (cc!=null && cc.has("latitude") && !cc.isNull("latitude")) ? cc.getDouble("latitude") : null;
	    	Double longitude = (cc!=null && cc.has("longitude") && !cc.isNull("longitude")) ? cc.getDouble("longitude") : null;
	    	
	    	locations.add(new Location(_type, _id, name, type, latitude, longitude));
	    }
	    
	    File file = null;
	    
	    try {
	    	file = new File("locations.csv");
		    StringBuffer buffer = new StringBuffer();
		    for(Location location : locations) {
		    	buffer.append(location);
		    }
		    FileUtils.writeStringToFile(file, buffer.toString());
		    System.out.println("Just wrote CSV file " + file.getAbsolutePath());
	    }
	    catch (Exception e) {
	    	System.out.println("Cannot write the CSV file");
	    }
	    
	}
	
	public static void disableCertificateValidation() {
	  // Create a trust manager that does not validate certificate chains
	  TrustManager[] trustAllCerts = new TrustManager[] { 
	    new X509TrustManager() {
	      public X509Certificate[] getAcceptedIssuers() { 
	        return new X509Certificate[0]; 
	      }
	      public void checkClientTrusted(X509Certificate[] certs, String authType) {}
	      public void checkServerTrusted(X509Certificate[] certs, String authType) {}
	  }};

	  // Ignore differences between given hostname and certificate hostname
	  HostnameVerifier hv = new HostnameVerifier() {
	    public boolean verify(String hostname, SSLSession session) { return true; }
	  };

	  // Install the all-trusting trust manager
	  try {
	    SSLContext sc = SSLContext.getInstance("SSL");
	    sc.init(null, trustAllCerts, new SecureRandom());
	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	    HttpsURLConnection.setDefaultHostnameVerifier(hv);
	  } catch (Exception e) {}
	}
}

class Location {
	public String _type; 
	public Integer _id; 
	public String name; 
	public String type; 
	public Double latitude; 
	public Double longitude;
	
	public Location(String _type, Integer _id, String name, String type, Double latitude, Double longitude) {
		this._type = _type;
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
		if(_type!=null) buffer.append(_type);
		buffer.append(COMMA);
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
