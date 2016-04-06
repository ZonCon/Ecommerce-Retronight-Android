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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Looper;

import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.db.DbConnection;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;

public class ThreadCleanCart extends Thread{

	String myEmail = null;
	String myToken = null;
	String myCountryId = null;
	String myStateId = null;
	String myCityId = null;
	DbConnection dbC;

	public ThreadCleanCart(String email, String token, String idCountry, String idState, String idCity, DbConnection conn) {
		// TODO Auto-generated constructor stub
		myEmail = email;
		myToken = token;
		myCountryId = idCountry;
		myStateId = idState;
		myCityId = idCity;
		dbC = conn;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		MLog.log("Starting Cart thread.. ");
		Looper.prepare();

		Thread t = Thread.currentThread();
		String tName = t.getName();

		// Get open cart

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_CART);
		map.put(MainActivity.DB_COL_CART_CART_ISOPEN, MainActivity.DB_RECORD_VALUE_CART_OPEN);
		String _idOpenCart = null;
		if(dbC.isOpen()) {
			dbC.isAvailale();
			_idOpenCart = dbC.retrieveId(map);
		}

		// If open cart exists proceed else show message no item present

		if(_idOpenCart != null && _idOpenCart.length() > 0) {

			map = new HashMap<String, String>();
			map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_CART_ITEM);
			map.put(MainActivity.DB_COL_FOREIGN_KEY, _idOpenCart);
			ArrayList<HashMap<String, String>> recordsItems = null;
			if(dbC.isOpen()) {
				dbC.isAvailale();
				recordsItems = dbC.retrieveRecords(map);
			}

			MLog.log("Checking cart items " + recordsItems.size());

			if(recordsItems.size() > 0) {

				// Compose the json string for requesting the cart information from the API

				String jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + myEmail + "\", \"token\": \"" + myToken + "\", \"idCountry\": \"" + myCountryId + "\", \"idState\": \"" + myStateId + "\", \"idCity\": \"" + myCityId + "\", \"numItems\": \"" + recordsItems.size() + "\", \"items\": [";

				for(int i = 0; i < recordsItems.size(); i++) {

					map = recordsItems.get(i);
					String idStream = map.get(MainActivity.DB_COL_CART_ITEM_STREAM_SRV_ID);
					String idItem = map.get(MainActivity.DB_COL_CART_ITEM_SRV_ID);
					if(i == (recordsItems.size() - 1)) {
						jsonStr += "{\"stream\": \"" + idStream + "\", \"item\": \"" + idItem + "\"}";	
					} else {
						jsonStr += "{\"stream\": \"" + idStream + "\", \"item\": \"" + idItem + "\"},";
					}

				}

				jsonStr += "]}]";

				MLog.log(jsonStr);

				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(MainActivity.API_CART);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
				nameValuePairs.add(new BasicNameValuePair("params", jsonStr));

				MLog.log(jsonStr);

				String responseString = null;
				try {
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse response = httpclient.execute(httppost);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					responseString = out.toString();
					MLog.log(responseString);

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(responseString != null) {
					
					jsonStr = responseString;
					if(jsonStr != null && jsonStr.length() > 0) {

						MLog.log(jsonStr);

						JSONObject jsonObj;
						try {

							jsonObj = new JSONObject(jsonStr);
							if(jsonObj.getString("result").equals("success")) {

								// Loop through the items array which has been downloaded via the API

								String valueStr = jsonObj.getString("value");
								JSONArray jsonArr = new JSONArray(valueStr);
								
								// First check if both the sizes match, if they dont match some server item may have been removed, cart inconsistent
								
								if(jsonArr.length() == recordsItems.size()) {
								
									return;	
								}
								
							} 
							
							// Delete the Cart Items
							
							map = new HashMap<String, String>();
							map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_CART_ITEM);
							map.put(MainActivity.DB_COL_FOREIGN_KEY, _idOpenCart);
							if(dbC.isOpen()) {
								dbC.isAvailale();
								dbC.deleteRecord(map);
							}
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					
				}

			}

		}

	}
}
