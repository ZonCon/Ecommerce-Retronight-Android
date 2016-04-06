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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Looper;

import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.db.DbConnection;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;

public class ThreadStreams extends Thread{

	int CONN_TIMEOUT = 2000;
	int SOCK_TIMEOUT = 2000;

	String myCountryId = null;
	String myStateId = null;
	String myCityId = null;
	DbConnection dbC;

	public ThreadStreams(String idCountry, String idState, String idCity, DbConnection conn) {
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

		MLog.log("Starting Stream thread.. ");
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

		httppost = new HttpPost(MainActivity.API_STREAMS);
		jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"idCountry\": \"" + myCountryId + "\", \"idState\": \"" + myStateId + "\", \"idCity\": \"" + myCityId + "\"}]";	

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("params", jsonStr));
		MLog.log("Stream API=" + jsonStr);

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
		} catch (Exception e) {

		}

		if(responseString != null) {

			jsonStr = responseString;

			try {

				JSONObject jsonObj = new JSONObject(jsonStr);
				if(jsonObj.getString("result").equals("success")) {

					if(dbC.isOpen()) {
						dbC.isAvailale();
						dbC.clearDynamicRecords();
					}

					String valueStr = jsonObj.getString("value");
					JSONArray jsonArr = new JSONArray(valueStr);
					for(int i = 0; i < jsonArr.length(); i++) {

						jsonObj = jsonArr.getJSONObject(i);

						if(jsonObj.has("productstream")) {

							JSONObject jsonObjStream = jsonObj.getJSONObject("productstream");
							String idStreamContainer = jsonObjStream.getString("idProductstreamContainer");
							String nameStream = jsonObjStream.getString("name");
							MLog.log("Namestream " + nameStream);

							HashMap<String, String> map = new HashMap<String, String>();
							map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_STREAM);
							map.put(MainActivity.DB_COL_NAME, nameStream);
							map.put(MainActivity.DB_COL_SRV_ID, idStreamContainer);

							String _idStream = null;
							if(dbC.isOpen()) {
								dbC.isAvailale();
								dbC.insertRecord(map);
								_idStream = dbC.retrieveId(map);
							}

							JSONArray jsonArrProductItems = jsonObj.getJSONArray("productitems");
							for(int j = 0; j < jsonArrProductItems.length(); j++) {

								JSONObject jsonObjItems = jsonArrProductItems.getJSONObject(j).getJSONObject("items");
								JSONArray jsonArrPictures = jsonArrProductItems.getJSONObject(j).getJSONArray("pictures");
								JSONArray jsonArrUrls = jsonArrProductItems.getJSONObject(j).getJSONArray("urls");
								JSONArray jsonArrLocations = jsonArrProductItems.getJSONObject(j).getJSONArray("locations");
								JSONArray jsonArrContacts = jsonArrProductItems.getJSONObject(j).getJSONArray("contacts");
								JSONArray jsonArrAttachments = jsonArrProductItems.getJSONObject(j).getJSONArray("attachments");
								String priceMapped = jsonArrProductItems.getJSONObject(j).getString("price");
								String discountMapped = jsonArrProductItems.getJSONObject(j).getString("discount");

								String idSrvProductitems = jsonObjItems.getString("idProductitems");
								String title = jsonObjItems.getString("title").replace("'", "''");
								String subTitle = jsonObjItems.getString("subtitle").replace("'", "''");
								String content = jsonObjItems.getString("content").replace("'", "''");
								String timestamp = jsonObjItems.getString("timestampPublish");
								String stock = jsonObjItems.getString("stockCurrent");
								String size = jsonObjItems.getString("size");
								String weight = jsonObjItems.getString("weight");
								String sku = jsonObjItems.getString("sku");
								String price = jsonObjItems.getString("price");
								String extra1 = jsonObjItems.getString("extra1").replace("'", "''");
								String extra2 = jsonObjItems.getString("extra2").replace("'", "''");
								String extra3 = jsonObjItems.getString("extra3").replace("'", "''");
								String extra4 = jsonObjItems.getString("extra4").replace("'", "''");
								String extra5 = jsonObjItems.getString("extra5").replace("'", "''");
								String extra6 = jsonObjItems.getString("extra6").replace("'", "''");
								String extra7 = jsonObjItems.getString("extra7").replace("'", "''");
								String extra8 = jsonObjItems.getString("extra8").replace("'", "''");
								String extra9 = jsonObjItems.getString("extra9").replace("'", "''");
								String extra10 = jsonObjItems.getString("extra10").replace("'", "''");
								String booking = jsonObjItems.getString("bookingPrice");
								String discount = jsonObjItems.getString("Discounts_idDiscounts");

								if(!priceMapped.equals("-1")) {
									price = priceMapped;
								}

								if(!discountMapped.equals("-1")) {
									discount = discountMapped;
								}

								map = new HashMap<String, String>();
								map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_ITEM);
								map.put(MainActivity.DB_COL_TITLE, title);
								map.put(MainActivity.DB_COL_SRV_ID, idSrvProductitems);
								map.put(MainActivity.DB_COL_SUB, subTitle);
								map.put(MainActivity.DB_COL_CONTENT, content);
								map.put(MainActivity.DB_COL_TIMESTAMP, timestamp);
								map.put(MainActivity.DB_COL_STOCK, stock);
								map.put(MainActivity.DB_COL_SIZE, size);
								map.put(MainActivity.DB_COL_WEIGHT, weight);
								map.put(MainActivity.DB_COL_SKU, sku);
								map.put(MainActivity.DB_COL_PRICE, price);
								map.put(MainActivity.DB_COL_FOREIGN_KEY, _idStream);
								map.put(MainActivity.DB_COL_EXTRA_1, extra1);
								map.put(MainActivity.DB_COL_EXTRA_2, extra2);
								map.put(MainActivity.DB_COL_EXTRA_3, extra3);
								map.put(MainActivity.DB_COL_EXTRA_4, extra4);
								map.put(MainActivity.DB_COL_EXTRA_5, extra5);
								map.put(MainActivity.DB_COL_EXTRA_6, extra6);
								map.put(MainActivity.DB_COL_EXTRA_7, extra7);
								map.put(MainActivity.DB_COL_EXTRA_8, extra8);
								map.put(MainActivity.DB_COL_EXTRA_9, extra9);
								map.put(MainActivity.DB_COL_EXTRA_10, extra10);
								map.put(MainActivity.DB_COL_BOOKING, booking);
								map.put(MainActivity.DB_COL_DISCOUNT, discount);


								String _idItem = null;
								if(dbC.isOpen()) {
									dbC.isAvailale();
									dbC.insertRecord(map);

									map = new HashMap<String, String>();
									map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_ITEM);
									map.put(MainActivity.DB_COL_SRV_ID, idSrvProductitems);
									map.put(MainActivity.DB_COL_TIMESTAMP, timestamp);
									map.put(MainActivity.DB_COL_FOREIGN_KEY, _idStream);
									_idItem = dbC.retrieveId(map);
								}

								MLog.log("Title=" + title + ", Pictures=" + jsonArrPictures.length() + ",_id=" + _idItem);

								if(jsonArrPictures.length() > 0) {

									for(int k = 0; k < jsonArrPictures.length(); k++) {

										JSONObject jsonObjPicture = jsonArrPictures.getJSONObject(k);
										String pathOrig = jsonObjPicture.getString("pathOriginal");
										String pathProc = jsonObjPicture.getString("pathProcessed");
										String pathTh = jsonObjPicture.getString("pathThumbnail");

										String[] strArrOrig = pathOrig.split("/");
										String[] strArrProc = pathProc.split("/");
										String[] strArrTh = pathTh.split("/");

										map = new HashMap<String, String>();
										map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_PICTURE);
										map.put(MainActivity.DB_COL_PATH_ORIG, strArrOrig[strArrOrig.length - 1]);
										map.put(MainActivity.DB_COL_PATH_PROC, strArrProc[strArrProc.length - 1]);
										map.put(MainActivity.DB_COL_PATH_TH, strArrTh[strArrTh.length - 1]);
										map.put(MainActivity.DB_COL_FOREIGN_KEY, _idItem);

										if(dbC.isOpen()) {
											dbC.isAvailale();
											dbC.insertRecord(map);
										}

									}

								}

								if(jsonArrUrls.length() > 0) {

									for(int k = 0; k < jsonArrUrls.length(); k++) {

										JSONObject jsonObjUrl = jsonArrUrls.getJSONObject(k);
										String caption = jsonObjUrl.getString("caption");
										String value = jsonObjUrl.getString("value");

										map = new HashMap<String, String>();
										map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_URL);
										map.put(MainActivity.DB_COL_CAPTION, caption);
										map.put(MainActivity.DB_COL_URL, value);
										map.put(MainActivity.DB_COL_FOREIGN_KEY, _idItem);

										if(dbC.isOpen()) {
											dbC.isAvailale();
											dbC.insertRecord(map);
										}


									}

								}

								if(jsonArrAttachments.length() > 0) {

									for(int k = 0; k < jsonArrAttachments.length(); k++) {

										JSONObject jsonObjAttachment = jsonArrAttachments.getJSONObject(k);
										String caption = jsonObjAttachment.getString("caption");
										String value = jsonObjAttachment.getString("path");

										map = new HashMap<String, String>();
										map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_ATTACHMENT);
										map.put(MainActivity.DB_COL_CAPTION, caption);
										map.put(MainActivity.DB_COL_URL, value);
										map.put(MainActivity.DB_COL_FOREIGN_KEY, _idItem);

										if(dbC.isOpen()) {
											dbC.isAvailale();
											dbC.insertRecord(map);
										}


									}

								}

								if(jsonArrLocations.length() > 0) {

									for(int k = 0; k < jsonArrLocations.length(); k++) {

										JSONObject jsonObjLocation = jsonArrLocations.getJSONObject(k);
										String caption = jsonObjLocation.getString("caption");
										String location = jsonObjLocation.getString("location");

										map = new HashMap<String, String>();
										map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_LOCATION);
										map.put(MainActivity.DB_COL_CAPTION, caption);
										map.put(MainActivity.DB_COL_LOCATION, location);
										map.put(MainActivity.DB_COL_FOREIGN_KEY, _idItem);

										if(dbC.isOpen()) {
											dbC.isAvailale();
											dbC.insertRecord(map);
										}


									}

								}

								if(jsonArrContacts.length() > 0) {

									for(int k = 0; k < jsonArrContacts.length(); k++) {

										JSONObject jsonObjContact = jsonArrContacts.getJSONObject(k);
										String name = jsonObjContact.getString("name");
										String email = jsonObjContact.getString("email");
										String phone = jsonObjContact.getString("phone");

										map = new HashMap<String, String>();
										map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_CONTACT);
										map.put(MainActivity.DB_COL_NAME, name);
										map.put(MainActivity.DB_COL_EMAIL, email);
										map.put(MainActivity.DB_COL_PHONE, phone);
										map.put(MainActivity.DB_COL_FOREIGN_KEY, _idItem);

										if(dbC.isOpen()) {
											dbC.isAvailale();
											dbC.insertRecord(map);
										}


									}

								}

							}

						} else if(jsonObj.has("discounts")) {

							JSONArray jsonObjDiscountArr = jsonObj.getJSONArray("discounts");

							for(int j = 0; j < jsonObjDiscountArr.length(); j++) {

								JSONObject jsonObjDiscount = jsonObjDiscountArr.getJSONObject(j);

								String idDiscounts = jsonObjDiscount.getString("idDiscounts");
								String code = jsonObjDiscount.getString("code").replace("'", "''");
								String type = jsonObjDiscount.getString("type");
								String timestamp = jsonObjDiscount.getString("timestamp");
								String value = jsonObjDiscount.getString("value");

								HashMap<String, String> map = new HashMap<String, String>();
								map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_DISCOUNT);
								map.put(MainActivity.DB_COL_NAME, code);
								map.put(MainActivity.DB_COL_TITLE, type);
								map.put(MainActivity.DB_COL_TIMESTAMP, timestamp);
								map.put(MainActivity.DB_COL_PRICE, value);
								map.put(MainActivity.DB_COL_SRV_ID, idDiscounts);

								if(dbC.isOpen()) {
									dbC.isAvailale();
									dbC.insertRecord(map);
								}

							}



						} else if(jsonObj.has("coupons")) {

							JSONArray jsonObjCouponArr = jsonObj.getJSONArray("coupons");

							for(int j = 0; j < jsonObjCouponArr.length(); j++) {

								JSONObject jsonObjCoupon = jsonObjCouponArr.getJSONObject(j);

								String idCoupons = jsonObjCoupon.getString("idCoupons");
								String code = jsonObjCoupon.getString("code").replace("'", "''");
								String type = jsonObjCoupon.getString("type");
								String timestamp = jsonObjCoupon.getString("timestamp");
								String value = jsonObjCoupon.getString("value");
								String allowDouble = jsonObjCoupon.getString("allowDoubleDiscounting");
								String maxVal = jsonObjCoupon.getString("maxVal");
								String minPurchase = jsonObjCoupon.getString("minPurchase");

								HashMap<String, String> map = new HashMap<String, String>();
								map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_COUPON);
								map.put(MainActivity.DB_COL_NAME, code);
								map.put(MainActivity.DB_COL_TITLE, type);
								map.put(MainActivity.DB_COL_TIMESTAMP, timestamp);
								map.put(MainActivity.DB_COL_PRICE, value);
								map.put(MainActivity.DB_COL_CAPTION, allowDouble);
								map.put(MainActivity.DB_COL_SRV_ID, idCoupons);
								map.put(MainActivity.DB_COL_EXTRA_1, maxVal);
								map.put(MainActivity.DB_COL_EXTRA_2, minPurchase);

								MLog.log("Coupon Found inserting " + maxVal + " " + minPurchase);

								if(dbC.isOpen()) {
									dbC.isAvailale();
									dbC.insertRecord(map);
								}

							}


						} else if(jsonObj.has("taxes")) {


							try {

								JSONObject jsonObjCoupon = jsonObj.getJSONObject("taxes");
								String taxLabel1 = jsonObjCoupon.getString("taxLabel1").replace("'", "''");
								String taxValue1 = jsonObjCoupon.getString("taxValue1");
								String taxLabel2 = jsonObjCoupon.getString("taxLabel2").replace("'", "''");
								String taxValue2 = jsonObjCoupon.getString("taxValue2");

								if(taxValue1.length() > 0 && taxLabel1.length() > 0) {

									if(!taxLabel1.equals("null") && !taxValue1.equals("null")) {

										double d = Double.parseDouble(taxValue1);

										HashMap<String, String> map = new HashMap<String, String>();
										map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_COUPON);
										map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_TAX_1);
										map.put(MainActivity.DB_COL_TITLE, taxLabel1);
										map.put(MainActivity.DB_COL_SUB, taxValue1);

										if(dbC.isOpen()) {
											dbC.isAvailale();
											dbC.insertRecord(map);
										}

									}

								}

								if(taxValue2.length() > 0 && taxLabel2.length() > 0) {

									if(!taxLabel2.equals("null") && !taxValue2.equals("null")) {

										double d = Double.parseDouble(taxValue1);

										HashMap<String, String> map = new HashMap<String, String>();
										map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_COUPON);
										map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_TAX_2);
										map.put(MainActivity.DB_COL_TITLE, taxLabel2);
										map.put(MainActivity.DB_COL_SUB, taxValue2);

										if(dbC.isOpen()) {
											dbC.isAvailale();
											dbC.insertRecord(map);
										}

									}

								}

							} catch (NumberFormatException e) {

								e.printStackTrace();

							}

						}

					}

					if(dbC.isOpen()) {
						dbC.isAvailale();
						dbC.printRecords();
					}

				}



			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}
