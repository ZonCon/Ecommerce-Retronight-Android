package com.megotechnologies.ecommerce_retronight.payment;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.megotechnologies.ecommerce_retronight.FragmentMeta;
import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.R;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCFragmentLifecycle;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCRunnable;
import com.megotechnologies.ecommerce_retronight.account.FragmentAccountDetectLogin;
import com.megotechnologies.ecommerce_retronight.dataobjects.PGRecord;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;
import com.megotechnologies.ecommerce_retronight.utilities.Validator;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FragmentPaymentCheckout extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	LinearLayout llCrumbs;
	TextView tvName, tvEmail, tvPhone, tvAddress, tvCountry, tvState, tvCity, tvPincode, tvTitle, tvCrumbsCart, tvCrumbsCheckout, tvCrumbsConfirm, tvCrumbsPayment;
	EditText editName, editEmail, editPhone, editAddress, editCountry, editState, editCity, editPincode;
	TextView butNext, butPrev;
	Thread thLoginChecker;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		v =  inflater.inflate(R.layout.fragment_payment_checkout, container, false);


		if(activity.IS_CONNECTED) {
			storeClassVariables();
			initUIHandles();
			initUIListeners();
			formatUI();

			HashMap<String, String> map = new HashMap<String, String>();
			map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_COUNTRY);
			ArrayList<HashMap<String, String>> recordsCountries = null;
			if(dbC.isOpen()) {
				dbC.isAvailale();
				recordsCountries = dbC.retrieveRecords(map);
			}

			map = new HashMap<String, String>();
			map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_STATE);
			ArrayList<HashMap<String, String>> recordsStates = null;
			if(dbC.isOpen()) {
				dbC.isAvailale();
				recordsStates = dbC.retrieveRecords(map);
			}

			map = new HashMap<String, String>();
			map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_CITY);
			ArrayList<HashMap<String, String>> recordsCities = null;
			if(dbC.isOpen()) {
				dbC.isAvailale();
				recordsCities = dbC.retrieveRecords(map);
			}

			map = new HashMap<String, String>();
			map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_PHONE);
			ArrayList<HashMap<String, String>> recordsPhone = null;
			if(dbC.isOpen()) {
				dbC.isAvailale();
				recordsPhone = dbC.retrieveRecords(map);
			}


			map = new HashMap<String, String>();
			map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_NAME);
			ArrayList<HashMap<String, String>> recordsName = null;
			if(dbC.isOpen()) {
				dbC.isAvailale();
				recordsName = dbC.retrieveRecords(map);
			}

			map = new HashMap<String, String>();
			map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_ADDRESS);
			ArrayList<HashMap<String, String>> recordsAddress = null;
			if(dbC.isOpen()) {
				dbC.isAvailale();
				recordsAddress = dbC.retrieveRecords(map);
			}

			map = new HashMap<String, String>();
			map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_PINCODE);
			ArrayList<HashMap<String, String>> recordsPincode = null;
			if(dbC.isOpen()) {
				dbC.isAvailale();
				recordsPincode = dbC.retrieveRecords(map);
			}

			String nameCountry = "";
			String nameState = "";
			String nameCity = "";
			String nameEmail = activity.getEmail();
			String namePhone = "";
			String name = "";
			String address = "";
			String pincode = "";

			if(recordsCountries.size() > 0) {
				map = recordsCountries.get(0);
				nameCountry = map.get(MainActivity.DB_COL_NAME);			
			}

			if(recordsStates.size() > 0) {
				map = recordsStates.get(0);
				nameState = map.get(MainActivity.DB_COL_NAME);
			}

			if(recordsCities.size() > 0) {
				map = recordsCities.get(0);
				nameCity = map.get(MainActivity.DB_COL_NAME);
			}

			if(recordsPhone.size() > 0) {
				map = recordsPhone.get(0);
				namePhone = map.get(MainActivity.DB_COL_PHONE);
			}

			if(recordsName.size() > 0) {
				map = recordsName.get(0);
				name = map.get(MainActivity.DB_COL_NAME);
			}

			if(recordsAddress.size() > 0) {
				map = recordsAddress.get(0);
				address = map.get(MainActivity.DB_COL_NAME);
			}

			if(recordsPincode.size() > 0) {
				map = recordsPincode.get(0);
				pincode = map.get(MainActivity.DB_COL_NAME);
			}

			editCountry.setText(nameCountry);
			editCountry.setEnabled(false);
			editState.setText(nameState);
			editState.setEnabled(false);
			editCity.setText(nameCity);
			editCity.setEnabled(false);
			editEmail.setText(nameEmail);
			if(IS_SIGNEDIN) {
				editEmail.setEnabled(false);

				//editEmail.setBackground(activity.getResources().getDrawable(R.drawable.gray_but_background));
			}

			editPhone.setText(namePhone);
			editName.setText(name);
			editAddress.setText(address);
			editPincode.setText(pincode);
		} else {
			AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
			alert.setMessage(MainActivity.MSG_CART_DISCONNECTED);
			alert.setTitle("Error");
			alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//dismiss the dialog
					activity.loadShop();
				}
			});
		}

		return v;
	}

	@Override
	public void storeClassVariables() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initUIHandles() {
		// TODO Auto-generated method stub
		tvTitle = (TextView)v.findViewById(R.id.tv_label_title);
		tvName = (TextView)v.findViewById(R.id.tv_label_name);
		tvEmail = (TextView)v.findViewById(R.id.tv_label_email);
		tvPhone = (TextView)v.findViewById(R.id.tv_label_phone);
		tvAddress = (TextView)v.findViewById(R.id.tv_label_address);
		tvCountry = (TextView)v.findViewById(R.id.tv_label_country);
		tvState = (TextView)v.findViewById(R.id.tv_label_state);
		tvCity = (TextView)v.findViewById(R.id.tv_label_city);
		tvPincode = (TextView)v.findViewById(R.id.tv_label_pincode);

		editName = (EditText)v.findViewById(R.id.edit_name);
		editEmail = (EditText)v.findViewById(R.id.edit_email);
		editPhone = (EditText)v.findViewById(R.id.edit_phone);
		editAddress = (EditText)v.findViewById(R.id.edit_address);
		editCountry = (EditText)v.findViewById(R.id.edit_country);
		editState = (EditText)v.findViewById(R.id.edit_state);
		editCity = (EditText)v.findViewById(R.id.edit_city);
		editPincode = (EditText)v.findViewById(R.id.edit_pincode);

		butNext = (TextView)v.findViewById(R.id.but_next);
		butPrev = (TextView)v.findViewById(R.id.but_prev);
		
		llCrumbs = (LinearLayout)v.findViewById(R.id.ll_crumbs);
		tvCrumbsCart = (TextView)v.findViewById(R.id.tv_crumbs_cart);
		tvCrumbsCheckout = (TextView)v.findViewById(R.id.tv_crumbs_checkout);
		tvCrumbsConfirm = (TextView)v.findViewById(R.id.tv_crumbs_confirm);
		tvCrumbsPayment = (TextView)v.findViewById(R.id.tv_crumbs_payment);
	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub

		butNext.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				final String name = editName.getText().toString();
				final String email = editEmail.getText().toString();
				final String phone = editPhone.getText().toString();
				final String address = editAddress.getText().toString();
				final String country = editCountry.getText().toString();
				final String state = editState.getText().toString();
				final String city = editCity.getText().toString();
				final String pincode = editPincode.getText().toString();

				if(name.length() > 0 && email.length() > 0 && phone.length() > 0 && address.length() > 0 && country.length() > 0 && state.length() > 0 && city.length() > 0 && pincode.length() > 0) {

					if(!Validator.isValidPhone(phone)) {

						AlertDialog.Builder builder = new AlertDialog.Builder(activity.context);
						builder.setMessage(MainActivity.MSG_PHONE_INVALID);
						builder.setPositiveButton(MainActivity.MSG_OK, null);
						builder.show();
						return;

					}

					if(!Validator.isValidPincode(pincode)) {

						AlertDialog.Builder builder = new AlertDialog.Builder(activity.context);
						builder.setMessage(MainActivity.MSG_PINCODE_INVALID);
						builder.setPositiveButton(MainActivity.MSG_OK, null);
						builder.show();
						return;

					}

					HashMap<String, String> map = new HashMap<String, String>();
					map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_NAME);
					if(dbC.isOpen()) {
						dbC.isAvailale();
						dbC.deleteRecord(map);
						map.put(MainActivity.DB_COL_NAME, name);
						dbC.insertRecord(map);
					}

					map = new HashMap<String, String>();
					map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_PHONE);
					if(dbC.isOpen()) {
						dbC.isAvailale();
						dbC.deleteRecord(map);
						map.put(MainActivity.DB_COL_PHONE, phone);
						dbC.insertRecord(map);
					}

					map = new HashMap<String, String>();
					map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_ADDRESS);
					if(dbC.isOpen()) {
						dbC.isAvailale();
						dbC.deleteRecord(map);
						map.put(MainActivity.DB_COL_NAME, address);
						dbC.insertRecord(map);
					}

					map = new HashMap<String, String>();
					map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_PINCODE);
					if(dbC.isOpen()) {
						dbC.isAvailale();
						dbC.deleteRecord(map);
						map.put(MainActivity.DB_COL_NAME, pincode);
						dbC.insertRecord(map);
					}

					PGRecord pgRecord = new PGRecord();
					pgRecord.billingName = name;
					pgRecord.billingAddress = address;
					pgRecord.billingCity = city;
					pgRecord.billingState = state;
					pgRecord.billingCountry = country;
					pgRecord.billingEmail = email;
					pgRecord.billingTel = phone;
					pgRecord.billingZip = pincode;

					FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
					FragmentAccountDetectLogin fragment = new FragmentAccountDetectLogin();
					fragment.pgRecord = pgRecord;
					fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_CONFIRM)
							.addToBackStack(MainActivity.SCREEN_CONFIRM)
							.commit();

				} else {

					AlertDialog.Builder builder = new AlertDialog.Builder(activity.context);
					builder.setMessage(MainActivity.MSG_BLANK_FIELDS);
					builder.setPositiveButton(MainActivity.MSG_OK, null);
					builder.show();

				}

			}

		});
		
		butPrev.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				activity.onBackPressed();

			}

		});

	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub

		butNext.setTextColor(getResources().getColor(R.color.white));
		butNext.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		butNext.setText("PROCEED TO CONFIRM");
		butNext.setGravity(Gravity.CENTER);
		butNext.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_shadow_background));
		butNext.setPadding(MainActivity.SPACING, MainActivity.SPACING / 2, MainActivity.SPACING, MainActivity.SPACING / 2);
		
		butPrev.setTextColor(getResources().getColor(R.color.black));
		butPrev.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		butPrev.setText("BACK TO CART");
		butPrev.setGravity(Gravity.CENTER);
		butPrev.setBackgroundDrawable(getResources().getDrawable(R.drawable.yellow_shadow_background));
		butPrev.setPadding(MainActivity.SPACING, MainActivity.SPACING / 2, MainActivity.SPACING, MainActivity.SPACING / 2);

		tvCrumbsCart.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) ((MainActivity.TEXT_SIZE_TILE / 3) * 2.5));
		tvCrumbsCheckout.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int)((MainActivity.TEXT_SIZE_TILE/3)*2.5));
		tvCrumbsConfirm.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) ((MainActivity.TEXT_SIZE_TILE / 3) * 2.5));
		tvCrumbsPayment.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int)((MainActivity.TEXT_SIZE_TILE/3)*2.5));

		tvTitle.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);

	}
	
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		activity.handlerLoading.sendEmptyMessage(1);
		
		MLog.log("Starting Verify Login thread.. ");

		String jsonStr = "";
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;

		httppost = new HttpPost(MainActivity.API_VERIFY_LOGIN);
		jsonStr = "[{\"email\": \"" + activity.getEmail() + "\", \"token\": \"" + activity.getToken() + "\"}]";	

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("params", jsonStr));
		MLog.log("VerifyStream API=" + jsonStr);

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

			try {

				JSONObject jsonObj = new JSONObject(jsonStr);
				if(jsonObj.getString("result").equals("success")) {

					String valueStr = jsonObj.getString("value");

					if(valueStr.equals("true")) {
						IS_SIGNEDIN = true;
						activity.handlerLoading.sendEmptyMessage(0);
						return;
					}
					
				}

				IS_SIGNEDIN = false;


			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		activity.handlerLoading.sendEmptyMessage(0);

	}

	@Override
	public void setRunFlag(Boolean value) {
		// TODO Auto-generated method stub

	}

}
