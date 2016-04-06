package com.megotechnologies.ecommerce_retronight.account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.megotechnologies.ecommerce_retronight.FragmentMeta;
import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.R;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCFragmentLifecycle;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCRunnable;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FragmentShippingLocation extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	public Boolean isBegin = false;
	Boolean RUN_FLAG = false;
	Thread countryTh, stateTh, cityTh;

	TextView tvCountry, tvState, tvCity, tvTitle, tvSubTitle;
	ArrayList<String> idCountries, idStates, idCities;
	ArrayList<String> nameCountries, nameStates, nameCities;
	ArrayAdapter<String> adapterCountries, adapterStates, adapterCities;

	Boolean loadedCity = false, loadedState = false, loadedCountry = false;
	int selCountryPos, selStatePos, selCityPos;
	String selCountryId, selCountryName, selStateId, selStateName, selCityId, selCityName;
	String myCountry = "", myState = "", myCity = "";

	Spinner spinCountry, spinState, spinCity;
	TextView butSave;

	String[] arrNil = new String[]{""};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		v =  inflater.inflate(R.layout.fragment_shipping_location, container, false);

		if(activity.IS_CONNECTED) {

			storeClassVariables();
			initUIHandles();
			initUIListeners();
			formatUI();

			loadLocation();

			if(isBegin) {
				activity.IS_CLICKABLE_FRAME = false;
			}

			setRunFlag(true);
			countryTh = new Thread(FragmentShippingLocation.this);
			countryTh.setName(MainActivity.TH_NAME_LOCATIONS_COUNTRIES);
			countryTh.start();

		} else {
			AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
			alert.setMessage(MainActivity.MSG_LOCATIONS_DISCONNECTED);
			alert.setTitle("Error");
			alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//dismiss the dialog
					activity.getFragmentManager().beginTransaction().remove(FragmentShippingLocation.this).commit();
				}
			});
			alert.create().show();
		}

		if(isBegin) {
			activity.hideHeaderFooter();
			activity.app.APP_EXIT_ON_BACK = true;
		}
		
		return v;

	}


	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(isBegin) {
			activity.IS_CLICKABLE_FRAME = true;
		}
	}

	public void loadLocation() {

		HashMap<String, String> mapCountry = new HashMap<String, String>();
		mapCountry.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_COUNTRY);

		HashMap<String, String> mapState = new HashMap<String, String>();
		mapState.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_STATE);

		HashMap<String, String> mapCity = new HashMap<String, String>();
		mapCity.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_CITY);

		HashMap<String, String> mapAddress = new HashMap<String, String>();
		mapAddress.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_ADDRESS);

		HashMap<String, String> mapPincode = new HashMap<String, String>();
		mapPincode.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_PINCODE);

		ArrayList<HashMap<String, String>> recordsCountry = null, recordsState = null, recordsCity = null, recordsAddress = null, recordsPincode = null;

		if(dbC.isOpen()) {
			dbC.isAvailale();
			recordsCountry = dbC.retrieveRecords(mapCountry);
			recordsState = dbC.retrieveRecords(mapState);
			recordsCity = dbC.retrieveRecords(mapCity);
			recordsAddress = dbC.retrieveRecords(mapAddress);
			recordsPincode = dbC.retrieveRecords(mapPincode);
		}

		if(recordsCity.size() > 0) {

			mapCountry = recordsCountry.get(0);
			mapState = recordsState.get(0);
			mapCity = recordsCity.get(0);

			myCountry = mapCountry.get(MainActivity.DB_COL_NAME);
			myState = mapState.get(MainActivity.DB_COL_NAME);
			myCity = mapCity.get(MainActivity.DB_COL_NAME);

			MLog.log("My Country " + myCountry);
			MLog.log("My State " + myState);
			MLog.log("My Cityy " + myCity);

		}

	}

	@Override
	public void initUIHandles() {
		// TODO Auto-generated method stub
		spinCountry = (Spinner)v.findViewById(R.id.spin_country);
		spinState = (Spinner)v.findViewById(R.id.spin_state);
		spinCity = (Spinner)v.findViewById(R.id.spin_city);

		tvTitle = (TextView)v.findViewById(R.id.tv_label_title);
		tvSubTitle = (TextView)v.findViewById(R.id.tv_label_subtitle);
		tvCountry = (TextView)v.findViewById(R.id.tv_label_country);
		tvState = (TextView)v.findViewById(R.id.tv_label_state);
		tvCity = (TextView)v.findViewById(R.id.tv_label_city);

		butSave = (TextView)v.findViewById(R.id.but_save);
		//butProceed = (Button)v.findViewById(R.id.but_proceed);
	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub

		spinCountry.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				//if(position < nameCountries.size()) {

				selCountryPos = position;
				selCountryId = idCountries.get(position);
				selCountryName = nameCountries.get(position);

				MLog.log(position + "," + adapterCountries.getPosition(myCountry));

				if(position != adapterCountries.getPosition(myCountry) && !loadedCountry && adapterCountries.getPosition(myCountry) != -1) {

					if(myCountry.length() > 0) {

						spinCountry.setSelection(adapterCountries.getPosition(myCountry));

					}

				} else {

					setRunFlag(true);
					stateTh = new Thread(FragmentShippingLocation.this);
					stateTh.setName(MainActivity.TH_NAME_LOCATIONS_STATES);
					stateTh.start();

				}

				loadedCountry = true;
				//}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				selCountryId = "";
				selCountryName = "";
			}
		});

		spinState.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				//if(position < nameStates.size()) {

				selStatePos = position;
				selStateId = idStates.get(position);
				selStateName = nameStates.get(position);

				if(position != adapterStates.getPosition(myState) && !loadedState && adapterStates.getPosition(myState) != -1) {

					if(myState.length() > 0) {

						spinState.setSelection(adapterStates.getPosition(myState));

					}

				} else {

					setRunFlag(true);
					cityTh = new Thread(FragmentShippingLocation.this);
					cityTh.setName(MainActivity.TH_NAME_LOCATIONS_CITIES);
					cityTh.start();

				}

				loadedState = true;

				//}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				selStateId = "";
				selStateName = "";
			}
		});

		spinCity.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				//if(position < nameCities.size()) {

				selCityPos = position;
				selCityId = idCities.get(position);
				selCityName = nameCities.get(position);

				if(position != adapterCities.getPosition(myCity) && !loadedCity && adapterCities.getPosition(myCity) != -1) {

					if(myCity.length() > 0) {

						spinCity.setSelection(adapterCities.getPosition(myCity));

					}

				} 

				loadedCity = true;

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				selCityId = "";
				selCityName = "";
			}
		});

		butSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(selCountryId != null && selCountryName != null && selStateId != null && selStateName != null && selCityId != null && selCityName != null) {

					if(selCountryId.length() > 0 && selCountryName.length() > 0 && selStateId.length() > 0 && selStateName.length() > 0 && selCityId.length() > 0 && selCityName.length() > 0) {

						if(dbC.isOpen()) {
							dbC.isAvailale();

							HashMap<String, String> mapCountry = new HashMap<String, String>();
							HashMap<String, String> mapState = new HashMap<String, String>();
							HashMap<String, String> mapCity = new HashMap<String, String>();
							HashMap<String, String> mapAddress = new HashMap<String, String>();
							HashMap<String, String> mapPincode = new HashMap<String, String>();

							mapCountry.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_COUNTRY);
							mapState.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_STATE);
							mapCity.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_CITY);
							mapAddress.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_ADDRESS);
							mapPincode.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_PINCODE);

							dbC.deleteRecord(mapCountry);
							dbC.deleteRecord(mapState);
							dbC.deleteRecord(mapCity);
							dbC.deleteRecord(mapPincode);
							dbC.deleteRecord(mapAddress);

							mapCountry = new HashMap<String, String>();
							mapState = new HashMap<String, String>();
							mapCity = new HashMap<String, String>();
							mapAddress = new HashMap<String, String>();
							mapPincode = new HashMap<String, String>();

							mapCountry.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_COUNTRY);
							mapCountry.put(MainActivity.DB_COL_NAME, selCountryName);
							mapCountry.put(MainActivity.DB_COL_SRV_ID, selCountryId);
							MLog.log("Saving " + selCountryName + " " + selCountryId);

							mapState.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_STATE);
							mapState.put(MainActivity.DB_COL_NAME, selStateName);
							mapState.put(MainActivity.DB_COL_SRV_ID, selStateId);
							MLog.log("Saving " + selStateName + " " + selStateId);

							mapCity.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_CITY);
							mapCity.put(MainActivity.DB_COL_NAME, selCityName);
							mapCity.put(MainActivity.DB_COL_SRV_ID, selCityId);
							MLog.log("Saving " + selCityName + " " + selCityId);

							dbC.insertRecord(mapCountry);
							dbC.insertRecord(mapState);
							dbC.insertRecord(mapCity);

							dbC.printRecords();

						}


						activity.populateLocation();
						MLog.log("Loading Splash...");
						activity.getFragmentManager().beginTransaction().remove(FragmentShippingLocation.this).commit();
						activity.IS_CLICKABLE_FRAME = true;
						activity.loadSplash();


					} else {

						AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
						alert.setMessage(MainActivity.MSG_BLANK_FIELDS);
						alert.setTitle("Error");
						alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								//dismiss the dialog  
							}
						});
						alert.create().show();

					}

				} else {

					AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
					alert.setMessage(MainActivity.MSG_BLANK_FIELDS);
					alert.setTitle("Error");
					alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							//dismiss the dialog  
						}
					});
					alert.create().show();

				}


			}

		});

	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub

		tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
		tvTitle.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvCountry.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvState.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvCity.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		butSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		butSave.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.dark_shadow_background));
		butSave.setPadding(MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2);
	}

	@Override
	public void storeClassVariables() {
		// TODO Auto-generated method stub
		idCountries = new ArrayList<String>();
		idStates = new ArrayList<String>();
		idCities = new ArrayList<String>();
		nameCountries = new ArrayList<String>();
		nameStates = new ArrayList<String>();
		nameCities = new ArrayList<String>();
	};

	protected Handler threadHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			MLog.log("Inside thread handler " + msg.what);

			switch (msg.what) {

			case 0:

				String jsonStr = (String)msg.obj;
				MLog.log(jsonStr);

				try {

					JSONObject jsonObj = new JSONObject(jsonStr);

					if(jsonObj.getString("result").equals("success")) {


						nameCountries.clear();
						idCountries.clear();

						String valueStr = jsonObj.getString("value");

						JSONArray jsonArr = new JSONArray(valueStr);
						for(int i = 0; i < jsonArr.length(); i++) {

							jsonObj = jsonArr.getJSONObject(i);

							String name = jsonObj.getString("name");

							String mapped = jsonObj.getString("mapped");							
							if(mapped.equals("1")){

								String id = jsonObj.getString("idCountries");
								idCountries.add(id);				
								nameCountries.add(name);

							}

						}

						String[] mStringArray = new String[nameCountries.size()];
						mStringArray = nameCountries.toArray(mStringArray);
						adapterCountries = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_spinner_item, mStringArray);
						spinCountry.setAdapter(adapterCountries);

					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;

			case 1:

				jsonStr = (String)msg.obj;
				MLog.log("States=" + jsonStr);

				try {

					JSONObject jsonObj = new JSONObject(jsonStr);

					if(jsonObj.getString("result").equals("success")) {

						nameStates.clear();
						idStates.clear();

						String valueStr = jsonObj.getString("value");
						JSONArray jsonArr = new JSONArray(valueStr);
						for(int i = 0; i < jsonArr.length(); i++) {

							jsonObj = jsonArr.getJSONObject(i);

							String name = jsonObj.getString("name");

							MLog.log("States=" + name);							

							String mapped = jsonObj.getString("statesmapped");							
							if(mapped.equals("1")){

								String id = jsonObj.getString("idStates");
								idStates.add(id);
								nameStates.add(name);

							}
						}

						MLog.log("States=" + nameStates.size());

						String[] mStringArray = new String[nameStates.size()];
						mStringArray = nameStates.toArray(mStringArray);
						adapterStates = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_spinner_item, mStringArray);
						spinState.setAdapter(adapterStates);

					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;

			case 2:

				jsonStr = (String)msg.obj;

				try {

					JSONObject jsonObj = new JSONObject(jsonStr);

					if(jsonObj.getString("result").equals("success")) {

						nameCities.clear();
						idCities.clear();

						String valueStr = jsonObj.getString("value");
						JSONArray jsonArr = new JSONArray(valueStr);
						for(int i = 0; i < jsonArr.length(); i++) {

							jsonObj = jsonArr.getJSONObject(i);

							String name = jsonObj.getString("name");

							String mapped = jsonObj.getString("mapped");							
							if(mapped.equals("1")){

								String id = jsonObj.getString("idCities");
								idCities.add(id);
								nameCities.add(name);

							}
						}

						String[] mStringArray = new String[nameCities.size()];
						mStringArray = nameCities.toArray(mStringArray);
						adapterCities = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_spinner_item, mStringArray);
						spinCity.setAdapter(adapterCities);

					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;


			default:
				break;
			}


		};

	};

	@Override
	public void run() {

		Looper.prepare();

		MainActivity a = (MainActivity)getActivity();
		//a.handlerLoading.sendEmptyMessage(1);

		MLog.log("Starting thread..." + RUN_FLAG);

		if(RUN_FLAG) {

			String jsonStr = "";
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = MainActivity.HTTP_TIMEOUT;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = MainActivity.SOCKET_TIMEOUT;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpPost httppost = null;

			Thread t = Thread.currentThread();
			String tName = t.getName();
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

			if(tName.equals(MainActivity.TH_NAME_LOCATIONS_COUNTRIES)) {

				httppost = new HttpPost(MainActivity.API_COUNTRIES);
				nameValuePairs.add(new BasicNameValuePair("params", "[{\"idProject\": \"" + MainActivity.PID + "\"}]"));

			} else if(tName.equals(MainActivity.TH_NAME_LOCATIONS_STATES)) {

				httppost = new HttpPost(MainActivity.API_STATES);
				nameValuePairs.add(new BasicNameValuePair("params", "[{\"idProject\": \"" + MainActivity.PID + "\", \"idCountry\": \"" + selCountryId + "\"}]"));

			} else if(tName.equals(MainActivity.TH_NAME_LOCATIONS_CITIES)){

				httppost = new HttpPost(MainActivity.API_CITIES);
				nameValuePairs.add(new BasicNameValuePair("params", "[{\"idProject\": \"" + MainActivity.PID + "\", \"idState\": \"" + selStateId + "\"}]"));

			}

			try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				String responseString = out.toString();
				MLog.log(responseString);
				Message msg = new Message();
				msg.obj = responseString;
				if(tName.equals(MainActivity.TH_NAME_LOCATIONS_COUNTRIES)) {
					msg.what = 0;
				} else if(tName.equals(MainActivity.TH_NAME_LOCATIONS_STATES)) {
					msg.what = 1;
				} else if(tName.equals(MainActivity.TH_NAME_LOCATIONS_CITIES)) {
					msg.what = 2;
				} else {
					msg.what = 3;
				}
				threadHandler.sendMessage(msg);
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


			setRunFlag(false);

		}

		//a.handlerLoading.sendEmptyMessage(0);
		//a.hideLoading();

	}

	@Override
	public void setRunFlag(Boolean value) {
		// TODO Auto-generated method stub
		RUN_FLAG = value;
	}

}
