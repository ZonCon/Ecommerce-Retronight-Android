package com.megotechnologies.ecommerce_retronight.payment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class FragmentOrderDetails extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	public String idOrder = null;

	Thread thOrderGet;
	LinearLayout llContainer;
	TextView tvTitle;

	String name = null, email = null, phone = null, address = null, country = null, state = null, city = null, pincode = null, ts = null, status = null, total = null;
	ArrayList<String> arrItemsName = null;
	ArrayList<String> arrItemsQuantity = null;
	ArrayList<String> arrItemsCost = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		v =  inflater.inflate(R.layout.fragment_orderdetails, container, false);

		if(activity.IS_CONNECTED) {

			storeClassVariables();
			initUIHandles();
			initUIListeners();
			formatUI();

			thOrderGet = new Thread(this);
			thOrderGet.setName(MainActivity.TH_NAME_ORDERS_GET);
			thOrderGet.start();

		} else {

			AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
			alert.setMessage(MainActivity.MSG_ORDERS_DISCONNECTED);
			alert.setTitle("Error");
			alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//dismiss the dialog
					activity.loadShop();
				}
			});
			alert.create().show();

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
		llContainer = (LinearLayout)v.findViewById(R.id.ll_container);
		tvTitle = (TextView)v.findViewById(R.id.tv_label_title);
	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub

		tvTitle.setTextSize(MainActivity.TEXT_SIZE_TITLE);
		tvTitle.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);

	}

	protected Handler threadHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			Bitmap bmp = (Bitmap)msg.obj;
			if(bmp != null) {

				MLog.log("Displaying = " + msg.what);

				ImageView iv = (ImageView)v.findViewById(msg.what);
				iv.setImageBitmap(bmp);

			} else {

				ImageView iv = (ImageView)v.findViewById(msg.what);
				iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));

			}

		}

	};


	protected Handler displayHandler = new Handler() {

		public void handleMessage(Message msg) {

			MLog.log("inside display handler " + msg.what);

			if(msg.what == 1) {

				LinearLayout llRow = new LinearLayout(activity.context);
				llRow.setOrientation(LinearLayout.HORIZONTAL);
				LinearLayout.LayoutParams paramsLL = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLL.topMargin = MainActivity.SPACING;
				paramsLL.leftMargin = MainActivity.SPACING;
				paramsLL.rightMargin = MainActivity.SPACING;
				llRow.setLayoutParams(paramsLL);
				llContainer.addView(llRow);

				LinearLayout llLeft = new LinearLayout(activity.context);
				llLeft.setOrientation(LinearLayout.VERTICAL);
				LinearLayout.LayoutParams paramsLLLeft = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLLLeft.rightMargin = (MainActivity.SPACING/2);
				paramsLLLeft.weight = 2;
				llLeft.setLayoutParams(paramsLLLeft);
				llRow.addView(llLeft);
				
				LinearLayout llRight = new LinearLayout(activity.context);
				llRight.setOrientation(LinearLayout.VERTICAL);
				LinearLayout.LayoutParams paramsLLRight = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLLRight.leftMargin = (MainActivity.SPACING/2);
				paramsLLRight.weight = 3;
				llRight.setLayoutParams(paramsLLRight);
				llRow.addView(llRight);

				TextView tvAddressLab = new TextView(activity.context);
				tvAddressLab.setText("Delivery Address");
				LinearLayout.LayoutParams paramsAddLab = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				tvAddressLab.setLayoutParams(paramsAddLab);
				tvAddressLab.setTextColor(getResources().getColor(R.color.text_color));
				tvAddressLab.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TITLE);
				llLeft.addView(tvAddressLab);

				String strAddress = "";
				
				strAddress += (name + "\n");
				strAddress += (address + "\n");
				strAddress += (city + " - " + pincode + "\n");
				strAddress += (state + ", " + country + "\n\n");
				strAddress += (email + "\n");
				strAddress += (phone);
				
				TextView tvAddress = new TextView(activity.context);
				tvAddress.setText(strAddress);
				LinearLayout.LayoutParams paramsAdd = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				tvAddress.setLayoutParams(paramsAdd);
				tvAddress.setTextColor(getResources().getColor(R.color.text_color));
				tvAddress.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TILE);
				tvAddress.setLineSpacing(0, 1.2f);
				llRight.addView(tvAddress);
				
				LinearLayout llLine = new LinearLayout(activity.context);
				llLine.setOrientation(LinearLayout.HORIZONTAL);
				LinearLayout.LayoutParams paramsLLLine = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 2);
				paramsLLLine.setMargins(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
				llLine.setLayoutParams(paramsLLLine);
				llLine.setBackgroundColor(getResources().getColor(R.color.line_separator));
				llContainer.addView(llLine);
				
				// status
				
				llRow = new LinearLayout(activity.context);
				llRow.setOrientation(LinearLayout.HORIZONTAL);
				paramsLL = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLL.leftMargin = MainActivity.SPACING;
				paramsLL.rightMargin = MainActivity.SPACING;
				llRow.setLayoutParams(paramsLL);
				llContainer.addView(llRow);

				llLeft = new LinearLayout(activity.context);
				llLeft.setOrientation(LinearLayout.VERTICAL);
				paramsLLLeft = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLLLeft.rightMargin = (MainActivity.SPACING/2);
				paramsLLLeft.weight = 2;
				llLeft.setLayoutParams(paramsLLLeft);
				llRow.addView(llLeft);
				
				llRight = new LinearLayout(activity.context);
				llRight.setOrientation(LinearLayout.VERTICAL);
				paramsLLRight = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLLRight.leftMargin = (MainActivity.SPACING);
				paramsLLRight.weight = 3;
				llRight.setLayoutParams(paramsLLRight);
				llRow.addView(llRight);

				TextView tvStatusLab = new TextView(activity.context);
				tvStatusLab.setText("Status");
				LinearLayout.LayoutParams paramsStatusLab = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				tvStatusLab.setLayoutParams(paramsStatusLab);
				tvStatusLab.setTextColor(getResources().getColor(R.color.text_color));
				tvStatusLab.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TITLE);
				llLeft.addView(tvStatusLab);

				TextView tvStatus = new TextView(activity.context);
				tvStatus.setText(status);
				LinearLayout.LayoutParams paramsStatus = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				tvStatus.setLayoutParams(paramsStatus);
				tvStatus.setTextColor(getResources().getColor(R.color.text_color));
				tvStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TILE);
				tvStatus.setLineSpacing(0, 1.2f);
				llRight.addView(tvStatus);
				
				llLine = new LinearLayout(activity.context);
				llLine.setOrientation(LinearLayout.HORIZONTAL);
				paramsLLLine = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 2);
				paramsLLLine.setMargins(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
				llLine.setLayoutParams(paramsLLLine);
				llLine.setBackgroundColor(getResources().getColor(R.color.line_separator));
				llContainer.addView(llLine);
				
				// Date
				
				llRow = new LinearLayout(activity.context);
				llRow.setOrientation(LinearLayout.HORIZONTAL);
				paramsLL = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLL.leftMargin = MainActivity.SPACING;
				paramsLL.rightMargin = MainActivity.SPACING;
				llRow.setLayoutParams(paramsLL);
				llContainer.addView(llRow);

				llLeft = new LinearLayout(activity.context);
				llLeft.setOrientation(LinearLayout.VERTICAL);
				paramsLLLeft = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLLLeft.rightMargin = (MainActivity.SPACING/2);
				paramsLLLeft.weight = 2;
				llLeft.setLayoutParams(paramsLLLeft);
				llRow.addView(llLeft);
				
				llRight = new LinearLayout(activity.context);
				llRight.setOrientation(LinearLayout.VERTICAL);
				paramsLLRight = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLLRight.leftMargin = (MainActivity.SPACING);
				paramsLLRight.weight = 3;
				llRight.setLayoutParams(paramsLLRight);
				llRow.addView(llRight);

				TextView tvDateLab = new TextView(activity.context);
				tvDateLab.setText("Order Date");
				LinearLayout.LayoutParams paramsDateLab = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				tvDateLab.setLayoutParams(paramsDateLab);
				tvDateLab.setTextColor(getResources().getColor(R.color.text_color));
				tvDateLab.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TITLE);
				llLeft.addView(tvDateLab);

				TextView tvDate = new TextView(activity.context);
				tvDate.setText(ts);
				LinearLayout.LayoutParams paramsDate = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				tvDate.setLayoutParams(paramsDate);
				tvDate.setTextColor(getResources().getColor(R.color.text_color));
				tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TILE);
				tvDate.setLineSpacing(0, 1.2f);
				llRight.addView(tvDate);
				
				llLine = new LinearLayout(activity.context);
				llLine.setOrientation(LinearLayout.HORIZONTAL);
				paramsLLLine = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 2);
				paramsLLLine.setMargins(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
				llLine.setLayoutParams(paramsLLLine);
				llLine.setBackgroundColor(getResources().getColor(R.color.line_separator));
				llContainer.addView(llLine);
				
				// Date
				
				llRow = new LinearLayout(activity.context);
				llRow.setOrientation(LinearLayout.HORIZONTAL);
				paramsLL = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLL.leftMargin = MainActivity.SPACING;
				paramsLL.rightMargin = MainActivity.SPACING;
				llRow.setLayoutParams(paramsLL);
				llContainer.addView(llRow);

				llLeft = new LinearLayout(activity.context);
				llLeft.setOrientation(LinearLayout.VERTICAL);
				paramsLLLeft = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLLLeft.rightMargin = (MainActivity.SPACING/2);
				paramsLLLeft.weight = 2;
				llLeft.setLayoutParams(paramsLLLeft);
				llRow.addView(llLeft);
				
				LinearLayout llMid = new LinearLayout(activity.context);
				llMid.setOrientation(LinearLayout.VERTICAL);
				LinearLayout.LayoutParams paramsLLMid = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLLMid.leftMargin = (MainActivity.SPACING/2);
				paramsLLMid.rightMargin = (MainActivity.SPACING/2);
				paramsLLMid.weight = 2;
				llMid.setLayoutParams(paramsLLMid);
				llRow.addView(llMid);
				
				llRight = new LinearLayout(activity.context);
				llRight.setOrientation(LinearLayout.VERTICAL);
				paramsLLRight = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLLRight.leftMargin = (MainActivity.SPACING/2);
				paramsLLRight.weight = 2;
				llRight.setLayoutParams(paramsLLRight);
				llRow.addView(llRight);

				TextView tvItemLab = new TextView(activity.context);
				tvItemLab.setText("Item\n");
				LinearLayout.LayoutParams paramsItemLab = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				tvItemLab.setLayoutParams(paramsItemLab);
				tvItemLab.setTextColor(getResources().getColor(R.color.text_color));
				tvItemLab.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TITLE);
				llLeft.addView(tvItemLab);
				
				TextView tvQLab = new TextView(activity.context);
				tvQLab.setText("Nos.\n");
				LinearLayout.LayoutParams paramsQLab = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				tvQLab.setLayoutParams(paramsQLab);
				tvQLab.setGravity(Gravity.CENTER);
				tvQLab.setTextColor(getResources().getColor(R.color.text_color));
				tvQLab.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TITLE);
				llMid.addView(tvQLab);

				TextView tvPriceLab = new TextView(activity.context);
				tvPriceLab.setText("Amount\n(INR)");
				LinearLayout.LayoutParams paramsPriceLab = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				tvPriceLab.setLayoutParams(paramsPriceLab);
				tvPriceLab.setGravity(Gravity.CENTER);
				tvPriceLab.setTextColor(getResources().getColor(R.color.text_color));
				tvPriceLab.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TITLE);
				llRight.addView(tvPriceLab);

				Double grossTotal = 0.0;
				for(int i = 0; i < arrItemsCost.size(); i++) {
					
					TextView tvItem = new TextView(activity.context);
					tvItem.setText(arrItemsName.get(i));
					LinearLayout.LayoutParams paramsItem = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					tvItem.setLayoutParams(paramsItem);
					tvItem.setTextColor(getResources().getColor(R.color.text_color));
					tvItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TILE);
					llLeft.addView(tvItem);
					
					TextView tvQ = new TextView(activity.context);
					tvQ.setText(arrItemsQuantity.get(i));
					LinearLayout.LayoutParams paramsQ = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					tvQ.setLayoutParams(paramsQ);
					tvQ.setTextColor(getResources().getColor(R.color.text_color));
					tvQ.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TILE);
					tvQ.setGravity(Gravity.CENTER);
					llMid.addView(tvQ);
					
					TextView tvPrice = new TextView(activity.context);
					tvPrice.setText(arrItemsCost.get(i));
					LinearLayout.LayoutParams paramsPrice = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					tvPrice.setLayoutParams(paramsPrice);
					tvPrice.setTextColor(getResources().getColor(R.color.text_color));
					tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TILE);
					tvPrice.setGravity(Gravity.CENTER);
					llRight.addView(tvPrice);
					
					grossTotal += Double.parseDouble(tvPrice.getText().toString());
					
				}
				
				llLine = new LinearLayout(activity.context);
				llLine.setOrientation(LinearLayout.HORIZONTAL);
				paramsLLLine = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 2);
				paramsLLLine.setMargins(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
				llLine.setLayoutParams(paramsLLLine);
				llLine.setBackgroundColor(getResources().getColor(R.color.line_separator));
				llContainer.addView(llLine);
				
				// Total
				
				llRow = new LinearLayout(activity.context);
				llRow.setOrientation(LinearLayout.HORIZONTAL);
				paramsLL = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLL.leftMargin = MainActivity.SPACING;
				paramsLL.rightMargin = MainActivity.SPACING;
				llRow.setLayoutParams(paramsLL);
				llContainer.addView(llRow);

				llLeft = new LinearLayout(activity.context);
				llLeft.setOrientation(LinearLayout.VERTICAL);
				paramsLLLeft = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLLLeft.rightMargin = (MainActivity.SPACING/2);
				paramsLLLeft.weight = 2;
				llLeft.setLayoutParams(paramsLLLeft);
				llRow.addView(llLeft);
				
				llRight = new LinearLayout(activity.context);
				llRight.setOrientation(LinearLayout.VERTICAL);
				paramsLLRight = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsLLRight.leftMargin = (MainActivity.SPACING);
				paramsLLRight.weight = 3;
				llRight.setLayoutParams(paramsLLRight);
				llRow.addView(llRight);

				TextView tvTotalLab = new TextView(activity.context);
				tvTotalLab.setText("Total");
				LinearLayout.LayoutParams paramsTotalLab = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				tvTotalLab.setLayoutParams(paramsTotalLab);
				tvTotalLab.setTextColor(getResources().getColor(R.color.text_color));
				tvTotalLab.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TITLE);
				llLeft.addView(tvTotalLab);

				TextView tvTotal = new TextView(activity.context);
				tvTotal.setText(String.valueOf(grossTotal) + " INR");
				LinearLayout.LayoutParams paramsTotal = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				tvTotal.setLayoutParams(paramsTotal);
				tvTotal.setTextColor(getResources().getColor(R.color.text_color));
				tvTotal.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TITLE);
				tvTotal.setLineSpacing(0, 1.2f);
				llRight.addView(tvTotal);

			} else if(msg.what == 4) {

				

			}

		}

	};

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Looper.prepare();

		activity.handlerLoading.sendEmptyMessage(1);

		Thread t = Thread.currentThread();
		String tName = t.getName();
		String[] strArr = tName.split(";");

		MLog.log("Starting Get Orders thread.. ");

		String jsonStr = "";
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;

		httppost = new HttpPost(MainActivity.API_ORDERS_SINGLE);
		jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + activity.getEmail() + "\", \"token\": \"" + activity.getToken() + "\", \"idOrder\": \"" + idOrder + "\"}]";	

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("params", jsonStr));
		MLog.log("Get Orders API=" + jsonStr);

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

					String value = jsonObj.getString("value");
					JSONArray jsonArray = new JSONArray(value);
					MLog.log("Arr=" + jsonArray.length());

					//for(int i = 0; i < jsonArray.length(); i++) {

					if(jsonArray.length() > 0) {

						arrItemsCost = new ArrayList<String>();
						arrItemsName = new ArrayList<String>();
						arrItemsQuantity = new ArrayList<String>();

						jsonObj = jsonArray.getJSONObject(0);
						name = jsonObj.getString("nameCustomer");
						email = jsonObj.getString("emailCustomer");
						phone = jsonObj.getString("phoneCustomer");
						address = jsonObj.getString("address");
						country = jsonObj.getString("country");
						state = jsonObj.getString("state");
						city = jsonObj.getString("city");
						pincode = jsonObj.getString("pincode");
						status = jsonObj.getString("status");
						ts = jsonObj.getString("timestamp");

						String itemString = jsonObj.getString("items");
						JSONArray itemsArray = new JSONArray(itemString);

						String items = "";
						int price = 0;
						for(int j = 0; j < itemsArray.length(); j++) {

							JSONObject itemObj = itemsArray.getJSONObject(j);
							String itemName = itemObj.getString("name");
							int itemPrice = itemObj.getInt("price");
							int itemQ = itemObj.getInt("quantity");

							arrItemsName.add(itemName);
							arrItemsCost.add(itemPrice + "");
							arrItemsQuantity.add(itemQ + "");

						}

					}

					displayHandler.sendEmptyMessage(1);


				}


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
