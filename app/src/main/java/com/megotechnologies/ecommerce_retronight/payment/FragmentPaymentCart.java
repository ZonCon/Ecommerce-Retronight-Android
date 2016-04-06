package com.megotechnologies.ecommerce_retronight.payment;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Bitmap;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import com.megotechnologies.ecommerce_retronight.FragmentMeta;
import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.R;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCFragmentLifecycle;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCRunnable;
import com.megotechnologies.ecommerce_retronight.account.FragmentAccountLogin;
import com.megotechnologies.ecommerce_retronight.dataobjects.CouponRecord;
import com.megotechnologies.ecommerce_retronight.dataobjects.DiscountRecord;
import com.megotechnologies.ecommerce_retronight.dataobjects.TaxRecord;
import com.megotechnologies.ecommerce_retronight.utilities.ImageProcessingFunctions;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FragmentPaymentCart extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	public Boolean isBackFromLogin = false;
	LinearLayout llCrumbs;
	TextView tvTitle, tvCrumbsCart, tvCrumbsCheckout, tvCrumbsConfirm, tvCrumbsPayment;
	Thread thCartDownload;
	Thread thPictDownload;
	Thread thValidateCoupon;
	//Thread thCheckSignin;
	Thread thLoginChecker;
	String currentCoupon = "", currentPurchasePrice = "", currentDiscountDifference = "";
	Boolean currentContainsDiscount = false; 
	int IV_ID_PREFIX = 4000, TV_ID_PREFIX = 6000, SPIN_ID_PREFIX = 8000, CLOSE_ID_PREFIX = 10000;
	LinearLayout llContainer;
	ArrayList<String> itemTitleList, itemQList, itemPictList, itemCostList, itemIdList, itemIdDiscount, itemBooking;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		v =  inflater.inflate(R.layout.fragment_payment_cart, container, false);

		if(activity.IS_CONNECTED) {
			storeClassVariables();
			initUIHandles();
			initUIListeners();
			formatUI();
			activity.removeAppliedCouponToCart();
			loadCart();
			if(isBackFromLogin) {
				FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
				FragmentPaymentCheckout fragment = new FragmentPaymentCheckout();
				fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_CHECKOUT)
				.addToBackStack(MainActivity.SCREEN_CHECKOUT)
				.commit();
			}
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

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		activity.app.PREVENT_CLOSE_AND_SYNC = true;

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
		llCrumbs = (LinearLayout)v.findViewById(R.id.ll_crumbs);
		tvCrumbsCart = (TextView)v.findViewById(R.id.tv_crumbs_cart);
		tvCrumbsCheckout = (TextView)v.findViewById(R.id.tv_crumbs_checkout);
		tvCrumbsConfirm = (TextView)v.findViewById(R.id.tv_crumbs_confirm);
		tvCrumbsPayment = (TextView)v.findViewById(R.id.tv_crumbs_payment);

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

		tvCrumbsCart.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int)((MainActivity.TEXT_SIZE_TILE/3)*2.5));
		tvCrumbsCheckout.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int)((MainActivity.TEXT_SIZE_TILE/3)*2.5));
		tvCrumbsConfirm.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) ((MainActivity.TEXT_SIZE_TILE / 3) * 2.5));
		tvCrumbsPayment.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int)((MainActivity.TEXT_SIZE_TILE/3)*2.5));

		tvTitle.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);

		activity.app.ENABLE_SYNC = false;

	}


	void loadCart() {

		thCartDownload = new Thread(this);
		thCartDownload.setName(MainActivity.TH_NAME_CART);
		thCartDownload.start();

	}

	protected Handler displayHandler = new Handler() {

		public void handleMessage(Message msg) {

			activity.showCartNotification();

			MLog.log("inside display handler " + msg.what);

			try {

				if(msg.what == 1) {

					// Cart has expired
					// Clear the cart items

					// Get the cart handle

					HashMap<String, String> map = new HashMap<String, String>();
					map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_CART);
					map.put(MainActivity.DB_COL_CART_CART_ISOPEN, MainActivity.DB_RECORD_VALUE_CART_OPEN);
					String _idOpenCart = null;
					if(dbC.isOpen()) {
						dbC.isAvailale();
						_idOpenCart = dbC.retrieveId(map);
					}

					// Delete the card items

					map = new HashMap<String, String>();
					map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_CART_ITEM);
					map.put(MainActivity.DB_COL_FOREIGN_KEY, _idOpenCart);
					if(dbC.isOpen()) {
						dbC.isAvailale();
						dbC.deleteRecord(map);
					}

					// Show Message

					AlertDialog.Builder builder = new AlertDialog.Builder(activity.context);
					builder.setTitle("Alert");
					builder.setMessage(MainActivity.MSG_CART_EXPIRED);
					builder.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							llContainer.removeAllViews();
						}
					});
					builder.show();
					return;

				} else if(msg.what == 2) {

					// Cart contains no items

					AlertDialog.Builder builder = new AlertDialog.Builder(activity.context);
					builder.setTitle("Alert");
					builder.setMessage(MainActivity.MSG_CART_NOITEMS);
					builder.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							llContainer.removeAllViews();
							activity.loadShop();
						}
					});
					builder.show();
					return;

				} else if(msg.what == 3) {

					loadLogin();
					return;

				} else if(msg.what == 4) {

					/*
					if(!IS_SIGNEDIN) {

						FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
						FragmentAccountLogin fragment = new FragmentAccountLogin();
						//fragment.isBegin = true;
						fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_LOGIN_ACCOUNT)
						.addToBackStack(MainActivity.SCREEN_LOGIN_ACCOUNT)
						.commit();


					} else {
					*/

						FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
						FragmentPaymentCheckout fragment = new FragmentPaymentCheckout();
						fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_CHECKOUT)
						.addToBackStack(MainActivity.SCREEN_CHECKOUT)
						.commit();

					//}


				} else if(msg.what == 5) {

					AlertDialog.Builder builder = new AlertDialog.Builder(activity.context);
					builder.setTitle("Alert");
					builder.setMessage(MainActivity.MIN_PURCHASE_LIMIT_ERROR);
					builder.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							//llContainer.removeAllViews();
							//activity.loadShop();
						}
					});
					builder.show();
					return;

				} else if(msg.what == 6) {

					MLog.log("Current Coupon = " + currentCoupon);
					MLog.log("Current Coupon Contains Discount = " + currentContainsDiscount);
					MLog.log("Current Coupon Current Purchase Price = " + currentPurchasePrice);
					MLog.log("Current Coupon Difference = " + currentDiscountDifference);

					if(activity.applyCouponToCart(currentCoupon, currentContainsDiscount, currentPurchasePrice, currentDiscountDifference)) {

						MLog.log("Applied Discount");						

					}

				} else if(msg.what == 7) {

					AlertDialog.Builder alert  = new AlertDialog.Builder(activity.context);
					alert.setMessage(MainActivity.MSG_COUPON_INVALID);
					alert.setTitle("Alert");
					alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							//dismiss the dialog
						}
					});
					alert.create().show();

				} else if(msg.what == 8) {

					AlertDialog.Builder alert  = new AlertDialog.Builder(activity.context);
					alert.setMessage(MainActivity.MAX_PURCHASE_LIMIT_ERROR);
					alert.setTitle("Alert");
					alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							//dismiss the dialog
						}
					});
					alert.create().show();

				}

				if(itemTitleList.size() > 0) {

					llContainer.removeAllViews();

					double total = 0;
					Boolean containsDiscount = false;
					Boolean containsBooking = false;

					for(int i = 0; i < itemTitleList.size(); i++) {

						LinearLayout llRow = new LinearLayout(activity.context);
						llRow.setOrientation(LinearLayout.HORIZONTAL);
						LinearLayout.LayoutParams paramsLL = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
						paramsLL.topMargin = MainActivity.SPACING/2;
						paramsLL.leftMargin = MainActivity.SPACING/2;
						paramsLL.rightMargin = MainActivity.SPACING/2;
						llRow.setLayoutParams(paramsLL);
						//llRow.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.dark_shadow_background));

						// Create the left linear layout for the thumbnail picture

						LinearLayout llLeft = new LinearLayout(activity.context);
						llLeft.setOrientation(LinearLayout.VERTICAL);
						LinearLayout.LayoutParams paramsLLLeft = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
						paramsLLLeft.weight = 1;
						llLeft.setLayoutParams(paramsLLLeft);
						llLeft.setPadding(MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2);

						// Format & Insert ImageView into the left linear layout

						ImageView ivLeft = new ImageView(activity.context);
						LinearLayout.LayoutParams paramsIvLeft = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, activity.SCREEN_WIDTH/4);
						ivLeft.setLayoutParams(paramsIvLeft);
						ivLeft.setId(IV_ID_PREFIX + i);
						ivLeft.setScaleType(ScaleType.CENTER_CROP);

						llContainer.addView(llRow);
						llRow.addView(llLeft);
						llLeft.addView(ivLeft);

						String picture = itemPictList.get(i);

						if(picture.contains("localhost")) {

							String[] strArr = picture.split("/");
							picture = MainActivity.UPLOADS + "/" + strArr[strArr.length - 1];

						}

						if(picture.length() > 0) {

							thPictDownload = new Thread(FragmentPaymentCart.this);
							thPictDownload.setName(MainActivity.TH_NAME_CART + ";" + picture + ";" + (IV_ID_PREFIX + i));
							thPictDownload.start();

						} else {

							msg = new Message();
							msg.obj = null;
							msg.what = (IV_ID_PREFIX + i);
							MLog.log("Sending message to " + msg.what);
							threadPictureHandler.sendMessage(msg);

						}

						// Insert the middle linear layout

						LinearLayout llRight = new LinearLayout(activity.context);
						llRight.setOrientation(LinearLayout.VERTICAL);
						LinearLayout.LayoutParams paramsLLRight = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
						paramsLLRight.weight = 2;
						paramsLLRight.leftMargin = (MainActivity.SPACING/2);
						//paramsLLRight.rightMargin = (MainActivity.SPACING/2);
						llRight.setLayoutParams(paramsLLRight);
						llRight.setPadding(0, MainActivity.SPACING / 2, MainActivity.SPACING / 2, MainActivity.SPACING / 2);

						LinearLayout llRightTitle = new LinearLayout(activity.context);
						llRightTitle.setOrientation(LinearLayout.HORIZONTAL);
						LinearLayout.LayoutParams paramsLLRightTitle = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
						llRightTitle.setLayoutParams(paramsLLRightTitle);

						// Insert title

						TextView tvMidTitle = new TextView(activity.context);
						tvMidTitle.setText(itemTitleList.get(i));
						LinearLayout.LayoutParams paramsLLRightText = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
						paramsLLRightText.weight = 3;
						tvMidTitle.setLayoutParams(paramsLLRightText);
						tvMidTitle.setTextColor(getResources().getColor(R.color.text_color));
						tvMidTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.TEXT_SIZE_TILE);
						tvMidTitle.setPadding(0, MainActivity.SPACING / 2, 0, 0);
						tvMidTitle.setEllipsize(TruncateAt.END);
						tvMidTitle.setSingleLine();

						// Close Button

						TextView tvRightClose = new TextView(activity.context);
						paramsLLRightText = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
						paramsLLRightText.weight = 1;
						tvRightClose.setText(Character.toString(MainActivity.FONT_CHAR_CLOSE));
						tvRightClose.setLayoutParams(paramsLLRightText);
						tvRightClose.setTextColor(getResources().getColor(R.color.white));
						tvRightClose.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int)(MainActivity.TEXT_SIZE_TITLE));
						tvRightClose.setPadding(MainActivity.SPACING/4, MainActivity.SPACING/4, 0, MainActivity.SPACING/4);
						tvRightClose.setEllipsize(TruncateAt.END);
						tvRightClose.setSingleLine();
						tvRightClose.setGravity(Gravity.RIGHT);
						tvRightClose.setTypeface(activity.tf);
						tvRightClose.setId(CLOSE_ID_PREFIX + i);
						tvRightClose.setLineSpacing(0, 1.3f);
						tvRightClose.setShadowLayer(2, 1, 1, getResources().getColor(R.color.black));
						tvRightClose.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub

								final View view = v;

								AlertDialog.Builder alert = new AlertDialog.Builder(activity.context);
								alert.setMessage(MainActivity.MSG_CART_CONFIRM_REMOVE);
								alert.setPositiveButton(MainActivity.MSG_YES, new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub

										int index = view.getId() - CLOSE_ID_PREFIX;
										activity.removeAppliedCouponToCart();
										String _id = itemIdList.get(index);
										HashMap<String, String> mapWhere = new HashMap<String, String>();
										mapWhere.put(MainActivity.DB_COL_ID, _id);
										if(dbC.isOpen()) {
											dbC.isAvailale();
											dbC.deleteRecord(mapWhere);
										}

										loadCart();


									}

								});
								alert.setNegativeButton(MainActivity.MSG_NO, null);
								alert.show();



							}

						});

						DiscountRecord dr = activity.getDiscountRecordFromId(itemIdDiscount.get(i));
						String discountedPrice = (itemCostList.get(i));
						TextView tvOrigPrice = null;
						String pricePerItemStr = String.format("%.2f", (Double.parseDouble(itemCostList.get(i)) / Double.parseDouble(itemQList.get(i))));
						if(dr!=null) {

							containsDiscount = true;
							String discountedPricePerItem = "";
							String totalOriginalPrice = (itemCostList.get(i));

							if(dr.type.contains(MainActivity.DB_DISCOUNT_TYPE_FLAT)) {

								discountedPricePerItem = String.format("%.2f", (Double.parseDouble(pricePerItemStr)) - (Double.parseDouble(dr.value)));

							} else {

								discountedPricePerItem = String.format("%.2f", (Double.parseDouble(pricePerItemStr)) - (Double.parseDouble(dr.value)*Double.parseDouble(pricePerItemStr))/100);

							}

							discountedPrice = String.format("%.2f", (Double.parseDouble(discountedPricePerItem) * Double.parseDouble(itemQList.get(i))));

							tvOrigPrice = new TextView(activity.context);
							tvOrigPrice.setText(String.format("%.2f", Double.parseDouble(totalOriginalPrice)) + " " + MainActivity.SYMBOL_RUPEE);
							paramsLLRightText = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
							tvOrigPrice.setLayoutParams(paramsLLRightText);
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
						tvMidPrice.setText(String.format("%.2f", Double.parseDouble(discountedPrice)) + " " + MainActivity.SYMBOL_RUPEE);
						paramsLLRightText = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
						tvMidPrice.setLayoutParams(paramsLLRightText);
						tvMidPrice.setTextColor(getResources().getColor(R.color.text_color));
						tvMidPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
						//tvRightTitle.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
						tvMidPrice.setEllipsize(TruncateAt.END);
						tvMidPrice.setSingleLine();

						TextView tvBooking = null;
						if(itemBooking.get(i) != null) {

							String booking = itemBooking.get(i);

							if(itemBooking.get(i).length() > 0 && !itemBooking.get(i).equals("null")) {
								tvBooking = new TextView(activity.context);
								tvBooking.setText("Booking price: " + String.format("%.2f", Double.parseDouble(booking)) + " " + MainActivity.SYMBOL_RUPEE);
								paramsLLRightText = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
								tvBooking.setLayoutParams(paramsLLRightText);
								tvBooking.setTextColor(getResources().getColor(R.color.text_color));
								tvBooking.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
							}

						}

						// Insert Quantity Label and Spinner

						TextView tvMidQ = new TextView(activity.context);
						paramsLLRightText = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
						tvMidQ.setText("Quantity");
						tvMidQ.setLayoutParams(paramsLLRightText);
						tvMidQ.setTextColor(getResources().getColor(R.color.light_gray));
						tvMidQ.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
						//tvRightTitle.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
						tvMidQ.setEllipsize(TruncateAt.END);
						tvMidQ.setSingleLine();

						Spinner spinQ = new Spinner(activity.context);
						spinQ.setLayoutParams(paramsLLRightText);
						ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity.context, android.R.layout.simple_spinner_item, MainActivity.ITEMS_LIST);
						spinQ.setAdapter(adapter);
						spinQ.setId(SPIN_ID_PREFIX + i);
						spinQ.setOnItemSelectedListener(new OnItemSelectedListener() {

							@Override
							public void onItemSelected(AdapterView<?> parent,
									View view, int position, long id) {
								// TODO Auto-generated method stub

								int index = parent.getId() - SPIN_ID_PREFIX;
								Spinner spin = (Spinner)parent;

								// If the quantity selection has changed, update the new quantity into the database and reload the cart

								if(Integer.parseInt(itemQList.get(index)) != Integer.parseInt((String) spin.getItemAtPosition(position))) {

									String q = (String)spin.getItemAtPosition(position);
									String _id = itemIdList.get(index);

									HashMap<String, String> mapWhere = new HashMap<String, String>();
									mapWhere.put(MainActivity.DB_COL_ID, _id);
									HashMap<String, String> map = new HashMap<String, String>();
									map.put(MainActivity.DB_COL_CART_ITEM_QUANTITY, q);
									if(dbC.isOpen()) {
										dbC.isAvailale();
										dbC.updateRecord(map, mapWhere);
									}

									MLog.log("Spin=" + parent.getId());
									MLog.log("DB_ID=" + itemIdList.get(index));
									MLog.log("Q=" + q);

									activity.removeAppliedCouponToCart();
									loadCart();

								}

							}

							@Override
							public void onNothingSelected(AdapterView<?> parent) {
								// TODO Auto-generated method stub

							}
						});
						spinQ.setSelection(Integer.parseInt(itemQList.get(i)) - 1);

						llRow.addView(llRight);
						llRight.addView(llRightTitle);
						llRightTitle.addView(tvMidTitle);
						llRightTitle.addView(tvRightClose);
						if(dr != null) {
							llRight.addView(tvOrigPrice);
						}
						llRight.addView(tvMidPrice);
						if(itemBooking.get(i) != null) {
							if(itemBooking.get(i).length() > 0 && !itemBooking.get(i).equals("null")) {
								llRight.addView(tvBooking);
							}
						}
						llRight.addView(tvMidQ);
						llRight.addView(spinQ);

						LinearLayout llRowLine = new LinearLayout(activity.context);
						llRowLine.setOrientation(LinearLayout.HORIZONTAL);
						paramsLL = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 2);
						llRowLine.setLayoutParams(paramsLL);
						//llRowLine.setBackgroundColor(getResources().getColor(R.color.white));
						llContainer.addView(llRowLine);

						LinearLayout llLeftLine = new LinearLayout(activity.context);
						llLeftLine.setOrientation(LinearLayout.VERTICAL);
						paramsLLLeft = new LinearLayout.LayoutParams(0, 2);
						paramsLLLeft.weight = 1;
						llLeftLine.setLayoutParams(paramsLLLeft);
						llRowLine.addView(llLeftLine);

						LinearLayout llRightLine = new LinearLayout(activity.context);
						llRightLine.setOrientation(LinearLayout.VERTICAL);
						paramsLLRight = new LayoutParams(0, 2);
						paramsLLRight.leftMargin = MainActivity.SPACING/2;
						paramsLLRight.rightMargin = MainActivity.SPACING/2;
						paramsLLRight.weight = 2;
						llRightLine.setLayoutParams(paramsLLRight);
						llRightLine.setBackgroundColor(getResources().getColor(R.color.line_separator));
						llRowLine.addView(llRightLine);

					}

					LinearLayout llCoupon = new LinearLayout(activity.context);
					LinearLayout.LayoutParams llCouponParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					llCouponParams.gravity = Gravity.CENTER;
					llCoupon.setLayoutParams(llCouponParams);
					llCoupon.setOrientation(LinearLayout.HORIZONTAL);
					llCoupon.setGravity(Gravity.CENTER);
					llCoupon.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
					llContainer.addView(llCoupon);

					final CouponRecord cr = activity.getCouponAppliedToCart();
					String couponCode = null;
					if(cr != null) {
						couponCode = cr.code;
						MLog.log("Setting Current Code = " + currentCoupon);
					}

					TextView tvCoupon = null;
					final EditText editCoupon = new EditText(activity.context);
					if(couponCode != null) {

						LinearLayout.LayoutParams llEditCouponParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
						llEditCouponParams.weight = 2;
						llEditCouponParams.setMargins(0, 0, MainActivity.SPACING / 2, 0);
						editCoupon.setLayoutParams(llEditCouponParams);
						editCoupon.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
						editCoupon.setTextColor(getResources().getColor(R.color.text_color));
						//editCoupon.setPadding(MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2);
						editCoupon.setGravity(Gravity.CENTER);
						editCoupon.setHint("ENTER COUPON CODE");
						editCoupon.setEnabled(false);
						editCoupon.setText(couponCode);
						llCoupon.addView(editCoupon);

						tvCoupon = new TextView(activity.context);
						LinearLayout.LayoutParams llTvCouponParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
						llTvCouponParams.weight = 2;
						llTvCouponParams.setMargins(MainActivity.SPACING / 2, 0, 0, 0);
						tvCoupon.setLayoutParams(llTvCouponParams);
						tvCoupon.setTextColor(getResources().getColor(R.color.white));
						tvCoupon.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
						tvCoupon.setText("REMOVE COUPON");
						tvCoupon.setGravity(Gravity.CENTER);
						tvCoupon.setBackgroundDrawable(getResources().getDrawable(R.drawable.red_shadow_background));
						tvCoupon.setPadding(MainActivity.SPACING, MainActivity.SPACING / 2, MainActivity.SPACING, MainActivity.SPACING / 2);
						llCoupon.addView(tvCoupon);

					} else {

						LinearLayout.LayoutParams llEditCouponParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
						llEditCouponParams.weight = 2;
						llEditCouponParams.setMargins(0, 0, MainActivity.SPACING / 2, 0);
						editCoupon.setLayoutParams(llEditCouponParams);
						editCoupon.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
						editCoupon.setTextColor(getResources().getColor(R.color.text_color));
						//editCoupon.setPadding(MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2);
						editCoupon.setGravity(Gravity.CENTER);
						editCoupon.setHint("ENTER COUPON CODE");
						editCoupon.setEnabled(true);
						llCoupon.addView(editCoupon);

						tvCoupon = new TextView(activity.context);
						LinearLayout.LayoutParams llTvCouponParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
						llTvCouponParams.weight = 2;
						llTvCouponParams.setMargins(MainActivity.SPACING / 2, 0, 0, 0);
						tvCoupon.setLayoutParams(llTvCouponParams);
						tvCoupon.setTextColor(getResources().getColor(R.color.white));
						tvCoupon.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
						tvCoupon.setText("APPLY COUPON");
						tvCoupon.setGravity(Gravity.CENTER);
						tvCoupon.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_shadow_background));
						tvCoupon.setPadding(MainActivity.SPACING, MainActivity.SPACING / 2, MainActivity.SPACING, MainActivity.SPACING / 2);
						llCoupon.addView(tvCoupon);

						TextView tvCaseIndicator = new TextView(activity.context);
						LinearLayout.LayoutParams llTvCaseIndicator = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
						llTvCaseIndicator.setMargins(MainActivity.SPACING, 0, 0, 0);
						tvCaseIndicator.setLayoutParams(llTvCaseIndicator);
						tvCaseIndicator.setTextColor(getResources().getColor(R.color.light_gray));
						tvCaseIndicator.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE * 0.8));
						tvCaseIndicator.setText("*Coupon Code is case sensitive");
						llContainer.addView(tvCaseIndicator);

					}

					String discountedOrigPrice = String.format("%.2f", total);
					String discountedPrice = String.format("%.2f", total);
					TextView tvOrigTotal = null;
					TextView tvDiscount = null;
					if(cr != null) {

						if(cr.type.contains(MainActivity.DB_DISCOUNT_TYPE_FLAT)) {

							discountedPrice = String.format("%.2f", (Double.parseDouble(discountedPrice)) - (Double.parseDouble(cr.value)));

						} else {

							double discountDifference = (Double.parseDouble(cr.value)*Double.parseDouble(discountedPrice))/100;

							if(cr.maxVal != null) {

								if(cr.maxVal.length() > 0) {

									if(Integer.parseInt(cr.maxVal) < discountDifference) {

										discountedPrice = String.format("%.2f", (Double.parseDouble(discountedPrice)) - Double.parseDouble(cr.maxVal));

									} else {

										discountedPrice = String.format("%.2f", (Double.parseDouble(discountedPrice)) - discountDifference);

									}

								} else {

									discountedPrice = String.format("%.2f", (Double.parseDouble(discountedPrice)) - discountDifference);

								}

							} else {

								discountedPrice = String.format("%.2f", (Double.parseDouble(discountedPrice)) - discountDifference);

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

						tvDiscount = new TextView(activity.context);
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
					tvTotalParams.setMargins(MainActivity.SPACING / 2, MainActivity.SPACING, MainActivity.SPACING, 0);
					tvTotal.setLayoutParams(tvTotalParams);
					tvTotal.setTextColor(getResources().getColor(R.color.text_color));
					tvTotal.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
					tvTotal.setText("Total: " + String.format("%.2f", Double.parseDouble(discountedPrice)) + " " + MainActivity.SYMBOL_RUPEE);
					tvTotal.setGravity(Gravity.RIGHT);
					tvTotal.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
					llContainer.addView(tvTotal);

					final Boolean finalContainsDiscount = containsDiscount;
					final String purchasePrice = discountedOrigPrice;
					final String discountDifference = String.format("%.2f", Double.parseDouble(purchasePrice) - Double.parseDouble(discountedPrice));
					if(couponCode != null) {

						tvCoupon.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub

								activity.removeAppliedCouponToCart();
								loadCart();

							}

						});


					} else {

						final Boolean finalContainsBooking = containsBooking;
						tvCoupon.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub

								if(!finalContainsBooking) {

									currentCoupon = editCoupon.getText().toString();
									currentContainsDiscount = finalContainsDiscount;
									currentPurchasePrice = purchasePrice;
									currentDiscountDifference = discountDifference;

									thValidateCoupon = new Thread(FragmentPaymentCart.this);
									thValidateCoupon.setName(MainActivity.TH_NAME_VALIDATE_COUPON);
									thValidateCoupon.start();

								} else {

									AlertDialog.Builder alert  = new AlertDialog.Builder(activity.context);
									alert.setMessage(MainActivity.MSG_COUPON_BOOKING_INVALID);
									alert.setTitle("Alert");
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

					if(activity.getTax1() != null) {

						double taxedPrice = 0;

						if(activity.getTax2() != null) {

							TaxRecord taxRecord1 = activity.getTax1();
							taxedPrice = Double.parseDouble(discountedPrice) + (Double.parseDouble(taxRecord1.value)*Double.parseDouble(discountedPrice))/100;

							TextView tvTax1 = new TextView(activity.context);
							LinearLayout.LayoutParams tvTax1Params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
							tvTax1Params.setMargins(MainActivity.SPACING / 2, MainActivity.SPACING / 2, MainActivity.SPACING, 0);
							tvTax1.setLayoutParams(tvTax1Params);
							tvTax1.setTextColor(getResources().getColor(R.color.text_color));
							tvTax1.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
							tvTax1.setText(taxRecord1.label + ": " + taxRecord1.value + "%");
							tvTax1.setGravity(Gravity.RIGHT);
							tvTax1.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
							//llContainer.addView(tvTax1);

							TaxRecord taxRecord2 = activity.getTax2();
							taxedPrice = (taxedPrice) + (Double.parseDouble(taxRecord2.value)*(taxedPrice))/100;

							TextView tvTax2 = new TextView(activity.context);
							LinearLayout.LayoutParams tvTax2Params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
							tvTax2Params.setMargins(MainActivity.SPACING / 2, MainActivity.SPACING / 2, MainActivity.SPACING, 0);
							tvTax2.setLayoutParams(tvTax2Params);
							tvTax2.setTextColor(getResources().getColor(R.color.text_color));
							tvTax2.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
							tvTax2.setText(taxRecord2.label + ": " + taxRecord2.value + "%");
							tvTax2.setGravity(Gravity.RIGHT);
							tvTax2.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
							//llContainer.addView(tvTax2);

							TextView tvTaxedTotal = new TextView(activity.context);
							LinearLayout.LayoutParams tvTaxedTotalParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
							tvTaxedTotalParams.setMargins(MainActivity.SPACING / 2, MainActivity.SPACING / 2, MainActivity.SPACING, MainActivity.SPACING);
							tvTaxedTotal.setLayoutParams(tvTotalParams);
							tvTaxedTotal.setTextColor(getResources().getColor(R.color.text_color));
							tvTaxedTotal.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
							tvTaxedTotal.setText("Net Total: " + String.format("%.2f", taxedPrice) + " " + MainActivity.SYMBOL_RUPEE);
							tvTaxedTotal.setGravity(Gravity.RIGHT);
							tvTaxedTotal.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
							//llContainer.addView(tvTaxedTotal);

						} else {


							TaxRecord taxRecord1 = activity.getTax1();
							taxedPrice = Double.parseDouble(discountedPrice) + (Double.parseDouble(taxRecord1.value)*Double.parseDouble(discountedPrice))/100;

							TextView tvTax1 = new TextView(activity.context);
							LinearLayout.LayoutParams tvTax1Params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
							tvTax1Params.setMargins(MainActivity.SPACING / 2, MainActivity.SPACING / 2, MainActivity.SPACING, 0);
							tvTax1.setLayoutParams(tvTotalParams);
							tvTax1.setTextColor(getResources().getColor(R.color.text_color));
							tvTax1.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
							tvTax1.setText(taxRecord1.label + ": " + taxRecord1.value + "%");
							tvTax1.setGravity(Gravity.RIGHT);
							tvTax1.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
							//llContainer.addView(tvTax1);

							TextView tvTaxedTotal = new TextView(activity.context);
							LinearLayout.LayoutParams tvTaxedTotalParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
							tvTaxedTotalParams.setMargins(MainActivity.SPACING / 2, MainActivity.SPACING / 2, MainActivity.SPACING, MainActivity.SPACING);
							tvTaxedTotal.setLayoutParams(tvTotalParams);
							tvTaxedTotal.setTextColor(getResources().getColor(R.color.text_color));
							tvTaxedTotal.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
							tvTaxedTotal.setText("Net Total: " + String.format("%.2f", taxedPrice) + " " + MainActivity.SYMBOL_RUPEE);
							tvTaxedTotal.setGravity(Gravity.RIGHT);
							tvTaxedTotal.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING);
							//llContainer.addView(tvTaxedTotal);

						}


					}


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
					tvNext.setTextColor(getResources().getColor(R.color.white));
					tvNext.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
					tvNext.setText("PROCEED TO CHECKOUT");
					tvNext.setGravity(Gravity.CENTER);
					tvNext.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_shadow_background));
					tvNext.setPadding(MainActivity.SPACING, MainActivity.SPACING / 2, MainActivity.SPACING, MainActivity.SPACING / 2);
					final double totalPurchase = (total);
					tvNext.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub

							MLog.log("Email=" + activity.getEmail());
							MLog.log("Token=" + activity.getToken());
							MLog.log("Total=" + totalPurchase);

							if(totalPurchase >= MainActivity.MIN_PURCHASE_LIMIT) {

								if(totalPurchase >= MainActivity.MAX_PURCHASE_LIMIT) {

									displayHandler.sendEmptyMessage(8);

								} else {

									if(activity.getToken() != null && activity.getEmail() != null) {

										if(activity.getToken().length() > 0 && activity.getEmail().length() > 0) {

											thLoginChecker = new Thread(FragmentPaymentCart.this);
											thLoginChecker.setName(MainActivity.TH_NAME_VERIFY_LOGIN);
											thLoginChecker.start();
											return;

										}

									}

									displayHandler.sendEmptyMessage(4);

								}

							} else {

								displayHandler.sendEmptyMessage(5);

							}

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
					tvPrev.setText("RETURN TO SHOP");
					tvPrev.setGravity(Gravity.CENTER);
					tvPrev.setBackgroundDrawable(getResources().getDrawable(R.drawable.yellow_shadow_background));
					tvPrev.setPadding(MainActivity.SPACING, MainActivity.SPACING / 2, MainActivity.SPACING, MainActivity.SPACING / 2);
					tvPrev.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub

							activity.loadShop();

						}

					});

					llContainer.addView(llButtons);
					llButtons.addView(tvNext);
					llButtons.addView(tvPrev);




				}

			} catch (IllegalStateException e) {

			}

		};

	};

	void loadLogin() {

		FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
		FragmentAccountLogin fragment = new FragmentAccountLogin();
		fragment.isBegin = false;
		fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_LOGIN_ACCOUNT)
		.addToBackStack(MainActivity.SCREEN_LOGIN_ACCOUNT)
		.commit();

		activity.getFragmentManager().beginTransaction().remove(FragmentPaymentCart.this).commit();

	}

	protected Handler threadHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			String jsonStr = (String)msg.obj;
			if(jsonStr != null && jsonStr.length() > 0) {

				MLog.log(jsonStr);

				JSONObject jsonObj;
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


						// Check if the number of cart items in the local DB match with what is returned from the server
						// If there is any discrepancy, one of the items may have been deleted from the server
						// In such a cart expired message must be shown to the user and the cart should be cleared

						if(recordsItems.size() == itemIdList.size()) {

							// Send an empty message to the display handler indicating that the download and extraction has finished
							// and we are now ready to display the cart items

							displayHandler.sendEmptyMessage(0);

						} else {

							displayHandler.sendEmptyMessage(1);

						}

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

			}

		}

	};

	protected Handler threadPictureHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			Bitmap bmp = (Bitmap)msg.obj;
			if(bmp != null) {

				MLog.log("Displaying = " + msg.what);

				ImageView iv = (ImageView)v.findViewById(msg.what);
				// Beta Test case
				if(iv != null) {
					iv.setImageBitmap(bmp);
				}

			} else {

				ImageView iv = (ImageView)v.findViewById(msg.what);
				// Beta Test case
				if(iv != null) {
					iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
				}

			}

		}

	};

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Looper.prepare();

		//if(!IS_SIGNEDIN) {

		//displayHandler.sendEmptyMessage(3);
		//return;
		//}

		Thread t = Thread.currentThread();
		String tName = t.getName();

		// Check if current thread is cart download thread or picture download thread

		//activity.handlerLoading.sendEmptyMessage(1);

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

						//String jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + activity.getEmail() + "\", \"token\": \"" + activity.getToken() + "\", \"idCountry\": \"" + myCountryId + "\", \"idState\": \"" + myStateId + "\", \"idCity\": \"" + myCityId + "\", \"numItems\": \"" + recordsItems.size() + "\", \"items\": [";
						String jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"idCountry\": \"" + myCountryId + "\", \"idState\": \"" + myStateId + "\", \"idCity\": \"" + myCityId + "\", \"numItems\": \"" + recordsItems.size() + "\", \"items\": [";

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
						HttpPost httppost = null;

						httpclient = new DefaultHttpClient();
						httppost = new HttpPost(MainActivity.API_CART);
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

			activity.handlerLoading.sendEmptyMessage(1);

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
				thLoginChecker = new Thread(FragmentPaymentCart.this);
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

		}  else if(tName.equals(MainActivity.TH_NAME_VALIDATE_COUPON)) {

			String jsonStr = "";
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = MainActivity.HTTP_TIMEOUT;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = MainActivity.SOCKET_TIMEOUT;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpPost httppost = null;

			httppost = new HttpPost(MainActivity.API_COUPON_VALIDATE);
			jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"idCountry\": \"" + activity.myCountryId + "\", \"idState\": \"" + activity.myStateId + "\", \"idCity\": \"" + activity.myCityId + "\"}]";	

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("params", jsonStr));
			MLog.log("VerifyCoupon API=" + jsonStr);

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
				thValidateCoupon = new Thread(FragmentPaymentCart.this);
				thValidateCoupon.setName(MainActivity.TH_NAME_VALIDATE_COUPON);
				thValidateCoupon.start();

			} 

			MLog.log("Coupons=" + responseString);

			if(responseString != null) {

				jsonStr = responseString;

				try {

					JSONObject jsonObj = new JSONObject(jsonStr);
					if(jsonObj.getString("result").equals("success")) {

						String valueStr = jsonObj.getString("value");
						jsonObj = new JSONObject(valueStr);
						JSONArray jsonArrTotal = (JSONArray)jsonObj.getJSONArray("total");
						JSONArray jsonArrAvl = (JSONArray)jsonObj.getJSONArray("available");

						Boolean foundTotal = false;
						Boolean foundAvl = false;

						for(int i = 0; i < jsonArrTotal.length(); i++) {

							JSONObject obj = (JSONObject)jsonArrTotal.get(i);
							String code = obj.getString("code");

							MLog.log("Matching total coupons " + code + " & " + currentCoupon);

							if(code.equals(currentCoupon)) {

								foundTotal = true;
								break;

							}

						}


						if(!foundTotal) {
							//Invalid coupon
							displayHandler.sendEmptyMessage(7);

						} else {

							if(jsonArrAvl.length() > 0) {

								JSONObject obj = (JSONObject)jsonArrAvl.getJSONObject(0);

								CouponRecord cr = new CouponRecord();
								cr.id = obj.getString("idCoupons");
								cr.code = obj.getString("code");
								cr.type = obj.getString("type");
								cr.value = obj.getString("value");
								cr.allowDoubleDiscouting = obj.getString("allowDoubleDiscounting");
								cr.minPurchase = obj.getString("minPurchase");
								cr.maxVal = obj.getString("maxVal");

								MLog.log("Entered Coupon = " + currentCoupon);
								MLog.log("Downloaded Coupon = " +cr.code);

								MLog.log("Limit Reached = " + cr.code.equals(currentCoupon));

								if(cr.code.equals(currentCoupon)) {

									activity.addNewCoupon(cr, obj.getString("timestamp"));
									displayHandler.sendEmptyMessage(6);	

								}

							}

						}

					}


				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}


		} else {

			String[] strArr = tName.split(";");
			String url = strArr[1];
			String id = strArr[2];

			Bitmap bmp = (new ImageProcessingFunctions()).decodeSampledBitmapFromStream(url, MainActivity.IMG_DETAIL_MAX_SIZE, MainActivity.IMG_DETAIL_MAX_SIZE);
			Message msg = new Message();
			msg.obj = bmp;
			msg.what = Integer.parseInt(id);
			MLog.log("Sending message to " + id);
			threadPictureHandler.sendMessage(msg);

		}

		//activity.handlerLoading.sendEmptyMessage(0);

	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		activity.app.PREVENT_CLOSE_AND_SYNC = false;
	}

	@Override
	public void setRunFlag(Boolean value) {
		// TODO Auto-generated method stub

	}

}
