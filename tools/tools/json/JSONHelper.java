package tools.json;

import java.io.BufferedReader;
import org.json.simple.parser.*;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONObject;

public class JSONHelper {

	public static JSONObject makeHttpPostRequest(String addr) {
		//Create connection
		JSONObject ans = null;
		URL url;
	    HttpURLConnection connection = null;  
		try{
	      url = new URL(addr);
	      connection = (HttpURLConnection)url.openConnection();
	      connection.setRequestMethod("POST");
	      connection.setRequestProperty("Content-Type", "application/json");
	      connection.setRequestProperty("Content-Language", "en-US");  
				
	      connection.setUseCaches (false);
	      connection.setDoInput(true);
	      connection.setDoOutput(true);

	      //Send request
	      DataOutputStream wr = new DataOutputStream (connection.getOutputStream ());
	      wr.flush ();
	      wr.close ();

	      //Get Response	
	      InputStream is = connection.getInputStream();
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	      String line;
	      StringBuffer response = new StringBuffer(); 
	      while((line = rd.readLine()) != null) {
	        response.append(line);
	        response.append('\r');
	      }
	      rd.close();
	      //System.out.println(response.toString());
	      ans = new JSONObject();
	      JSONParser k = new JSONParser();
	      ans = (JSONObject) k.parse(response.toString());
	      System.out.println(ans.toJSONString());

	    } catch (Exception e) {

	      e.printStackTrace();

	    } finally {

	      if(connection != null) {
	        connection.disconnect(); 
	      }
	    }
		return ans;					
	}
}
