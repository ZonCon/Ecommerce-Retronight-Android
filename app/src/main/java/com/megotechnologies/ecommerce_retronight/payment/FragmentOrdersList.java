package com.megotechnologies.ecommerce_retronight.payment;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
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
import com.megotechnologies.ecommerce_retronight.account.FragmentAccountLogin;
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

public class FragmentOrdersList extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	String idOrderSelected = null;
	public int offset = 0;
	public Boolean isNewsstream = false;
	public Boolean isAlertstream = false;

	ArrayList<String> arrIdOrders = null,  arrTsOrders = null,  arrItemsOrders = null,  arrPrices = null;

	Thread thOrdersGet, thLoginChecker;
	int IV_ID_PREFIX = 4000, TV_ID_PREFIX = 6000;
	LinearLayout llContainer;
	TextView tvLoadmore, tvTitle;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		activity.lastCreatedActivity = MainActivity.SCREEN_STREAMS;

		v =  inflater.inflate(R.layout.fragment_orderslist, container, false);

		if(activity.IS_CONNECTED) {

			storeClassVariables();
			initUIHandles();
			initUIListeners();
			formatUI();

			thOrdersGet = new Thread(this);
			thOrdersGet.setName(MainActivity.TH_NAME_ORDERS_GET);
			thOrdersGet.start();

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
		tvLoadmore = (TextView)v.findViewById(R.id.tv_loadmore);
		tvTitle = (TextView)v.findViewById(R.id.tv_label_title);
	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub

		tvLoadmore.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				thOrdersGet = new Thread(FragmentOrdersList.this);
				thOrdersGet.setName(MainActivity.TH_NAME_ORDERS_GET);
				thOrdersGet.start();

			}

		});

	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub

		tvTitle.setTextSize(MainActivity.TEXT_SIZE_TITLE);
		tvTitle.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);

		tvLoadmore.setTextSize(MainActivity.TEXT_SIZE_TILE);
		tvLoadmore.setTextColor(getResources().getColor(R.color.light_gray));

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

				if(arrIdOrders.size() > 0) {

					if(offset == 0) {

						llContainer.removeAllViews();

					}

					for(int i = 0; i < arrIdOrders.size(); i++) {

						offset++;

						LinearLayout llRow = new LinearLayout(activity.context);
						llRow.setOrientation(LinearLayout.HORIZONTAL);
						LinearLayout.LayoutParams paramsLL = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
						paramsLL.topMargin = MainActivity.SPACING;
						paramsLL.leftMargin = MainActivity.SPACING;
						paramsLL.rightMargin = MainActivity.SPACING;
						llRow.setLayoutParams(paramsLL);
						llContainer.addView(llRow);

						final String idOrder = arrIdOrders.get(i);
						llRow.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub

								if(activity.IS_CLICKABLE_FRAGMENT) {

									activity.IS_CLICKABLE_FRAGMENT = false;

									thLoginChecker = new Thread(FragmentOrdersList.this);
									thLoginChecker.setName(MainActivity.TH_NAME_VERIFY_LOGIN);
									thLoginChecker.start();

									idOrderSelected = idOrder;

								} 

							}

						});

						LinearLayout llLeft = new LinearLayout(activity.context);
						llLeft.setOrientation(LinearLayout.VERTICAL);
						LinearLayout.LayoutParams paramsLLLeft = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
						//paramsLLLeft.leftMargin = (MainActivity.SPACING);
						paramsLLLeft.rightMargin = (MainActivity.SPACING/2);
						paramsLLLeft.weight = 1;
						llLeft.setLayoutParams(paramsLLLeft);
						//llLeft.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
						llRow.addView(llLeft);

						TextView tvPrice = new TextView(activity.context);
						tvPrice.setText(arrPrices.get(i) + " INR");
						LinearLayout.LayoutParams paramsPrice = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
						tvPrice.setLayoutParams(paramsPrice);
						tvPrice.setTextColor(getResources().getColor(R.color.text_color));
						tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TITLE - 3);
						//tvRightTitle.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
						//tvPrice.setEllipsize(TruncateAt.END);
						tvPrice.setSingleLine();
						llLeft.addView(tvPrice);

						LinearLayout llRight = new LinearLayout(activity.context);
						llRight.setOrientation(LinearLayout.VERTICAL);
						LinearLayout.LayoutParams paramsLLRight = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
						paramsLLRight.leftMargin = (MainActivity.SPACING);
						paramsLLRight.rightMargin = (MainActivity.SPACING/2);
						paramsLLRight.weight = 3;
						llRight.setLayoutParams(paramsLLRight);
						//llRight.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
						llRow.addView(llRight);

						TextView tvItems = new TextView(activity.context);
						tvItems.setText(arrItemsOrders.get(i));
						LinearLayout.LayoutParams paramsItems = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
						tvItems.setLayoutParams(paramsItems);
						tvItems.setTextColor(getResources().getColor(R.color.text_color));
						tvItems.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TITLE);
						//tvRightTitle.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
						tvItems.setEllipsize(TruncateAt.END);
						llRight.addView(tvItems);

						TextView tvTs = new TextView(activity.context);
						tvTs.setText("Ordered on " + arrTsOrders.get(i));
						LinearLayout.LayoutParams paramsTs = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
						tvTs.setLayoutParams(paramsTs);
						tvTs.setTextColor(getResources().getColor(R.color.text_color));
						tvTs.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TILE);
						tvTs.setPadding(0, MainActivity.SPACING, 0, 0);
						tvTs.setEllipsize(TruncateAt.END);
						llRight.addView(tvTs);
						
						LinearLayout llLine = new LinearLayout(activity.context);
						llLine.setOrientation(LinearLayout.HORIZONTAL);
						LinearLayout.LayoutParams paramsLLLine = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 2);
						paramsLLLine.setMargins(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
						llLine.setLayoutParams(paramsLLLine);
						llLine.setBackgroundColor(getResources().getColor(R.color.line_separator));

						llContainer.addView(llLine);


					}


				} else {


					AlertDialog.Builder alert = new AlertDialog.Builder(activity.context);
					if(offset == 0) {
						alert.setMessage(MainActivity.MSG_ORDERS_NOITEMS);	
					} else {
						alert.setMessage(MainActivity.MSG_ORDERS_END);
					}
					alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							// TODO Auto-generated method stub


						}

					});
					alert.show();

				}


			} else if(msg.what == 4) {

				if(!IS_SIGNEDIN) {

					FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
					FragmentAccountLogin fragment = new FragmentAccountLogin();
					fragment.isBegin = true;
					fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_LOGIN_ACCOUNT)
					.addToBackStack(MainActivity.SCREEN_LOGIN_ACCOUNT)
					.commit();


				} else {

					FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
					FragmentOrderDetails fragment = new FragmentOrderDetails();
					fragment.idOrder = idOrderSelected;
					fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_ORDER_DETAILS)
					.addToBackStack(MainActivity.SCREEN_ORDER_DETAILS)
					.commit();

				}

			}

		}

	};


	@Override
	public void run() {
		// TODO Auto-generated method stub
		Looper.prepare();

		Thread t = Thread.currentThread();
		String tName = t.getName();

		activity.handlerLoading.sendEmptyMessage(1);

		if(tName.equals(MainActivity.TH_NAME_VERIFY_LOGIN)) {

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
							displayHandler.sendEmptyMessage(4);
							activity.handlerLoading.sendEmptyMessage(0);
							return;
						}

					}

					displayHandler.sendEmptyMessage(4);
					IS_SIGNEDIN = false;


				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		} else if(tName.equals(MainActivity.TH_NAME_ORDERS_GET)) {

			MLog.log("Starting Get Orders thread.. ");

			String jsonStr = "";
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;

			httppost = new HttpPost(MainActivity.API_ORDERS_LIST);
			jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + activity.getEmail() + "\", \"token\": \"" + activity.getToken() + "\", \"offset\": \"" + offset + "\"}]";	

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

						arrIdOrders = new ArrayList<String>();
						arrTsOrders = new ArrayList<String>();
						arrItemsOrders = new ArrayList<String>();
						arrPrices = new ArrayList<String>();

						for(int i = 0; i < jsonArray.length(); i++) {

							jsonObj = jsonArray.getJSONObject(i);
							String idOrder = jsonObj.getString("idOrders");
							String timestamp = jsonObj.getString("timestamp");
							String itemString = jsonObj.getString("items");
							String statusString = jsonObj.getString("status");
							JSONArray itemsArray = new JSONArray(itemString);

							String items = "";
							int price = 0;
							for(int j = 0; j < itemsArray.length(); j++) {


								JSONObject itemObj = itemsArray.getJSONObject(j);
								String itemName = itemObj.getString("name");
								int itemPrice = itemObj.getInt("price");

								if(j == (itemsArray.length() - 1)) {

									items += itemName;

								} else {

									items += (itemName + " / ");
								}

								price += itemPrice;

							}

							//if(statusString.contains(MainActivity.ORDER_CANCELLED) || statusString.contains(MainActivity.ORDER_PROCESSING) || statusString.contains(MainActivity.ORDER_COMPLETE)) {

								arrIdOrders.add(idOrder);
								arrTsOrders.add(timestamp);
								arrItemsOrders.add(items);
								arrPrices.add(String.valueOf(price));

							//}

						}

						displayHandler.sendEmptyMessage(1);

					}


				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		activity.handlerLoading.sendEmptyMessage(0);
	}

	@Override
	public void setRunFlag(Boolean value) {
		// TODO Auto-generated method stub

	}

}
