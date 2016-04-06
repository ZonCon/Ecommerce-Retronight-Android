package com.megotechnologies.ecommerce_retronight.payment;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.megotechnologies.ecommerce_retronight.FragmentMeta;
import com.megotechnologies.ecommerce_retronight.shop.FragmentShopItemsList;
import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.R;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCFragmentLifecycle;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCRunnable;
import com.megotechnologies.ecommerce_retronight.account.FragmentAccountLogin;
import com.megotechnologies.ecommerce_retronight.dataobjects.CouponRecord;
import com.megotechnologies.ecommerce_retronight.dataobjects.DiscountRecord;
import com.megotechnologies.ecommerce_retronight.dataobjects.PGRecord;
import com.megotechnologies.ecommerce_retronight.dataobjects.TaxRecord;
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

public class FragmentPaymentConfirm extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	LinearLayout llCrumbs;
	int IV_ID_PREFIX = 4000, TV_ID_PREFIX = 6000, SPIN_ID_PREFIX = 8000;
	ArrayList<String> itemTitleList, itemQList, itemPictList, itemCostList, itemIdList, itemIdDiscount, itemBooking;
	public PGRecord pgRecord;
	//public String name, email, phone, address, country, state, city, pincode;
	TextView tvTitle, tvSub, tvCrumbsCart, tvCrumbsCheckout, tvCrumbsConfirm, tvCrumbsPayment;
	Thread thCartDownload, thOrderUpload;
	LinearLayout llContainer;
	Thread thLoginChecker;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		v =  inflater.inflate(R.layout.fragment_payment_confirm, container, false);

		if(activity.IS_CONNECTED) {
			storeClassVariables();
			initUIHandles();
			initUIListeners();
			formatUI();
			loadCart();
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
			alert.create().show();
		}

		return v;
	}

	void uploadOrder() {

		thOrderUpload = new Thread(this);
		thOrderUpload.setName(MainActivity.TH_NAME_ORDER);
		thOrderUpload.start();

	}

	void loadCart() {

		thCartDownload = new Thread(this);
		thCartDownload.setName(MainActivity.TH_NAME_CART);
		thCartDownload.start();

	}

	@Override
	public void storeClassVariables() {
		// TODO Auto-generated method stub
		itemTitleList = new ArrayList<String>();
		itemQList = new ArrayList<String>();
		itemCostList = new ArrayList<String>();
		itemPictList = new ArrayList<String>();
		itemIdList = new ArrayList<String>();
		itemIdDiscount = new ArrayList<String>();
		itemBooking = new ArrayList<String>();
	}

	@Override
	public void initUIHandles() {
		// TODO Auto-generated method stub
		llContainer = (LinearLayout)v.findViewById(R.id.ll_container);
		tvTitle = (TextView)v.findViewById(R.id.tv_label_title);
		tvSub = (TextView)v.findViewById(R.id.tv_label_subtitle);

		llCrumbs = (LinearLayout)v.findViewById(R.id.ll_crumbs);
		tvCrumbsCart = (TextView)v.findViewById(R.id.tv_crumbs_cart);
		tvCrumbsCheckout = (TextView)v.findViewById(R.id.tv_crumbs_checkout);
		tvCrumbsConfirm = (TextView)v.findViewById(R.id.tv_crumbs_confirm);
		tvCrumbsPayment = (TextView)v.findViewById(R.id.tv_crumbs_payment);
	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub


	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub
		tvCrumbsCart.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) ((MainActivity.TEXT_SIZE_TILE / 3) * 2.5));
		tvCrumbsCheckout.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) ((MainActivity.TEXT_SIZE_TILE / 3) * 2.5));
		tvCrumbsConfirm.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) ((MainActivity.TEXT_SIZE_TILE / 3) * 2.5));
		tvCrumbsPayment.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) ((MainActivity.TEXT_SIZE_TILE / 3) * 2.5));

		tvTitle.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
	}

	protected Handler displayHandler = new Handler() {

		public void handleMessage(Message msg) {


			if(msg.what == 4) {

				uploadOrder();
				return;
			}


			if(itemTitleList.size() > 0) {

				llContainer.removeAllViews();

				double total = 0;
				Boolean containsDiscount = false;
				Boolean containsBooking = false;

				LinearLayout llLine = new LinearLayout(activity.context);
				llLine.setOrientation(LinearLayout.HORIZONTAL);
				LinearLayout.LayoutParams paramsLLLine = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 2);
				paramsLLLine.setMargins(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
				llLine.setLayoutParams(paramsLLLine);
				llLine.setBackgroundColor(getResources().getColor(R.color.line_separator));

				llContainer.addView(llLine);

				TextView tvCartTitle = new TextView(activity.context);
				tvCartTitle.setText("Cart Items");
				LinearLayout.LayoutParams paramsLLCartText = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				tvCartTitle.setLayoutParams(paramsLLCartText);
				tvCartTitle.setTextColor(getResources().getColor(R.color.text_color));
				tvCartTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TITLE);
				tvCartTitle.setEllipsize(TruncateAt.END);
				tvCartTitle.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
				tvCartTitle.setSingleLine();
				tvCartTitle.setGravity(Gravity.CENTER);

				llContainer.addView(tvCartTitle);

				for(int i = 0; i < itemTitleList.size(); i++) {

					LinearLayout llRow = new LinearLayout(activity.context);
					llRow.setOrientation(LinearLayout.HORIZONTAL);
					LinearLayout.LayoutParams paramsLL = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					paramsLL.topMargin = MainActivity.SPACING;
					paramsLL.leftMargin = MainActivity.SPACING;
					paramsLL.rightMargin = MainActivity.SPACING;
					llRow.setLayoutParams(paramsLL);

					llContainer.addView(llRow);

					// Insert the middle linear layout

					int textContSize = (MainActivity.SCREEN_WIDTH*3)/5;
					LinearLayout llMid = new LinearLayout(activity.context);
					llMid.setOrientation(LinearLayout.VERTICAL);
					LinearLayout.LayoutParams paramsLLMid = new LayoutParams(textContSize, LinearLayout.LayoutParams.WRAP_CONTENT);
					paramsLLMid.leftMargin = (MainActivity.SPACING);
					paramsLLMid.rightMargin = (MainActivity.SPACING/2);
					llMid.setLayoutParams(paramsLLMid);
					llMid.setPadding(0, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);

					// Insert title

					TextView tvMidTitle = new TextView(activity.context);
					tvMidTitle.setText(itemTitleList.get(i));
					LinearLayout.LayoutParams paramsLLMidText = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					tvMidTitle.setLayoutParams(paramsLLMidText);
					tvMidTitle.setTextColor(getResources().getColor(R.color.text_color));
					tvMidTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TILE);
					tvMidTitle.setEllipsize(TruncateAt.END);
					tvMidTitle.setSingleLine();

					DiscountRecord dr = activity.getDiscountRecordFromId(itemIdDiscount.get(i));
					String discountedPrice = itemCostList.get(i);
					TextView tvOrigPrice = null;
					String pricePerItemStr = String.format("%.2f", (Double.parseDouble(itemCostList.get(i)) / Double.parseDouble(itemQList.get(i))));
					if(dr!=null) {

						containsDiscount = true;
						String discountedPricePerItem = "";
						String totalOriginalPrice = itemCostList.get(i);;

						if(dr.type.contains(MainActivity.DB_DISCOUNT_TYPE_FLAT)) {

							discountedPricePerItem = String.format("%.2f", ((Double.parseDouble(pricePerItemStr)) - (Double.parseDouble(dr.value))));

						} else {

							discountedPricePerItem = String.format("%.2f", ((Double.parseDouble(pricePerItemStr)) - (Double.parseDouble(dr.value)*Double.parseDouble(pricePerItemStr))/100));

						}

						discountedPrice = String.format("%.2f", (Double.parseDouble(discountedPricePerItem) * Double.parseDouble(itemQList.get(i))));

						tvOrigPrice = new TextView(activity.context);
						tvOrigPrice.setText(String.format("%.2f", Double.parseDouble(totalOriginalPrice)) + " " + MainActivity.SYMBOL_RUPEE);
						paramsLLMidText = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
						tvOrigPrice.setLayoutParams(paramsLLMidText);
						tvOrigPrice.setTextColor(getResources().getColor(R.color.text_color));
						tvOrigPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
						tvOrigPrice.setPaintFlags(tvOrigPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
						tvOrigPrice.setEllipsize(TruncateAt.END);
						tvOrigPrice.setSingleLine();
					}

					// Insert price

					if(itemBooking.get(i) != null) {

						String booking = itemBooking.get(i);
						if(itemBooking.get(i).length() > 0 && !itemBooking.get(i).equals("null")) {
							containsBooking = true;
							total += Double.parseDouble(booking) * Double.parseDouble(itemQList.get(i));
						} else {
							total += Double.parseDouble(discountedPrice);		
						}

					} else {
						total += Double.parseDouble(discountedPrice);
					}

					TextView tvMidPrice = new TextView(activity.context);
					tvMidPrice.setText(String.format("%.2f", Double.parseDouble(itemCostList.get(i))) + " " + MainActivity.SYMBOL_RUPEE);
					tvMidPrice.setLayoutParams(paramsLLMidText);
					tvMidPrice.setTextColor(getResources().getColor(R.color.text_color));
					tvMidPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
					tvMidPrice.setEllipsize(TruncateAt.END);
					tvMidPrice.setSingleLine();

					TextView tvBooking = null;
					if(itemBooking.get(i) != null) {

						String booking = itemBooking.get(i);

						if(itemBooking.get(i).length() > 0 && !itemBooking.get(i).equals("null")) {
							tvBooking = new TextView(activity.context);
							tvBooking.setText("Booking price: " + String.format("%.2f", Double.parseDouble(booking)) + " " + MainActivity.SYMBOL_RUPEE);
							LayoutParams paramsLLRightText = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
							tvBooking.setLayoutParams(paramsLLRightText);
							tvBooking.setTextColor(getResources().getColor(R.color.text_color));
							tvBooking.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

						}

					}

					llRow.addView(llMid);
					llMid.addView(tvMidTitle);
					if(dr != null) {
						llMid.addView(tvOrigPrice);
					}
					llMid.addView(tvMidPrice);
					if(itemBooking.get(i) != null) {
						if(itemBooking.get(i).length() > 0 && !itemBooking.get(i).equals("null")) {
							llMid.addView(tvBooking);
						}
					}
					// Insert the right linear layout

					int textQSize = (MainActivity.SCREEN_WIDTH*2)/5;
					LinearLayout llRight = new LinearLayout(activity.context);
					llRight.setOrientation(LinearLayout.VERTICAL);
					LinearLayout.LayoutParams paramsLLRight = new LayoutParams(textQSize, LinearLayout.LayoutParams.WRAP_CONTENT);
					paramsLLRight.leftMargin = (MainActivity.SPACING);
					paramsLLRight.rightMargin = (MainActivity.SPACING/2);
					llRight.setLayoutParams(paramsLLRight);
					llRight.setPadding(0, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);

					// Insert Quantity Label and Spinner

					TextView tvMidQ = new TextView(activity.context);
					LinearLayout.LayoutParams paramsLLRightText = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					tvMidQ.setText("Quantity");
					tvMidQ.setLayoutParams(paramsLLRightText);
					tvMidQ.setTextColor(getResources().getColor(R.color.text_color));
					tvMidQ.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
					tvMidQ.setEllipsize(TruncateAt.END);
					tvMidQ.setSingleLine();

					TextView tvRightQ = new TextView(activity.context);
					paramsLLRightText = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					tvRightQ.setText(itemQList.get(i));
					tvRightQ.setLayoutParams(paramsLLRightText);
					tvRightQ.setTextColor(getResources().getColor(R.color.text_color));
					tvRightQ.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
					tvRightQ.setEllipsize(TruncateAt.END);
					tvRightQ.setSingleLine();

					llRow.addView(llRight);
					llRight.addView(tvMidQ);
					llRight.addView(tvRightQ);

				}

				llLine = new LinearLayout(activity.context);
				llLine.setOrientation(LinearLayout.HORIZONTAL);
				paramsLLLine = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 2);
				paramsLLLine.setMargins(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
				llLine.setLayoutParams(paramsLLLine);
				llLine.setBackgroundColor(getResources().getColor(R.color.line_separator));

				llContainer.addView(llLine);


				final CouponRecord cr = activity.getCouponAppliedToCart();
				String couponCode = null;
				if(cr != null) {
					couponCode = cr.code;
				}

				String discountedOrigPrice = String.format("%.2f", (total));
				String discountedPrice = String.format("%.2f", (total));
				TextView tvOrigTotal = null;

				if(cr != null) {

					if(cr.type.contains(MainActivity.DB_DISCOUNT_TYPE_FLAT)) {

						discountedPrice = String.format("%.2f", ((Double.parseDouble(discountedPrice)) - (Double.parseDouble(cr.value))));

					} else {

						double discountDifference = (Double.parseDouble(cr.value)*Double.parseDouble(discountedPrice))/100;

						if(cr.maxVal != null) {

							if(cr.maxVal.length() > 0) {

								if(Double.parseDouble(cr.maxVal) < discountDifference) {

									discountedPrice = String.format("%.2f", ((Double.parseDouble(discountedPrice)) - Double.parseDouble(cr.maxVal)));

								} else {

									discountedPrice = String.format("%.2f", ((Double.parseDouble(discountedPrice)) - discountDifference));

								}

							} else {

								discountedPrice = String.format("%.2f", ((Double.parseDouble(discountedPrice)) - discountDifference));

							}

						} else {

							discountedPrice = String.format("%.2f", ((Double.parseDouble(discountedPrice)) - discountDifference));

						}

					}

					tvOrigTotal = new TextView(activity.context);
					tvOrigTotal.setText("Total: " + String.format("%.2f", Double.parseDouble(discountedOrigPrice)) + " " + MainActivity.SYMBOL_RUPEE);
					LinearLayout.LayoutParams tvOrigTotalParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
					tvOrigTotalParams.setMargins(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
					tvOrigTotal.setLayoutParams(tvOrigTotalParams);
					tvOrigTotal.setTextColor(getResources().getColor(R.color.text_color));
					tvOrigTotal.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
					tvOrigTotal.setPaintFlags(tvOrigTotal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
					tvOrigTotal.setEllipsize(TruncateAt.END);
					tvOrigTotal.setGravity(Gravity.RIGHT);
					tvOrigTotal.setSingleLine();
					llContainer.addView(tvOrigTotal);
					
					TextView tvDiscount = new TextView(activity.context);
					if(cr.type.contains(MainActivity.DB_DISCOUNT_TYPE_FLAT)) {
						tvDiscount.setText("(INR " + cr.value + " off)");	
					} else {
						if(cr.maxVal != null) {

							if(cr.maxVal.length() > 0) {
								tvDiscount.setText("(" + cr.value + "% off upto INR " + cr.maxVal + ")");
							} else {
								tvDiscount.setText("(" + cr.value + "% off)");
							}

						} else {
							tvDiscount.setText("(" + cr.value + "% off)");	
						}

					}
					tvOrigTotalParams = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					tvOrigTotalParams.setMargins(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
					tvDiscount.setLayoutParams(tvOrigTotalParams);
					tvDiscount.setTextColor(getResources().getColor(R.color.text_color));
					tvDiscount.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
					tvDiscount.setEllipsize(TruncateAt.END);
					tvDiscount.setGravity(Gravity.RIGHT);
					tvDiscount.setSingleLine();
					llContainer.addView(tvDiscount);
				}


				TextView tvTotal = new TextView(activity.context);
				LinearLayout.LayoutParams tvTotalParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				tvTotalParams.setMargins(MainActivity.SPACING / 2, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
				tvTotal.setLayoutParams(tvTotalParams);
				tvTotal.setTextColor(getResources().getColor(R.color.text_color));
				tvTotal.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
				tvTotal.setText("Total: " + String.format("%.2f", Double.parseDouble(discountedPrice)) + " " + MainActivity.SYMBOL_RUPEE);
				tvTotal.setGravity(Gravity.RIGHT);
				tvTotal.setPadding(MainActivity.SPACING / 2, MainActivity.SPACING, MainActivity.SPACING / 2, MainActivity.SPACING);

				llContainer.addView(tvTotal);

				if(activity.getTax1() != null) {

					double taxedPrice = 0;

					if(activity.getTax2() != null) {

						TaxRecord taxRecord1 = activity.getTax1();
						taxedPrice = Double.parseDouble(discountedPrice) + (Double.parseDouble(taxRecord1.value)*Double.parseDouble(discountedPrice))/100;

						TextView tvTax1 = new TextView(activity.context);
						LinearLayout.LayoutParams tvTax1Params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
						tvTax1Params.setMargins(MainActivity.SPACING/2, 0, MainActivity.SPACING, 0);
						tvTax1.setLayoutParams(tvTax1Params);
						tvTax1.setTextColor(getResources().getColor(R.color.text_color));
						tvTax1.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
						tvTax1.setText(taxRecord1.label + ": " + taxRecord1.value + "%");
						tvTax1.setGravity(Gravity.RIGHT);
						tvTax1.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
						llContainer.addView(tvTax1);

						TaxRecord taxRecord2 = activity.getTax2();
						taxedPrice = (taxedPrice) + (Double.parseDouble(taxRecord2.value)*(taxedPrice))/100;

						TextView tvTax2 = new TextView(activity.context);
						LinearLayout.LayoutParams tvTax2Params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
						tvTax2Params.setMargins(MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING, 0);
						tvTax2.setLayoutParams(tvTax2Params);
						tvTax2.setTextColor(getResources().getColor(R.color.text_color));
						tvTax2.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
						tvTax2.setText(taxRecord2.label + ": " + taxRecord2.value + "%");
						tvTax2.setGravity(Gravity.RIGHT);
						tvTax2.setPadding(MainActivity.SPACING, MainActivity.SPACING/2, MainActivity.SPACING, 0);
						llContainer.addView(tvTax2);

						TextView tvTaxedTotal = new TextView(activity.context);
						LinearLayout.LayoutParams tvTaxedTotalParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
						tvTaxedTotalParams.setMargins(MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING, MainActivity.SPACING);
						tvTaxedTotal.setLayoutParams(tvTotalParams);
						tvTaxedTotal.setTextColor(getResources().getColor(R.color.text_color));
						tvTaxedTotal.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
						tvTaxedTotal.setText("Net Total: " + String.format("%.2f", taxedPrice) + " " + MainActivity.SYMBOL_RUPEE);
						tvTaxedTotal.setGravity(Gravity.RIGHT);
						tvTaxedTotal.setPadding(MainActivity.SPACING, MainActivity.SPACING/2, MainActivity.SPACING, MainActivity.SPACING);
						llContainer.addView(tvTaxedTotal);

					} else {

						TaxRecord taxRecord1 = activity.getTax1();
						taxedPrice = Double.parseDouble(discountedPrice) + (Double.parseDouble(taxRecord1.value)*Double.parseDouble(discountedPrice))/100;

						TextView tvTax1 = new TextView(activity.context);
						LinearLayout.LayoutParams tvTax1Params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
						tvTax1Params.setMargins(MainActivity.SPACING/2, 0, MainActivity.SPACING, 0);
						tvTax1.setLayoutParams(tvTotalParams);
						tvTax1.setTextColor(getResources().getColor(R.color.text_color));
						tvTax1.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
						tvTax1.setText(taxRecord1.label + ": " + taxRecord1.value + "%");
						tvTax1.setGravity(Gravity.RIGHT);
						tvTax1.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
						llContainer.addView(tvTax1);

						TextView tvTaxedTotal = new TextView(activity.context);
						LinearLayout.LayoutParams tvTaxedTotalParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
						tvTaxedTotalParams.setMargins(MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING, MainActivity.SPACING);
						tvTaxedTotal.setLayoutParams(tvTotalParams);
						tvTaxedTotal.setTextColor(getResources().getColor(R.color.text_color));
						tvTaxedTotal.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
						tvTaxedTotal.setText("Net Total: " + String.format("%.2f", taxedPrice) + " " + MainActivity.SYMBOL_RUPEE);
						tvTaxedTotal.setGravity(Gravity.RIGHT);
						tvTaxedTotal.setPadding(MainActivity.SPACING, MainActivity.SPACING/2, MainActivity.SPACING, MainActivity.SPACING);
						llContainer.addView(tvTaxedTotal);

					}


				}

				llLine = new LinearLayout(activity.context);
				llLine.setOrientation(LinearLayout.HORIZONTAL);
				paramsLLLine = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 1);
				paramsLLLine.setMargins(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
				llLine.setLayoutParams(paramsLLLine);
				llLine.setBackgroundColor(getResources().getColor(R.color.line_separator));

				llContainer.addView(llLine);

				TextView tvShippingTitle = new TextView(activity.context);
				tvShippingTitle.setText("Shipping Details");
				LinearLayout.LayoutParams paramsLLShippingText = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				tvShippingTitle.setLayoutParams(paramsLLShippingText);
				tvShippingTitle.setTextColor(getResources().getColor(R.color.text_color));
				tvShippingTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TITLE);
				tvShippingTitle.setEllipsize(TruncateAt.END);
				tvShippingTitle.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
				tvShippingTitle.setSingleLine();
				tvShippingTitle.setGravity(Gravity.CENTER);

				llContainer.addView(tvShippingTitle);

				TextView tvShippingDetails = new TextView(activity.context);
				tvShippingDetails.setText(pgRecord.billingName + "\n" +
								pgRecord.billingEmail + "\n" +
								pgRecord.billingTel + "\n" +
								pgRecord.billingAddress + "\n" +
								pgRecord.billingCountry + "\n" +
								pgRecord.billingState + "\n" +
								pgRecord.billingCity + "\n" +
								pgRecord.billingZip + "\n"
				);
				tvShippingDetails.setLayoutParams(paramsLLShippingText);
				tvShippingDetails.setTextColor(getResources().getColor(R.color.text_color));
				tvShippingDetails.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TILE);
				tvShippingDetails.setLineSpacing(0.0f, 1.3f);
				tvShippingDetails.setEllipsize(TruncateAt.END);
				tvShippingDetails.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);

				llContainer.addView(tvShippingDetails);

				TextView tvTerms = new TextView(activity.context);
				tvTerms.setText("By tapping on 'Proceed to Payment' you are agreeing to our terms and conditions");
				LinearLayout.LayoutParams paramsLLTermsText = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				tvTerms.setLayoutParams(paramsLLTermsText);
				tvTerms.setTextColor(getResources().getColor(R.color.light_blue));
				tvTerms.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TITLE);
				tvTerms.setEllipsize(TruncateAt.END);
				tvTerms.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
				tvTerms.setGravity(Gravity.LEFT);
				tvTerms.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						// TODO Auto-generated method stub

						HashMap<String, String> map = new HashMap<String, String>();
						map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_STREAM);
						ArrayList<HashMap<String, String>> records =  null;
						if(dbC.isOpen()) {
							dbC.isAvailale();
							records =  dbC.retrieveRecords(map);
						}

						if(records.size() >= MainActivity.NUM_INIT_STREAMS) {

							map = records.get(1);

							FragmentShopItemsList fragment = new FragmentShopItemsList();
							fragment.idStream = Integer.parseInt(map.get(MainActivity.DB_COL_SRV_ID));
							activity.fragMgr.beginTransaction()
									.add(R.id.ll_body, fragment, MainActivity.SCREEN_SHOP_ITEM_LIST)
									.addToBackStack(MainActivity.SCREEN_SHOP_ITEM_LIST)
									.commit();

						}

					}
				});

				llContainer.addView(tvTerms);

				LinearLayout llButtons = new LinearLayout(activity.context);
				llButtons.setOrientation(LinearLayout.VERTICAL);
				llButtons.setGravity(Gravity.CENTER);
				LinearLayout.LayoutParams llButtonsParams = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				llButtonsParams.gravity = Gravity.CENTER;
				llButtons.setLayoutParams(llButtonsParams);

				TextView tvNext = new TextView(activity.context);
				LinearLayout.LayoutParams tvNextParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				tvNextParams.setMargins(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
				tvNext.setLayoutParams(tvNextParams);
				//tvAdd.setBackgroundColor(0xFFFFFFFF);
				//tvNext.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.dark_shadow_background));
				tvNext.setTextColor(getResources().getColor(R.color.white));
				tvNext.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
				tvNext.setText("PROCEED TO PAYMENT");
				tvNext.setGravity(Gravity.CENTER);
				tvNext.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_shadow_background));
				tvNext.setPadding(MainActivity.SPACING, MainActivity.SPACING / 2, MainActivity.SPACING, MainActivity.SPACING / 2);
				tvNext.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						thLoginChecker = new Thread(FragmentPaymentConfirm.this);
						thLoginChecker.setName(MainActivity.TH_NAME_VERIFY_LOGIN);
						thLoginChecker.start();

					}

				});

				TextView tvPrev = new TextView(activity.context);
				LinearLayout.LayoutParams tvPrevParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				tvPrevParams.setMargins(MainActivity.SPACING, 0, MainActivity.SPACING, MainActivity.SPACING);
				tvPrev.setLayoutParams(tvPrevParams);
				//tvAdd.setBackgroundColor(0xFFFFFFFF);
				//tvNext.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.dark_shadow_background));
				tvPrev.setTextColor(getResources().getColor(R.color.black));
				tvPrev.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
				tvPrev.setText("BACK TO CHECKOUT");
				tvPrev.setGravity(Gravity.CENTER);
				tvPrev.setBackgroundDrawable(getResources().getDrawable(R.drawable.yellow_shadow_background));
				tvPrev.setPadding(MainActivity.SPACING, MainActivity.SPACING / 2, MainActivity.SPACING, MainActivity.SPACING / 2);
				tvPrev.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						activity.onBackPressed();

					}

				});

				llContainer.addView(llButtons);
				llButtons.addView(tvNext);
				llButtons.addView(tvPrev);
			}

		};

	};

	void loadLogin() {

		FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
		FragmentAccountLogin fragment = new FragmentAccountLogin();
		//fragment.isBegin = true;
		fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_LOGIN_ACCOUNT)
		.addToBackStack(MainActivity.SCREEN_LOGIN_ACCOUNT)
		.commit();

		activity.getFragmentManager().beginTransaction().remove(FragmentPaymentConfirm.this).commit();

	}

	protected Handler threadHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			String jsonStr = (String)msg.obj;
			if(jsonStr != null && jsonStr.length() > 0) {

				MLog.log(jsonStr);
				JSONObject jsonObj;

				switch (msg.what) {

				case 1:

					try {

						jsonObj = new JSONObject(jsonStr);
						if(jsonObj.getString("result").equals("success")) {

							// Get the cart handle

							HashMap<String, String> map = new HashMap<String, String>();
							map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_CART);
							map.put(MainActivity.DB_COL_CART_CART_ISOPEN, MainActivity.DB_RECORD_VALUE_CART_OPEN);
							String _idOpenCart = null;
							if(dbC.isOpen()) {
								dbC.isAvailale();
								_idOpenCart = dbC.retrieveId(map);
							}

							// Get the card items

							map = new HashMap<String, String>();
							map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_CART_ITEM);
							map.put(MainActivity.DB_COL_FOREIGN_KEY, _idOpenCart);
							ArrayList<HashMap<String, String>> recordsItems = null;
							if(dbC.isOpen()) {
								dbC.isAvailale();
								recordsItems = dbC.retrieveRecords(map);
							}

							// Clear all the storage arrays

							itemTitleList.clear();
							itemQList.clear();
							itemCostList.clear();
							itemPictList.clear();
							itemIdList.clear();
							itemIdDiscount.clear();
							itemBooking.clear();

							// Loop through the items array which has been downloaded via the API

							String valueStr = jsonObj.getString("value");
							JSONArray jsonArr = new JSONArray(valueStr);
							for(int i = 0; i < jsonArr.length(); i++) {

								// Parse the information downloaded and extract it into the storage array

								jsonObj = jsonArr.getJSONObject(i);
								JSONArray jsonArrInfo = jsonObj.getJSONArray("info");
								JSONArray jsonArrPictures = jsonObj.getJSONArray("pictures");
								String priceMapped = jsonObj.getString("price");

								if(jsonArrInfo.length() > 0) {

									JSONObject jsonObjItem = jsonArrInfo.getJSONObject(0);
									String title = jsonObjItem.getString("title");
									String priceItem = jsonObjItem.getString("price");
									String price;
									String booking = jsonObjItem.getString("bookingPrice");
									if(priceMapped.equals("-1")) {
										price = priceItem;
									} else {
										price = priceMapped;
									}

									map = recordsItems.get(i);
									int q = Integer.parseInt(map.get(MainActivity.DB_COL_CART_ITEM_QUANTITY));
									double cost = Double.parseDouble(price) * q;

									MLog.log("Title=" + title + ", Q=" + q + ", Cost=" + cost);
									itemTitleList.add(title);
									itemQList.add(String.valueOf(q));
									itemCostList.add(String.valueOf(cost));
									itemIdList.add(map.get(MainActivity.DB_COL_ID));
									itemIdDiscount.add(map.get(MainActivity.DB_COL_DISCOUNT));
									itemBooking.add(booking);

									if(jsonArrPictures.length() > 0) {

										JSONObject jsonObjPict = jsonArrPictures.getJSONObject(0);
										String pathPicture = jsonObjPict.getString("pathThumbnail");
										itemPictList.add(pathPicture);

									} else {

										itemPictList.add("");

									}

								}


							}

							// Send an empty message to the display handler indicating that the download and extraction has finished
							// and we are now ready to display the cart items

							displayHandler.sendEmptyMessage(0);

						} else {

							String reason = jsonObj.getString("reason");
							if(reason.contains("Failed")) {

								loadLogin();

							}

						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


					break;

				case 2:

					try {

						jsonObj = new JSONObject(jsonStr);
						if(jsonObj.getString("result").equals("success")) {

							float total = 0;
							for(int i = 0; i < itemTitleList.size(); i++) {

								DiscountRecord dr = activity.getDiscountRecordFromId(itemIdDiscount.get(i));
								String discountedPrice = itemCostList.get(i);
								String pricePerItemStr = String.valueOf(Double.parseDouble(itemCostList.get(i)) / Double.parseDouble(itemQList.get(i)));
								if(dr != null) {


									String discountedPricePerItem = "";
									String totalOriginalPrice = itemCostList.get(i);;

									if(dr.type.contains(MainActivity.DB_DISCOUNT_TYPE_FLAT)) {

										discountedPricePerItem = String.valueOf((Float.parseFloat(pricePerItemStr)) - (Float.parseFloat(dr.value)));

									} else {

										discountedPricePerItem = String.valueOf((Float.parseFloat(pricePerItemStr)) - (Float.parseFloat(dr.value)*Float.parseFloat(pricePerItemStr))/100);

									}

									discountedPrice = String.valueOf(Float.parseFloat(discountedPricePerItem) * Float.parseFloat(itemQList.get(i)));

								}

								if(itemBooking.get(i) != null) {

									String booking = itemBooking.get(i);
									if(itemBooking.get(i).length() > 0 && !itemBooking.get(i).equals("null")) {
										total += Double.parseDouble(booking) * Double.parseDouble(itemQList.get(i));
									} else {
										total += Double.parseDouble(discountedPrice);		
									}

								} else {
									total += Double.parseDouble(discountedPrice);
								}

								//total += Float.parseFloat(discountedPrice);

							}

							final CouponRecord cr = activity.getCouponAppliedToCart();
							String couponCode = null;
							if(cr != null) {
								couponCode = cr.code;
							}
							
							String discountedOrigPrice = String.valueOf(total);
							String discountedPrice = String.valueOf(total);
							TextView tvOrigTotal = null;

							if(cr != null) {

								if(cr.type.contains(MainActivity.DB_DISCOUNT_TYPE_FLAT)) {

									discountedPrice = String.valueOf((Float.parseFloat(discountedPrice)) - (Float.parseFloat(cr.value)));

								} else {

									float discountDifference = (Float.parseFloat(cr.value)*Float.parseFloat(discountedPrice))/100;

									if(cr.maxVal != null) {

										if(cr.maxVal.length() > 0) {

											if(Float.parseFloat(cr.maxVal) < discountDifference) {

												discountedPrice = String.valueOf((Float.parseFloat(discountedPrice)) - Float.parseFloat(cr.maxVal));

											} else {

												discountedPrice = String.valueOf((Float.parseFloat(discountedPrice)) - discountDifference);

											}

										} else {

											discountedPrice = String.valueOf((Float.parseFloat(discountedPrice)) - discountDifference);

										}

									} else {

										discountedPrice = String.valueOf((Float.parseFloat(discountedPrice)) - discountDifference);

									}

								}

							}

							total = Float.parseFloat(discountedPrice);
							MLog.log("Discounted Price = " + total);
							
							if(activity.getTax1() != null) {

								double taxedPrice = 0;

								if(activity.getTax2() != null) {

									TaxRecord taxRecord1 = activity.getTax1();
									taxedPrice = Double.parseDouble(discountedPrice) + (Double.parseDouble(taxRecord1.value)*Double.parseDouble(discountedPrice))/100;
									
									MLog.log("Taxed Price 1 = " + taxedPrice);

									TaxRecord taxRecord2 = activity.getTax2();
									taxedPrice = (taxedPrice) + (Double.parseDouble(taxRecord2.value)*(taxedPrice))/100;

									total = Float.parseFloat(taxedPrice + "");
									MLog.log("Taxed Price 2 = " + taxedPrice);


								} else {

									TaxRecord taxRecord1 = activity.getTax1();
									taxedPrice = Double.parseDouble(discountedPrice) + (Double.parseDouble(taxRecord1.value)*Double.parseDouble(discountedPrice))/100;

									total = Float.parseFloat(taxedPrice + "");
									MLog.log("Taxed Price 1 = " + taxedPrice);


								}


							}
							
							MLog.log("Taxed Price = " + total);

							FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
							FragmentPaymentGateway fragment = new FragmentPaymentGateway();
							pgRecord.idOrder = jsonObj.getInt("value");
							long time = System.currentTimeMillis();
							pgRecord.idTransaction = time + activity.getEmail().length();
							pgRecord.amount = total;
							fragment.pgRecord = pgRecord;
							fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_PAYMENT)
							.addToBackStack(MainActivity.SCREEN_PAYMENT)
							.commit();
							 

						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


					break;

				default:
					break;
				}


			}

		}

	};

	@Override
	public void run() {
		// TODO Auto-generated method stub

		activity.handlerLoading.sendEmptyMessage(1);

		Looper.prepare();

		Thread t = Thread.currentThread();
		String tName = t.getName();

		// Check if current thread is cart download thread or picture download thread

		if(tName.equals(MainActivity.TH_NAME_CART)) {

			if(activity.myCity != null) {

				String myCountryId = activity.myCountryId;
				String myStateId = activity.myStateId;
				String myCityId = activity.myCityId;

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

						String jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + activity.getEmail() + "\", \"token\": \"" + activity.getToken() + "\", \"idCountry\": \"" + myCountryId + "\", \"idState\": \"" + myStateId + "\", \"idCity\": \"" + myCityId + "\", \"numItems\": \"" + recordsItems.size() + "\", \"items\": [";

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

						HttpParams httpParameters = new BasicHttpParams();
						int timeoutConnection = MainActivity.HTTP_TIMEOUT;
						HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
						int timeoutSocket = MainActivity.SOCKET_TIMEOUT;
						HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
						HttpClient httpclient = new DefaultHttpClient(httpParameters);

						HttpPost httppost = new HttpPost(MainActivity.API_CART);
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
						nameValuePairs.add(new BasicNameValuePair("params", jsonStr));

						MLog.log(jsonStr);

						try {
							httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
							HttpResponse response = httpclient.execute(httppost);
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							response.getEntity().writeTo(out);
							out.close();
							String responseString = out.toString();
							MLog.log(responseString);

							// If response is received from the server pass it to the handler

							Message msg = new Message();
							msg.obj = responseString;
							msg.what = 1;
							threadHandler.sendMessage(msg);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							loadCart();

						}

					} else {

						// Show empty cart message
						displayHandler.sendEmptyMessage(2);

					}

				} else {

					//// Show empty cart message

					displayHandler.sendEmptyMessage(2);

				}

			}


		} else if(tName.equals(MainActivity.TH_NAME_VERIFY_LOGIN)) {

			MLog.log("Starting Verify Login thread.. ");

			String jsonStr = "";
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = MainActivity.HTTP_TIMEOUT;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = MainActivity.SOCKET_TIMEOUT;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
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

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				thLoginChecker = new Thread(FragmentPaymentConfirm.this);
				thLoginChecker.setName(MainActivity.TH_NAME_VERIFY_LOGIN);
				thLoginChecker.start();

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

			activity.handlerLoading.sendEmptyMessage(0);

		}

		else {

			// Calculate total by adding individual items

			double total = 0;
			for(int i = 0; i < itemIdList.size(); i++) {

				DiscountRecord dr = activity.getDiscountRecordFromId(itemIdDiscount.get(i));
				String discountedPrice = itemCostList.get(i);
				String pricePerItemStr = String.valueOf(Double.parseDouble(itemCostList.get(i)) / Double.parseDouble(itemQList.get(i)));
				if(dr != null) {


					String discountedPricePerItem = "";
					String totalOriginalPrice = itemCostList.get(i);;

					if(dr.type.contains(MainActivity.DB_DISCOUNT_TYPE_FLAT)) {

						discountedPricePerItem = String.valueOf((Double.parseDouble(pricePerItemStr)) - (Double.parseDouble(dr.value)));

					} else {

						discountedPricePerItem = String.valueOf((Double.parseDouble(pricePerItemStr)) - (Double.parseDouble(dr.value)*Double.parseDouble(pricePerItemStr))/100);

					}

					discountedPrice = String.valueOf(Double.parseDouble(discountedPricePerItem) * Double.parseDouble(itemQList.get(i)));



				} 

				String booking = itemBooking.get(i);
				if(itemBooking.get(i) != null) {

					if(itemBooking.get(i).length() > 0 && !itemBooking.get(i).equals("null")) {
						total += Double.parseDouble(booking) * Double.parseDouble(itemQList.get(i));
					} else {
						total += Double.parseDouble(discountedPrice);		
					}

				} else {
					total += Double.parseDouble(discountedPrice);
				}

			}
			
			MLog.log("Calculating Order total=" + total);

			// Apply coupon and recalculate

			final CouponRecord cr = activity.getCouponAppliedToCart();
			String couponCode = null;
			if(cr != null) {
				couponCode = cr.code;
			}

			String discountedOrigPrice = String.valueOf(total);
			String discountedPrice = String.valueOf(total);

			if(cr != null) {

				if(cr.type.contains(MainActivity.DB_DISCOUNT_TYPE_FLAT)) {

					discountedPrice = String.valueOf((Double.parseDouble(discountedPrice)) - (Double.parseDouble(cr.value)));

				} else {

					double discountDifference = (Double.parseDouble(cr.value)*Double.parseDouble(discountedPrice))/100;

					if(cr.maxVal != null) {

						if(cr.maxVal.length() > 0) {

							if(Double.parseDouble(cr.maxVal) < discountDifference) {

								discountedPrice = String.valueOf((Double.parseDouble(discountedPrice)) - Double.parseDouble(cr.maxVal));

							} else {

								discountedPrice = String.valueOf((Double.parseDouble(discountedPrice)) - discountDifference);

							}

						} else {

							discountedPrice = String.valueOf((Double.parseDouble(discountedPrice)) - discountDifference);

						}

					} else {

						discountedPrice = String.valueOf((Double.parseDouble(discountedPrice)) - discountDifference);

					}

				}

			}
			
			discountedPrice = String.format("%.2f", Double.parseDouble(discountedPrice));
			
			MLog.log("Calculating Order Discounted=" + discountedPrice);
			
			double taxedPrice = 0;
			
			if(activity.getTax1() != null) {

				if(activity.getTax2() != null) {

					TaxRecord taxRecord1 = activity.getTax1();
					taxedPrice = Double.parseDouble(discountedPrice) + (Double.parseDouble(taxRecord1.value)*Double.parseDouble(discountedPrice))/100;

					TaxRecord taxRecord2 = activity.getTax2();
					taxedPrice = (taxedPrice) + (Double.parseDouble(taxRecord2.value)*(taxedPrice))/100;

				} else {

					TaxRecord taxRecord1 = activity.getTax1();
					taxedPrice = Double.parseDouble(discountedPrice) + (Double.parseDouble(taxRecord1.value)*Double.parseDouble(discountedPrice))/100;

				}

			}


			String jsonStr = "";

			if(cr != null) {

				if(activity.getTax1() != null) {

					TaxRecord taxRecord1 = activity.getTax1();
					if(activity.getTax2() != null) {
					
						TaxRecord taxRecord2 = activity.getTax2();
						jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + activity.getEmail() + "\", \"token\": \"" + activity.getToken() + "\", \"nameCustomer\": \"" + pgRecord.billingName + "\", \"phoneCustomer\": \"" + pgRecord.billingTel + "\", \"address\": \"" + pgRecord.billingAddress + "\", \"country\": \"" + pgRecord.billingCountry + "\", \"state\": \"" + pgRecord.billingState + "\", \"city\": \"" + pgRecord.billingCity + "\", \"pincode\": \"" + pgRecord.billingZip + "\", \"modePayment\": \"online\", \"price\": \"" + String.format("%.2f", Double.parseDouble(discountedPrice))  + "\", \"priceOriginal\": \"" + String.format("%.2f", Double.parseDouble(discountedOrigPrice)) + "\", \"discountCode\": \"" + cr.code + "\", \"discountType\": \"" + cr.type + "\", \"discountValue\": \"" + cr.value + "\", \"numItems\": \"" +  itemIdList.size() + "\", \"taxLabel1\": \"" + taxRecord1.label + "\", \"taxValue1\": \"" + taxRecord1.value + "\", \"taxLabel2\": \"" + taxRecord2.label + "\", \"taxValue2\": \"" + taxRecord2.value + "\", \"taxLabel3\": \"\", \"taxValue3\": \"\", \"taxedPrice\": \"" + String.format("%.2f", taxedPrice) + "\", \"items\": [";
						
					} else {
						
						jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + activity.getEmail() + "\", \"token\": \"" + activity.getToken() + "\", \"nameCustomer\": \"" + pgRecord.billingName + "\", \"phoneCustomer\": \"" + pgRecord.billingTel + "\", \"address\": \"" + pgRecord.billingAddress + "\", \"country\": \"" + pgRecord.billingCountry + "\", \"state\": \"" + pgRecord.billingState + "\", \"city\": \"" + pgRecord.billingCity + "\", \"pincode\": \"" + pgRecord.billingZip + "\", \"modePayment\": \"online\", \"price\": \"" + String.format("%.2f", Double.parseDouble(discountedPrice)) + "\", \"priceOriginal\": \"" + String.format("%.2f", Double.parseDouble(discountedOrigPrice)) + "\", \"discountCode\": \"" + cr.code + "\", \"discountType\": \"" + cr.type + "\", \"discountValue\": \"" + cr.value + "\", \"numItems\": \"" +  itemIdList.size() + "\", \"taxLabel1\": \"" + taxRecord1.label + "\", \"taxValue1\": \"" + taxRecord1.value + "\", \"taxLabel2\": \"\", \"taxValue2\": \"\", \"taxLabel3\": \"\", \"taxValue3\": \"\", \"taxedPrice\": \"" + String.format("%.2f", taxedPrice) + "\", \"items\": [";
						
					}
					
				} else {
					
					jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + activity.getEmail() + "\", \"token\": \"" + activity.getToken() + "\", \"nameCustomer\": \"" + pgRecord.billingName + "\", \"phoneCustomer\": \"" + pgRecord.billingTel + "\", \"address\": \"" + pgRecord.billingAddress + "\", \"country\": \"" + pgRecord.billingCountry + "\", \"state\": \"" + pgRecord.billingState + "\", \"city\": \"" + pgRecord.billingCity + "\", \"pincode\": \"" + pgRecord.billingZip + "\", \"modePayment\": \"online\", \"price\": \"" + String.format("%.2f", Double.parseDouble(discountedPrice)) + "\", \"priceOriginal\": \"" + String.format("%.2f", Double.parseDouble(discountedOrigPrice)) + "\", \"discountCode\": \"" + cr.code + "\", \"discountType\": \"" + cr.type + "\", \"discountValue\": \"" + cr.value + "\", \"numItems\": \"" +  itemIdList.size() + "\", \"taxLabel1\": \"\", \"taxValue1\": \"\", \"taxLabel2\": \"\", \"taxValue2\": \"\", \"taxLabel3\": \"\", \"taxValue3\": \"\", \"taxedPrice\": \"\", \"items\": [";
					
				}
				

			} else {

				if(activity.getTax1() != null) {

					TaxRecord taxRecord1 = activity.getTax1();
					if(activity.getTax2() != null) {
					
						TaxRecord taxRecord2 = activity.getTax2();
						jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + activity.getEmail() + "\", \"token\": \"" + activity.getToken() + "\", \"nameCustomer\": \"" + pgRecord.billingName + "\", \"phoneCustomer\": \"" + pgRecord.billingTel + "\", \"address\": \"" + pgRecord.billingAddress + "\", \"country\": \"" + pgRecord.billingCountry + "\", \"state\": \"" + pgRecord.billingState + "\", \"city\": \"" + pgRecord.billingCity + "\", \"pincode\": \"" + pgRecord.billingZip + "\", \"modePayment\": \"online\", \"price\": \"" + String.format("%.2f", Double.parseDouble(discountedPrice)) + "\", \"priceOriginal\": \"" + String.format("%.2f", Double.parseDouble(discountedOrigPrice)) + "\", \"discountCode\": \"\", \"discountType\": \"\", \"discountValue\": \"\", \"numItems\": \"" +  itemIdList.size() + "\", \"taxLabel1\": \"" + taxRecord1.label + "\", \"taxValue1\": \"" + taxRecord1.value + "\", \"taxLabel2\": \"" + taxRecord2.label + "\", \"taxValue2\": \"" + taxRecord2.value + "\", \"taxLabel3\": \"\", \"taxValue3\": \"\", \"taxedPrice\": \"" + String.format("%.2f", taxedPrice) + "\", \"items\": [";
						
					} else {
						
						jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + activity.getEmail() + "\", \"token\": \"" + activity.getToken() + "\", \"nameCustomer\": \"" + pgRecord.billingName + "\", \"phoneCustomer\": \"" + pgRecord.billingTel + "\", \"address\": \"" + pgRecord.billingAddress + "\", \"country\": \"" + pgRecord.billingCountry + "\", \"state\": \"" + pgRecord.billingState + "\", \"city\": \"" + pgRecord.billingCity + "\", \"pincode\": \"" + pgRecord.billingZip + "\", \"modePayment\": \"online\", \"price\": \"" + String.format("%.2f", Double.parseDouble(discountedPrice)) + "\", \"priceOriginal\": \"" + String.format("%.2f", Double.parseDouble(discountedOrigPrice)) + "\", \"discountCode\": \"\", \"discountType\": \"\", \"discountValue\": \"\", \"numItems\": \"" +  itemIdList.size() + "\", \"taxLabel1\": \"" + taxRecord1.label + "\", \"taxValue1\": \"" + taxRecord1.value + "\", \"taxLabel2\": \"\", \"taxValue2\": \"\", \"taxLabel3\": \"\", \"taxValue3\": \"\", \"taxedPrice\": \"" + String.format("%.2f", taxedPrice) + "\", \"items\": [";
						
					}
					
				} else {
					
					jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + activity.getEmail() + "\", \"token\": \"" + activity.getToken() + "\", \"nameCustomer\": \"" + pgRecord.billingName + "\", \"phoneCustomer\": \"" + pgRecord.billingTel + "\", \"address\": \"" + pgRecord.billingAddress + "\", \"country\": \"" + pgRecord.billingCountry + "\", \"state\": \"" + pgRecord.billingState + "\", \"city\": \"" + pgRecord.billingCity + "\", \"pincode\": \"" + pgRecord.billingZip + "\", \"modePayment\": \"online\", \"price\": \"" + String.format("%.2f", Double.parseDouble(discountedPrice)) + "\", \"priceOriginal\": \"" + String.format("%.2f", Double.parseDouble(discountedOrigPrice)) + "\", \"discountCode\": \"\", \"discountType\": \"\", \"discountValue\": \"\", \"numItems\": \"" +  itemIdList.size() + "\", \"taxLabel1\": \"\", \"taxValue1\": \"\", \"taxLabel2\": \"\", \"taxValue2\": \"\", \"taxLabel3\": \"\", \"taxValue3\": \"\", \"taxedPrice\": \"\", \"items\": [";
					
				}
				
				//jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + activity.getEmail() + "\", \"token\": \"" + activity.getToken() + "\", \"nameCustomer\": \"" + pgRecord.billingName + "\", \"phoneCustomer\": \"" + pgRecord.billingTel + "\", \"address\": \"" + pgRecord.billingAddress + "\", \"country\": \"" + pgRecord.billingCountry + "\", \"state\": \"" + pgRecord.billingState + "\", \"city\": \"" + pgRecord.billingCity + "\", \"pincode\": \"" + pgRecord.billingZip + "\", \"modePayment\": \"online\", \"price\": \"" + discountedPrice + "\", \"priceOriginal\": \"" + discountedOrigPrice + "\", \"discountCode\": \"\", \"discountType\": \"\", \"discountValue\": \"\", \"taxLabel1\": \"\", \"taxValue1\": \"\", \"taxLabel2\": \"\", \"taxValue2\": \"\", \"taxLabel3\": \"\", \"taxValue3\": \"\", \"numItems\": \"" +  itemIdList.size() + "\", \"items\": [";

			}

			MLog.log("Calculating Order Discounted=" + discountedPrice);

			for(int i = 0; i < itemIdList.size(); i++) {

				String booking = itemBooking.get(i);

				jsonStr += "{";

				DiscountRecord dr = activity.getDiscountRecordFromId(itemIdDiscount.get(i));
				discountedPrice = itemCostList.get(i);
				String pricePerItemStr = String.valueOf(Double.parseDouble(itemCostList.get(i)) / Double.parseDouble(itemQList.get(i)));
				if(dr != null) {


					String discountedPricePerItem = "";
					String totalOriginalPrice = itemCostList.get(i);

					if(dr.type.contains(MainActivity.DB_DISCOUNT_TYPE_FLAT)) {

						discountedPricePerItem = String.valueOf((Double.parseDouble(pricePerItemStr)) - (Double.parseDouble(dr.value)));

					} else {

						discountedPricePerItem = String.valueOf((Double.parseDouble(pricePerItemStr)) - (Double.parseDouble(dr.value)*Double.parseDouble(pricePerItemStr))/100);

					}

					discountedPrice = String.valueOf(Double.parseDouble(discountedPricePerItem) * Double.parseDouble(itemQList.get(i)));

					jsonStr += "\"nameItem\": \"" + itemTitleList.get(i) + "\",";
					jsonStr += "\"qItem\": \"" + itemQList.get(i) + "\",";
					jsonStr += "\"priceItem\": \"" + discountedPrice + "\",";
					jsonStr += "\"discountCode\": \"" + dr.code + "\",";
					jsonStr += "\"discountType\": \"" + dr.type + "\",";
					jsonStr += "\"discountValue\": \"" + dr.value + "\",";
					jsonStr += "\"bookingPrice\": \"" + booking + "\",";
					jsonStr += "\"priceOriginal\": \"" + itemCostList.get(i) + "\"";

				} else {

					jsonStr += "\"nameItem\": \"" + itemTitleList.get(i) + "\",";
					jsonStr += "\"priceItem\": \"" + itemCostList.get(i) + "\",";
					jsonStr += "\"qItem\": \"" + itemQList.get(i) + "\",";
					jsonStr += "\"discountCode\": \"\",";
					jsonStr += "\"discountType\": \"\",";
					jsonStr += "\"discountValue\": \"\",";
					jsonStr += "\"bookingPrice\": \"" + booking + "\",";
					jsonStr += "\"priceOriginal\": \"" + itemCostList.get(i) + "\"";

				}

				if(i == (itemIdList.size() - 1)) {
					jsonStr += "}";
				} else {
					jsonStr += "},";
				}

			}

			jsonStr += "]}]";

			MLog.log(jsonStr);

			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = MainActivity.HTTP_TIMEOUT;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = MainActivity.SOCKET_TIMEOUT;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			HttpClient httpclient = new DefaultHttpClient(httpParameters);

			HttpPost httppost = new HttpPost(MainActivity.API_ORDER_NEW);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("params", jsonStr));

			MLog.log(jsonStr);

			try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				String responseString = out.toString();
				MLog.log(responseString);

				// If response is received from the server pass it to the handler

				Message msg = new Message();
				msg.what = 2;
				msg.obj = responseString;
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

		}

		activity.handlerLoading.sendEmptyMessage(0);

	}

	@Override
	public void setRunFlag(Boolean value) {
		// TODO Auto-generated method stub

	}

}
