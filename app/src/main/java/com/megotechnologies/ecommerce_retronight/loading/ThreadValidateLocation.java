package com.megotechnologies.ecommerce_retronight.loading;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Looper;

import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.db.DbConnection;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;

public class ThreadValidateLocation extends Thread{


	int CONN_TIMEOUT = 2000;
	int SOCK_TIMEOUT = 2000;

	String myCountryId = null;
	String myStateId = null;
	String myCityId = null;
	DbConnection dbC;
	
	public ThreadValidateLocation(String idCountry, String idState, String idCity, DbConnection conn) {
		// TODO Auto-generated constructor stub
		myCountryId = idCountry;
		myStateId = idState;
		myCityId = idCity;
		dbC = conn;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		MLog.log("Starting Location Validation thread.. ");
		Looper.prepare();

		Thread t = Thread.currentThread();
		String tName = t.getName();

		String jsonStr = "";

		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.
		int timeoutConnection = CONN_TIMEOUT;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.
		int timeoutSocket = SOCK_TIMEOUT;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		HttpClient httpclient = new DefaultHttpClient(httpParameters);

		HttpPost httppost = null;

		httppost = new HttpPost(MainActivity.API_VALIDATE_LOC);
		jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"idCountry\": \"" + myCountryId + "\", \"idState\": \"" + myStateId + "\", \"idCity\": \"" + myCityId + "\"}]";	

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("params", jsonStr));
		MLog.log("LOCATION VALIDATE API=" + jsonStr);

		String responseString = null;

		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			out.close();
			responseString = out.toString();
			MLog.log("Location validation=" + responseString);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {

		}

		if(responseString != null) {

			jsonStr = responseString;

			try {

				JSONObject jsonObj = new JSONObject(jsonStr);
				if(jsonObj.getString("result").equals("success")) {

					if(jsonObj.getString("value").equals("true")) {
						
						// do nothing
						
					} else {
						
						MLog.log("Deleting my location");
						
						HashMap<String, String> mapCountry = new HashMap<String, String>();
						mapCountry.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_COUNTRY);

						HashMap<String, String> mapState = new HashMap<String, String>();
						mapState.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_STATE);

						HashMap<String, String> mapCity = new HashMap<String, String>();
						mapCity.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_CITY);
						
						if(dbC.isOpen()) {
							dbC.isAvailale();
							dbC.deleteRecord(mapCountry);
							dbC.deleteRecord(mapState);
							dbC.deleteRecord(mapCity);
						}
						
						
					}
						
				}


			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}
