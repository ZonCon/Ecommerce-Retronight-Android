package com.megotechnologies.ecommerce_retronight;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.megotechnologies.ecommerce_retronight.about.FragmentAboutItemsList;
import com.megotechnologies.ecommerce_retronight.account.FragmentShippingLocation;
import com.megotechnologies.ecommerce_retronight.alerts.FragmentAlertItemsList;
import com.megotechnologies.ecommerce_retronight.dataobjects.CouponRecord;
import com.megotechnologies.ecommerce_retronight.dataobjects.DiscountRecord;
import com.megotechnologies.ecommerce_retronight.dataobjects.TaxRecord;
import com.megotechnologies.ecommerce_retronight.db.DbConnection;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCActivityLifecycle;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCRunnable;
import com.megotechnologies.ecommerce_retronight.loading.FragmentLoading;
import com.megotechnologies.ecommerce_retronight.payment.FragmentPaymentCart;
import com.megotechnologies.ecommerce_retronight.policy.FragmentPolicyItemsList;
import com.megotechnologies.ecommerce_retronight.shop.FragmentShopCategories;
import com.megotechnologies.ecommerce_retronight.shop.FragmentShopItemsList;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity implements ZCActivityLifecycle, ZCRunnable {

	public static Boolean LOG = true;

	// Project
	
	public static String PID = "14";
	
	// API
	
	public static String API = "http://www.zoncon.com/v6_1/index.php/projects/";
	public static String API_STREAMS = API + "get_public_streams";
	public static String API_INDI_STREAMS = API + "get_public_individual_stream";
	public static String API_NOTIFICATIONS = API + "get_public_notification";
	public static String API_COUNTRIES = API + "get_public_countries";
	public static String API_STATES = API + "get_public_states";
	public static String API_CITIES = API + "get_public_cities";
	public static String API_CART = API + "get_public_cart";
	public static String API_USER_APPS = API + "get_public_user_apps";
	public static String API_CREATE_ACCOUNT = API + "add_public_consumer";
	public static String API_RESET_PASSWORD = API + "public_consumer_password_reset";
	public static String API_FORGOT_CHANGE_PASSWORD = API + "public_consumer_forgot_change_password";
	public static String API_LOGIN = API + "public_consumer_login";
	public static String API_ORDER_NEW = API + "public_new_order";
	public static String API_ORDER_CONFIRM = API + "public_confirm_order_payment";
	public static String API_ORDER_CANCEL = API + "public_cancel_order_payment";
	public static String API_VALIDATE_LOC = API + "public_validate_location";
	public static String API_VERIFY_LOGIN = API + "public_verify_login";
	public static String API_ORDERS_LIST = API + "public_orders_get";
	public static String API_COUPON_VALIDATE = API + "public_coupon_validate";
	public static String API_ORDERS_SINGLE = API + "public_orders_get_single";
	public static String UPLOADS = "http://www.zoncon.com/v6_1/uploads";
	public static String PG_IFRAME_URL = "";
	public static String PG_REDIRECT_URL = "";
	public static String PG_MERCHANT_ID = "";

	// Fonts


	public static char FONT_CHAR_URL = 0xe70a;
	public static char FONT_CHAR_CONTACT = 0xe708;
	public static char FONT_CHAR_LOCATION = 0xe706;
	public static char FONT_CHAR_ATTACHMENT = 0xe70b;
	public static char FONT_CHAR_CLOSE = 0xe701;
	public static char FONT_CHAR_SHARE = 0xe713;
	public static char FONT_CHAR_LIKED = 0xe71c;

	// Payment Gateway
	
	public static String PG_ACCESS_CODE = "AVUO02BI71CK19OUKC";
	public static String PG_CURRENCY = "INR";

	// Maps

	public static String MAPS_PREFIX = "http://maps.google.com/?q=";

	// External Storage

	public static String STORAGE_PATH = "";

	// Screen

	public static int SCREEN_WIDTH;
	public static int SCREEN_HEIGHT;
	public static int SPACING = 20;

	// Pictures

	public static int IMG_TH_MAX_SIZE = 200;
	public static int IMG_DETAIL_MAX_SIZE = 800;

	// Symbols
	
	public static String SYMBOL_RUPEE = "INR";
	
	// Text size
	
	public static char TEXT_SIZE_TILE = 12;
	public static char TEXT_SIZE_BUTTON = 17;
	public static char TEXT_SIZE_TITLE = 16;
	
	// DB
	
	public static String DB_TABLE = "records";
	public static String DB_NAME = "zoncon_ecommerce_retronight.db";
	public static int DB_VERSION = 5;
	
	// DB Columns
	
	public static String DB_COL_ID = "_id";
	public static String DB_COL_SRV_ID = "server_id";
	public static String DB_COL_TYPE = "type";
	public static String DB_COL_TITLE = "title";
	public static String DB_COL_SUB = "subtitle";
	public static String DB_COL_CONTENT = "content";
	public static String DB_COL_TIMESTAMP = "timestamp";
	public static String DB_COL_STOCK = "stock";
	public static String DB_COL_PRICE = "price";
	public static String DB_COL_EXTRA_1 = "extra1";
	public static String DB_COL_EXTRA_2 = "extra2";
	public static String DB_COL_EXTRA_3 = "extra3";
	public static String DB_COL_EXTRA_4 = "extra4";
	public static String DB_COL_EXTRA_5 = "extra5";
	public static String DB_COL_EXTRA_6 = "extra6";
	public static String DB_COL_EXTRA_7 = "extra7";
	public static String DB_COL_EXTRA_8 = "extra8";
	public static String DB_COL_EXTRA_9 = "extra9";
	public static String DB_COL_EXTRA_10 = "extra10";
	public static String DB_COL_BOOKING = "bookingPrice";
	public static String DB_COL_DISCOUNT = "discount";
	public static String DB_COL_SKU = "sku";
	public static String DB_COL_SIZE = "size";
	public static String DB_COL_WEIGHT = "weight";
	public static String DB_COL_NAME = "name";
	public static String DB_COL_CAPTION = "caption";
	public static String DB_COL_URL = "url";
	public static String DB_COL_LOCATION = "location";
	public static String DB_COL_EMAIL = "email";
	public static String DB_COL_PHONE = "phone";
	public static String DB_COL_PATH_ORIG = "path_original";
	public static String DB_COL_PATH_PROC = "path_processed";
	public static String DB_COL_PATH_TH = "path_thumbnail";
	public static String DB_COL_CART_ITEM_STREAM_SRV_ID = "cart_item_stream_server_id";
	public static String DB_COL_CART_ITEM_SRV_ID = "cart_item_server_id";
	public static String DB_COL_CART_ITEM_QUANTITY = "cart_item_quantity";
	public static String DB_COL_CART_COUPON_CODE = "cart_coupon_code";
	public static String DB_COL_CART_CART_ISOPEN = "cart_isopen";
	public static String DB_COL_FOREIGN_KEY = "foreign_key";
	public static String[] DB_ALL_COL = {DB_COL_ID, DB_COL_SRV_ID, DB_COL_TYPE, DB_COL_TITLE, DB_COL_SUB, DB_COL_CONTENT, DB_COL_TIMESTAMP, DB_COL_STOCK, DB_COL_PRICE, DB_COL_EXTRA_1, DB_COL_EXTRA_2, DB_COL_EXTRA_3, DB_COL_EXTRA_4, DB_COL_EXTRA_5, DB_COL_EXTRA_6, DB_COL_EXTRA_7, DB_COL_EXTRA_8, DB_COL_EXTRA_9, DB_COL_EXTRA_10, DB_COL_BOOKING, DB_COL_DISCOUNT, DB_COL_SKU, DB_COL_SIZE, DB_COL_WEIGHT, DB_COL_NAME, DB_COL_CAPTION, DB_COL_URL, DB_COL_LOCATION, DB_COL_EMAIL, DB_COL_PHONE, DB_COL_PATH_ORIG, DB_COL_PATH_PROC, DB_COL_PATH_TH, DB_COL_CART_ITEM_STREAM_SRV_ID, DB_COL_CART_ITEM_SRV_ID, DB_COL_CART_ITEM_QUANTITY, DB_COL_CART_COUPON_CODE, DB_COL_CART_CART_ISOPEN, DB_COL_FOREIGN_KEY};
	
	// DB Record Types
	
	public static String DB_RECORD_TYPE_STREAM = "RECORD_STREAM";
	public static String DB_RECORD_TYPE_ITEM = "RECORD_ITEM";
	public static String DB_RECORD_TYPE_PICTURE = "RECORD_PICTURE";
	public static String DB_RECORD_TYPE_ATTACHMENT = "RECORD_ATTACHMENT";
	public static String DB_RECORD_TYPE_URL = "RECORD_URL";
	public static String DB_RECORD_TYPE_LOCATION = "RECORD_LOCATION";
	public static String DB_RECORD_TYPE_CONTACT = "RECORD_CONTACT";
	public static String DB_RECORD_TYPE_CART = "RECORD_CART";
	public static String DB_RECORD_TYPE_CART_ITEM = "RECORD_CART_ITEM";
	public static String DB_RECORD_TYPE_MY_COUNTRY = "RECORD_MY_COUNTRY";
	public static String DB_RECORD_TYPE_MY_STATE = "RECORD_MY_STATE";
	public static String DB_RECORD_TYPE_MY_CITY = "RECORD_MY_CITY";
	public static String DB_RECORD_TYPE_MY_PHONE = "RECORD_MY_PHONE";
	public static String DB_RECORD_TYPE_MY_NAME = "RECORD_MY_NAME";
	public static String DB_RECORD_TYPE_MY_ADDRESS = "RECORD_MY_ADDRESS";
	public static String DB_RECORD_TYPE_MY_PINCODE = "RECORD_MY_PINCODE";
	public static String DB_RECORD_TYPE_NOTIF = "RECORD_NOTIF";
	public static String DB_RECORD_TYPE_DISCOUNT = "RECORD_DISCOUNT";
	public static String DB_RECORD_TYPE_COUPON = "RECORD_COUPON";
	public static String DB_RECORD_TYPE_TAX_1 = "RECORD_TAX_1";
	public static String DB_RECORD_TYPE_TAX_2 = "RECORD_TAX_2";
	public static String DB_RECORD_TYPE_MESSAGESTREAM_PUSH = "MESSAGE_PUSH";
	public static String DB_RECORD_VALUE_CART_OPEN = "yes";
	public static String DB_RECORD_VALUE_CART_CLOSED = "no";
	public static String DB_DISCOUNT_TYPE_FLAT = "FLAT";
	public static String DB_DISCOUNT_TYPE_PERCENTAGE = "PERCENTAGE";
	

	// Threads
	
	public static String TH_NAME_LOCATIONS_COUNTRIES = "countries";
	public static String TH_NAME_LOCATIONS_STATES = "states";
	public static String TH_NAME_LOCATIONS_CITIES = "cities";
	public static String TH_NAME_CART = "cart";
	public static String TH_NAME_ORDER = "order";
	public static String TH_NAME_ACCOUNT_CREATE = "account";
	public static String TH_NAME_APPS_USING = "appsusing";
	public static String TH_NAME_PASSWORD_RESET = "password_reset";
	public static String TH_NAME_PASSWORD_CHANGE = "password_change";
	public static String TH_NAME_LOGIN = "login";
	public static String TH_NAME_CONFIRM_ORDER = "confirmorder";
	public static String TH_NAME_CANCEL_ORDER = "cancelorder";
	public static String TH_NAME_VERIFY_LOGIN = "verify_login";
	public static String TH_NAME_ORDERS_GET = "get_orders";
	public static String TH_NAME_LOAD_MORE = "load_more";
	public static String TH_NAME_LOAD_MORE_CHECKER = "load_more_checker";
	public static String TH_NAME_VALIDATE_COUPON = "validate_coupon";
	
	// Thread Constants
	
	public static String TH_STATE_TERM = "TERMINATED";
	public static int TH_CHECKER_DURATION = 200;
	public static int TH_SPLASH_MIN_DURATION = 3000;
	
	// Messages

	public static String MSG_ORDERS_NOITEMS = "You haven't completed any orders yet!";
	public static String MSG_ORDERS_END = "No more orders!";
	public static String MSG_CART_NOITEMS = "Cart is empty!";
	public static String MSG_CART_CONFIRM_REMOVE = "Are you sure you want to remove this item from the cart?";
	public static String MSG_CART_EXPIRED = "Cart has expired!";
	public static String MSG_CART_ITEM_LIMIT = "Maximum quantity limit for this item has been reached!";
	public static String MSG_CART_DISCONNECTED = "Cart cannot be accessed offline. Please check your Internet connection!";
	public static String MSG_COUPON_INVALID_LOCATION = "This coupon is not available at your location!";
	public static String MSG_COUPON_INVALID = "Coupon is invalid!";
	public static String MSG_COUPON_BOOKING_INVALID = "Coupon cannot be applied for items with booking amounts!";
	public static String MSG_COUPON_DOUBLE_NOT_ALLOWED = "This coupon can only be applied to non-discounted items!";
	public static String MSG_COUPON_MIN_PURCHASE_LIMIT = "This coupon is applicable for purchases above INR ";
	public static String MSG_DISCONNECTED = "Cannot be accessed offline. Please check your Internet connection!";
	public static String MSG_ORDERS_DISCONNECTED = "Orders cannot be accessed offline. Please check your Internet connection!";
	public static String MSG_LOCATIONS_DISCONNECTED = "Locations cannot be accessed offline. Please check your Internet connection!";
	public static String MSG_BLANK_FIELDS = "No fields can be left blank!";
	public static String MSG_CONFIRM = "Are you sure?";
	public static String MSG_PHONE_INVALID = "Please provide a valid phone number!";
	public static String MSG_PINCODE_INVALID = "Please provide a valid pincode number!";
	public static String MSG_PASSWORDS_NOT_MATCHING = "Passwords are not matching!";
	public static String MSG_PASSWORDS_CHANGE_SUCCESS = "Password has been changed successfully!";
	public static String MSG_PASSWORDS_CHANGE_FAILURE = "Password could not be changed! Please verify credentials..";
	public static String MSG_PASSWORDS_INVALID = "New password is invalid!";
	public static String MSG_ZONCON_PASSWORD_RESET_SUCCESS = "Password has been reset successfully! Please login again";
	public static String MSG_ZONCON_PASSWORD_RESET_ERROR = "Either the code has expired or you have entered an incorrect email address!";
	public static String MSG_ZONCON_ACCOUNT_NOT_EXISTS = "Account for the given email does not exist!";
	public static String MSG_ZONCON_ACCOUNT_SUCCESS = "Account created successfully! Now please login with the same credentials.";
	public static String MSG_ZONCON_ACCOUNT_DUPLICATE = "Account already exists for this email address!";
	public static String MSG_INVALID_EMAIL = "Invalid Email Address!";
	public static String MSG_INVALID_EMAIL_PASSWORD = "Invalid Email Address or Password! Password should be minimum 8 characters";
	public static String MSG_LOGIN_FAIL = "Email Address or Password is incorrect!";
	public static String MSG_ORDER_SUCCESS = "Your order has been placed successfully! Thank you for shopping with us.";
	public static String MSG_ORDER_FAILURE = "Your order could not be placed! Please try again.";
	public static String MSG_VERSION_UPDATE = "Please update your app to the latest version from the Play Store! If you haven't yet received a software update, please wait for a few hours until it becomes available.";
	public static String MSG_YES = "Yes";
	public static String MSG_NO = "No";
	public static String MSG_OK = "OK";
	public static String MSG_PRODUCT_NOT_AVAILABLE = "This product is not available at your location!";

	// Order Status

	public static String ORDER_PROCESSING = "PROCESSING";
	public static String ORDER_CANCELLED = "CANCELLED";
	public static String ORDER_COMPLETE = "COMPLETE";

	// Splash

	public static int SPLASH_DURATION = 3000;

	// Loading

	public static int LOADING_PIC_COUNT = 3;


	// Values

	public static int MAX_CART_QUANTITY = 100;
	public static String[] ITEMS_LIST = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

	// Cart

	public static long TIMEOUT_CART_CLEAR = 86400000;

	// Market

	public static String MARKET_URL_PREFIX_1 = "market://details?id=";
	public static String MARKET_URL_PREFIX_2 = "http://play.google.com/store/apps/details?id=";

	// Market

	public static String SHARE_SUBJECT = "ZonCon ECommerce App";
	public static String SHARE_CONTENT = "Thank you for downoading the ZonCon Ecommerce mobile app!";

	// Notifications

	public static int NOTIF_SLEEP_TIME = 300000;

	// Load More

	public static String LOAD_MORE_CAPTION = "LOAD MORE";
	public static String LOAD_MORE_CAPTION_LOADING = "Loading...";
	public static String LOAD_MORE_CAPTION_END = "REACHED THE END";

	// Body

	public static int BODY_TOP_MARGIN_DP = 70;
	public static int BODY_BOTTOM_MARGIN_DP = 0;


	public static String SHARE_ITEM_POSTFIX = "Brought to you by ZonCon Ecommerce!";

	public static String SCREEN_SHOP_CATEGORIES = "ShopCategories";
	public static String SCREEN_SHOP_ITEM_LIST = "ShopItemList";
	public static String SCREEN_SHOP_ITEM_DETAILS = "ShopItemDetails";
	public static String SCREEN_ABOUT_ITEM_LIST = "AboutItemList";
	public static String SCREEN_ABOUT_ITEM_DETAILS = "AboutItemDetails";
	public static String SCREEN_ALERT_ITEM_LIST = "AlertItemList";
	public static String SCREEN_ALERT_ITEM_DETAILS = "AlertItemDetails";
	public static String SCREEN_POLICY_ITEM_LIST = "PolicyItemList";
	public static String SCREEN_POLICY_ITEM_DETAILS = "PolicyItemDetails";
	public static String SCREEN_LOADING = "Splash";
	public static String SCREEN_LOCATION = "Location";
	public static String SCREEN_STREAMS = "Stream";
	public static String SCREEN_ACCOUNT = "Account";
	public static String SCREEN_CART = "Cart";
	public static String SCREEN_CREATE_ACCOUNT = "CreateAccount";
	public static String SCREEN_LOGIN_ACCOUNT = "LoginAccount";
	public static String SCREEN_FORGOT = "Forgot";
	public static String SCREEN_RESET = "Reset";
	public static String SCREEN_SHIPPING = "Shipping";
	public static String SCREEN_CHECKOUT = "Checkout";
	public static String SCREEN_CONFIRM = "Confirm";
	public static String SCREEN_PAYMENT = "Payment";
	public static String SCREEN_ORDER_LIST = "Orderlist";
	public static String SCREEN_ORDER_DETAILS = "Orderdetails";

	public static String STREAM_NAME_VERSIONS = "Versions";

	public static String TOKEN_KEY = "token";
	public static String EMAIL_KEY = "email";
	public static String MUSIC_KEY = "music";

	public static String DEEP_LINK = "deeplink";
	public static String DEEP_LINK_VALUE_CART = "cart";
	public static String DEEP_LINK_VALUE_ALERTS = "alerts";
	public static String DEEP_LINK_VALUE_ACCOUNT = "account";
	public static String DEEP_LINK_VALUE_PRODUCT_LIST = "productlist";
	public static String DEEP_LINK_VALUE_POLICIES = "policies";
	public static String DEEP_LINK_VALUE_DEV = "dev";

	public static String DEEP_LINK_PARAM = "deeplinkparam";	

	public static int SOCKET_TIMEOUT = 20000;
	public static int HTTP_TIMEOUT = 20000;


	public static int NUM_INIT_STREAMS = 6;

	public static int MIN_PURCHASE_LIMIT = 500;
	public static int MAX_PURCHASE_LIMIT = 500000;
	public static String MIN_PURCHASE_LIMIT_ERROR = "Minimum purchase limit is " + MainActivity.SYMBOL_RUPEE + " " + MIN_PURCHASE_LIMIT;
	public static String MAX_PURCHASE_LIMIT_ERROR = "Maximum purchase limit is " + MainActivity.SYMBOL_RUPEE + " " + MAX_PURCHASE_LIMIT;

	public Boolean IS_CONNECTED = false;

	public static final String MyPREFERENCES = "MyPrefs" ;

	public ZonConApplication app;

	public static ProgressDialog pageLoad;

	public Context context;
	public SharedPreferences sharedPreferences;
	public DbConnection dbC;
	public FragmentManager fragMgr;

	protected LinearLayout llHead;
	protected ImageView ivHeadTitle, ivHeadMenu, ivHeadCart;
	protected TextView tvCartNotif;
	protected FrameLayout flBody;

	public Typeface tf;

	protected Boolean RUN_FLAG = false;
	public Boolean IS_CLICKED = false;
	public Boolean IS_CLICKABLE_FRAME = true;
	public Boolean IS_CLICKABLE_FRAGMENT = true;
	public int CART_NOTIF_DURATION = 10000;
	public String myCountry = null, myState = null, myCity = null;
	public String myCountryId = null, myStateId = null, myCityId = null;
	public String lastCreatedActivity = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
			// call something for API Level 11+

			try {

				display.getSize(size);
				SCREEN_WIDTH = size.x;
				SCREEN_HEIGHT = size.y;

			} catch (java.lang.NoSuchMethodError ignore) { // Older device
				SCREEN_WIDTH = display.getWidth();
				SCREEN_HEIGHT = display.getHeight();
			}

		} else {

			SCREEN_WIDTH = display.getWidth();
			SCREEN_HEIGHT = display.getHeight();

		}

		String packName = getApplicationContext().getPackageName();
		STORAGE_PATH = Environment.getExternalStorageDirectory() + "/" + packName;
		File dir = new File(STORAGE_PATH);
		if (!dir.exists()) {
			dir.mkdir();
		}

		storeContext();
		storeClassVariables();
		initUIHandles();
		formatUI();
		initUIListeners();

		app.context = MainActivity.this;

		if(getIntent().getExtras() != null) {

			MLog.log("Extras not null");

			String deepLink = getIntent().getExtras().getString(MainActivity.DEEP_LINK);

			if(deepLink != null) {

				if(deepLink.length() > 0) {

					if(deepLink.contains(MainActivity.DEEP_LINK_VALUE_PRODUCT_LIST)) {

						fragMgr.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
						String _idStream = getIntent().getExtras().getString(MainActivity.DEEP_LINK_PARAM);
						FragmentShopItemsList frag = new FragmentShopItemsList();
						frag.idStream = Integer.parseInt(_idStream);
						fragMgr.beginTransaction()
						.add(R.id.ll_body, frag)
						.addToBackStack(null)
						.commit();

					} else if(deepLink.contains(MainActivity.DEEP_LINK_VALUE_CART)) {

						loadCart();

					} else if(deepLink.contains(MainActivity.DEEP_LINK_VALUE_ALERTS)) {

						loadAlerts();

					} else if(deepLink.contains(MainActivity.DEEP_LINK_VALUE_ACCOUNT)) {

						loadMenu();

					} else if(deepLink.contains(MainActivity.DEEP_LINK_VALUE_POLICIES)) {

						HashMap<String, String> map = new HashMap<String, String>();
						map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_STREAM);
						ArrayList<HashMap<String, String>> records =  null;
						if(dbC.isOpen()) {
							dbC.isAvailale();
							records =  dbC.retrieveRecords(map);
						}

						if(records.size() >= 2) {

							map = records.get(1);

							FragmentShopItemsList fragment = new FragmentShopItemsList();
							fragment.idStream = Integer.parseInt(map.get(MainActivity.DB_COL_SRV_ID));
							fragMgr.beginTransaction()
							.add(R.id.ll_body, fragment)
							.addToBackStack(null)
							.commit();

						}

					} else if(deepLink.contains(MainActivity.DEEP_LINK_VALUE_DEV)) {

						FragmentDev fragment = new FragmentDev();	
						fragMgr.beginTransaction()
						.add(R.id.ll_body, fragment)
						.addToBackStack(null)
						.commit();

					} 

				}

			}

		} else {
			populateLocation();
		}

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		GoogleAnalytics.getInstance(MainActivity.this).reportActivityStop(this);
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//GoogleAnalytics.getInstance(this).getLogger().setLogLevel(LogLevel.VERBOSE);
		GoogleAnalytics.getInstance(MainActivity.this).reportActivityStart(this);
		
		MLog.log("Resuming..." + app.stateOfLifeCycle);
		MLog.log("Resuming..." + app.wasInBackground + "");
		MLog.log("Resuming..." + app.ENABLE_SYNC + "");

		if(!app.PREVENT_CLOSE_AND_SYNC && !app.ISGOINGOUTOFAPP) {

			loadSplash();

		} else {

			// Do Nothing

		}

		if(app.ISGOINGOUTOFAPP) {

			app.ISGOINGOUTOFAPP = false;

		}

		/*
		if(app.wasInBackground) {

			if(app.ENABLE_SYNC) {

				loadSplash();

			} else {

				// Do Nothing

			}

		} else {

			if(getIntent().getExtras() == null) {
				if(app.ENABLE_SYNC) {
					loadSplash();
				}
			} else {
				getIntent().removeExtra(MainActivity.DEEP_LINK);
				getIntent().removeExtra(MainActivity.DEEP_LINK_PARAM);
			}

		}*/

	}

	public void clearToken() {

		Editor editor = sharedPreferences.edit();
		editor.remove(TOKEN_KEY);
		editor.commit();

	}

	public void clearEmail() {

		Editor editor = sharedPreferences.edit();
		editor.remove(EMAIL_KEY);
		editor.commit();
	}

	public void updateEmail(String email) {

		Editor editor = sharedPreferences.edit();
		editor.remove(EMAIL_KEY);
		editor.putString(EMAIL_KEY, email);
		editor.commit();

	}

	public void updateToken(String token) {

		Editor editor = sharedPreferences.edit();
		editor.remove(TOKEN_KEY);
		editor.putString(TOKEN_KEY, token);
		editor.commit();

	}

	public String getEmail() {

		return sharedPreferences.getString(EMAIL_KEY, "");

	}

	public String getToken() {

		return sharedPreferences.getString(TOKEN_KEY, "");

	}

	public String getGold22Rate(int index) {

		return null;

	}

	public void populateLocation() {

		HashMap<String, String> mapCountry = new HashMap<String, String>();
		mapCountry.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_COUNTRY);

		HashMap<String, String> mapState = new HashMap<String, String>();
		mapState.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_STATE);

		HashMap<String, String> mapCity = new HashMap<String, String>();
		mapCity.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_CITY);

		ArrayList<HashMap<String, String>> recordsCountry = null, recordsState = null, recordsCity = null;

		if(dbC.isOpen()) {
			dbC.isAvailale();
			recordsCountry = dbC.retrieveRecords(mapCountry);
			recordsState = dbC.retrieveRecords(mapState);
			recordsCity = dbC.retrieveRecords(mapCity);
		}

		if(recordsCountry != null && recordsState != null && recordsCity != null) {

			if(recordsCountry.size() > 0 && recordsState.size() > 0 && recordsCity.size() > 0) {

				mapCountry = recordsCountry.get(0);
				mapState = recordsState.get(0);
				mapCity = recordsCity.get(0);

				myCountry = mapCountry.get(MainActivity.DB_COL_NAME);
				myCountryId = mapCountry.get(MainActivity.DB_COL_SRV_ID);
				myState = mapState.get(MainActivity.DB_COL_NAME);
				myStateId = mapState.get(MainActivity.DB_COL_SRV_ID);
				myCity = mapCity.get(MainActivity.DB_COL_NAME);
				myCityId = mapCity.get(MainActivity.DB_COL_SRV_ID);

				MLog.log("My Country " + myCountry + " " + myCountryId);
				MLog.log("My State " + myState + " " + myStateId);
				MLog.log("My Cityy " + myCity + " " + myCityId);

			} else {

				myCity = null;
				myCityId = null;
				myCountry = null;
				myCountryId = null;
				myState = null;
				myStateId = null;

			}
		} else {

			myCity = null;
			myCityId = null;
			myCountry = null;
			myCountryId = null;
			myState = null;
			myStateId = null;

		}
	}

	public void loadSplash() {

		try {

			app.ENABLE_SYNC = true;
			app.PREVENT_CLOSE_AND_SYNC = false;

			populateLocation();
			fragMgr.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			FragmentTransaction fragmentTransaction;

			if(myCity == null) {

				fragmentTransaction = fragMgr.beginTransaction();
				FragmentShippingLocation fragmentLoc = new FragmentShippingLocation();
				fragmentLoc.isBegin = true;
				fragmentTransaction.add(R.id.ll_body, fragmentLoc, MainActivity.SCREEN_LOCATION)
				.addToBackStack(MainActivity.SCREEN_LOCATION)
				.commit();

			} else {


				fragmentTransaction = fragMgr.beginTransaction();
				FragmentLoading fragmentLoc = new FragmentLoading();
				fragmentLoc.isBegin = true;
				fragmentTransaction.add(R.id.ll_body, fragmentLoc, MainActivity.SCREEN_LOADING)
				.addToBackStack(MainActivity.SCREEN_LOADING)
				.commit();


			}

		} catch (IllegalStateException e) {

		}

	}

	public void loadShop() {

		try {

			app.PREVENT_CLOSE_AND_SYNC = false;

			populateLocation();
			fragMgr.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			FragmentTransaction fragmentTransaction;

			if(myCity == null) {

				fragmentTransaction = fragMgr.beginTransaction();
				FragmentShippingLocation fragmentLoc = new FragmentShippingLocation();
				fragmentLoc.isBegin = true;
				fragmentTransaction.add(R.id.ll_body, fragmentLoc, MainActivity.SCREEN_LOCATION)
				.addToBackStack(MainActivity.SCREEN_LOCATION)
				.commit();

			} else {


				fragmentTransaction = fragMgr.beginTransaction();
				FragmentShopCategories fragmentLoc = new FragmentShopCategories();
				fragmentTransaction.add(R.id.ll_body, fragmentLoc, MainActivity.SCREEN_SHOP_CATEGORIES)
				.addToBackStack(MainActivity.SCREEN_SHOP_CATEGORIES)
				.commit();

			}

		}catch(IllegalStateException e) {

		}

	}

	public void loadCart() {

		try {

			app.PREVENT_CLOSE_AND_SYNC = true;

			populateLocation();
			fragMgr.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			FragmentTransaction fragmentTransaction;

			if(myCity == null) {

				fragmentTransaction = fragMgr.beginTransaction();
				FragmentShippingLocation fragmentLoc = new FragmentShippingLocation();
				fragmentLoc.isBegin = true;
				fragmentTransaction.add(R.id.ll_body, fragmentLoc, MainActivity.SCREEN_LOCATION)
				.addToBackStack(MainActivity.SCREEN_LOCATION)
				.commit();

			} else {

				fragmentTransaction = fragMgr.beginTransaction();
				FragmentPaymentCart fragmentLoc = new FragmentPaymentCart();
				fragmentTransaction.add(R.id.ll_body, fragmentLoc, MainActivity.SCREEN_CART)
				.addToBackStack(MainActivity.SCREEN_CART)
				.commit();

			}

		} catch (IllegalStateException e) {

		}

	}


	public void loadAlerts() {

		try {

			app.PREVENT_CLOSE_AND_SYNC = false;

			HashMap<String, String> map = new HashMap<String, String>();
			map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_STREAM);
			ArrayList<HashMap<String, String>> records =  null;
			if(dbC.isOpen()) {
				dbC.isAvailale();
				records =  dbC.retrieveRecords(map);
			}

			if(records.size() > MainActivity.NUM_INIT_STREAMS) {

				for(int i = 0; i < records.size(); i++) {

					map = records.get(i);
					String name = map.get(MainActivity.DB_COL_NAME);

					if(name.toLowerCase().contains("alert")) {

						//fragMgr.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
						FragmentAlertItemsList fragment = new FragmentAlertItemsList();
						fragment.idStream = Integer.parseInt(map.get(MainActivity.DB_COL_SRV_ID));
						fragMgr.beginTransaction()
								.add(R.id.ll_body, fragment, MainActivity.SCREEN_ALERT_ITEM_LIST)
								.addToBackStack(MainActivity.SCREEN_ALERT_ITEM_LIST)
								.commit();

						break;

					}

				}

			}

		} catch (IllegalStateException e) {

		}

	}

	public void loadPolicy() {

		try {

			app.PREVENT_CLOSE_AND_SYNC = false;

			HashMap<String, String> map = new HashMap<String, String>();
			map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_STREAM);
			ArrayList<HashMap<String, String>> records =  null;
			if(dbC.isOpen()) {
				dbC.isAvailale();
				records =  dbC.retrieveRecords(map);
			}

			if(records.size() > MainActivity.NUM_INIT_STREAMS) {

				for(int i = 0; i < records.size(); i++) {

					map = records.get(i);
					String name = map.get(MainActivity.DB_COL_NAME);

					if(name.toLowerCase().contains("terms")) {

						//fragMgr.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
						FragmentPolicyItemsList fragment = new FragmentPolicyItemsList();
						fragment.idStream = Integer.parseInt(map.get(MainActivity.DB_COL_SRV_ID));
						fragMgr.beginTransaction()
								.add(R.id.ll_body, fragment, MainActivity.SCREEN_POLICY_ITEM_LIST)
								.addToBackStack(MainActivity.SCREEN_POLICY_ITEM_LIST)
								.commit();

						break;

					}

				}

			}

		} catch (IllegalStateException e) {

		}

	}

	public void loadAbout() {

		try {

			app.PREVENT_CLOSE_AND_SYNC = false;

			HashMap<String, String> map = new HashMap<String, String>();
			map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_STREAM);
			ArrayList<HashMap<String, String>> records =  null;
			if(dbC.isOpen()) {
				dbC.isAvailale();
				records =  dbC.retrieveRecords(map);
			}

			if(records.size() > MainActivity.NUM_INIT_STREAMS) {

				for(int i = 0; i < records.size(); i++) {

					map = records.get(i);
					String name = map.get(MainActivity.DB_COL_NAME);

					if(name.toLowerCase().contains("about")) {


						//fragMgr.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
						FragmentAboutItemsList fragment = new FragmentAboutItemsList();
						fragment.idStream = Integer.parseInt(map.get(MainActivity.DB_COL_SRV_ID));
						fragMgr.beginTransaction()
								.add(R.id.ll_body, fragment, MainActivity.SCREEN_ABOUT_ITEM_LIST)
								.addToBackStack(MainActivity.SCREEN_ABOUT_ITEM_LIST)
								.commit();

						break;

					}

				}

			}

		} catch (IllegalStateException e) {

		}

	}

	public void loadMenu() {

		try {

			app.PREVENT_CLOSE_AND_SYNC = true;

			populateLocation();
			fragMgr.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			FragmentTransaction fragmentTransaction;

			if(myCity == null) {

				fragmentTransaction = fragMgr.beginTransaction();
				FragmentShippingLocation fragmentLoc = new FragmentShippingLocation();
				fragmentLoc.isBegin = true;
				fragmentTransaction.add(R.id.ll_body, fragmentLoc, MainActivity.SCREEN_LOCATION)
				.addToBackStack(MainActivity.SCREEN_LOCATION)
				.commit();

			} else {

				fragmentTransaction = fragMgr.beginTransaction();
				FragmentMenu fragmentLoc = new FragmentMenu();
				fragmentTransaction.add(R.id.ll_body, fragmentLoc, MainActivity.SCREEN_ACCOUNT)
				.addToBackStack(MainActivity.SCREEN_ACCOUNT)
				.commit();

			}

		} catch (IllegalStateException e) {

		}

	}


	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		int backStackEntryCount = fragMgr.getBackStackEntryCount();
		MLog.log("Back Pressed = " + lastCreatedActivity);
		MLog.log("Back Stack Entry = " + backStackEntryCount);

		if(app.APP_EXIT_ON_BACK) {
			super.onBackPressed();
			finish();
		} else {

			if(backStackEntryCount == 1){

				String fragmentTag = fragMgr.getBackStackEntryAt(fragMgr.getBackStackEntryCount() - 1).getName();
				MLog.log("Back Fragment found " + fragmentTag);
				if(fragmentTag.equals(MainActivity.SCREEN_SHOP_CATEGORIES)) {
					super.onBackPressed();
					finish();
				} else {
					loadShop();
				}

			} else {

				MLog.log("Pressing Back");
				super.onBackPressed();

			}

		}
		IS_CLICKABLE_FRAGMENT = true;

	}

	Handler cartNotifHandler = new Handler() {

		public void handleMessage(Message msg) {

			if(msg.what == 0) {

				tvCartNotif.setVisibility(RelativeLayout.GONE);

			} else {

				tvCartNotif.setText(msg.what + "");
				tvCartNotif.setVisibility(RelativeLayout.VISIBLE);

			}

		};

	};

	Handler alertNotifHandler = new Handler() {

		public void handleMessage(Message msg) {

			if(msg.what == 0) {

			} else {

			}

		};

	};

	public void clearAlertNotification() {

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_NOTIF);
		if(dbC.isOpen()) {
			dbC.isAvailale();
			ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();
			list = dbC.retrieveRecords(map);
			if(list.isEmpty()) {
			} else {

				if(list.size() > 1) {

					dbC.deleteRecord(map);

				} else {

					HashMap<String, String> mapR = list.get(0);
					int isOpen = Integer.parseInt(mapR.get(MainActivity.DB_COL_CART_CART_ISOPEN));
					if(isOpen == 0) {
						mapR.put(MainActivity.DB_COL_CART_CART_ISOPEN, "1");
						dbC.updateRecord(mapR, map);
						alertNotifHandler.sendEmptyMessage(0);
					} 

				}

			}
		}

	}

	public void showAlertNotification() {


		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_NOTIF);
		if(dbC.isOpen()) {
			dbC.isAvailale();
			ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();
			list = dbC.retrieveRecords(map);
			if(list.isEmpty()) {
				Message msg = new Message();
				alertNotifHandler.sendEmptyMessage(0);
				//show notification
			} else {

				if(list.size() > 1) {

					dbC.deleteRecord(map);

				} else {

					HashMap<String, String> mapR = list.get(0);
					int isOpen = Integer.parseInt(mapR.get(MainActivity.DB_COL_CART_CART_ISOPEN));
					if(isOpen == 0) {
						alertNotifHandler.sendEmptyMessage(1);
					} else {
						alertNotifHandler.sendEmptyMessage(0);
					}

				}

			}
		}

	}

	public void showCartNotification() {

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

				//cartNotifHandler.sendEmptyMessage(1);
				cartNotifHandler.sendEmptyMessage(recordsItems.size());

			} else {

				cartNotifHandler.sendEmptyMessage(0);

			}

		} else {

			cartNotifHandler.sendEmptyMessage(0);

		}

	}



	int pixelsToDp(int pixels) {

		DisplayMetrics metrics = MainActivity.this.getResources().getDisplayMetrics();
		float dp = pixels;
		float fpixels = metrics.density * dp;
		return pixels = (int) (fpixels + 0.5f);

	}

	public int dpToPixels(int dp) {
		float density = getApplicationContext().getResources().getDisplayMetrics().density;
		return Math.round((float) dp * density);
	}

	float pixelsToSp(Context context, float px) {
		float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
		//Log.d(TAG, "Scaled Density " + scaledDensity + "");
		return px/scaledDensity;
	}

	float spToPixels(Context context, float px) {
		float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
		//Log.d(TAG, "Scaled Density " + scaledDensity + "");
		return px*scaledDensity;
	}

	public Boolean checkIfExistsInExternalStorage(String fileName) {

		String filePath = STORAGE_PATH + "/" + fileName;
		File file = new File(filePath);
		if(file.exists()) {

			if(file.length() > 0) {
				return file.exists();
			} else {

				file.delete();
				return false;

			}

		}

		return file.exists();

	}

	@Override
	public void storeContext() {
		// TODO Auto-generated method stub
		context = MainActivity.this;
		app = (ZonConApplication)getApplicationContext();
	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub

		ivHeadMenu.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (!IS_CLICKED) {

					IS_CLICKED = true;
					loadMenu();

				}
			}

		});

		ivHeadCart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(!IS_CLICKED) {

					IS_CLICKED = true;
					loadCart();

				} 

			}

		});

	}

	@Override
	public void initUIHandles() {
		// TODO Auto-generated method stub
		llHead = (LinearLayout)findViewById(R.id.ll_head);
		ivHeadTitle = (ImageView)findViewById(R.id.iv_head_title);
		ivHeadCart = (ImageView)findViewById(R.id.iv_head_cart);
		ivHeadMenu = (ImageView)findViewById(R.id.iv_head_menu);

		tvCartNotif = (TextView)findViewById(R.id.tv_cart_notif);

		flBody = (FrameLayout)findViewById(R.id.ll_body);

		tf = Typeface.createFromAsset(getAssets(), "icomoon.ttf");

	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub

		if(isTablet(getApplicationContext())) {

			TEXT_SIZE_TILE = (char)(18);
			TEXT_SIZE_TITLE = (char)(22);

		}

	}

	public void highlightFooter(int index) {


	}

	public void showConnected(Boolean value) {

	}

	@Override
	public void storeClassVariables() {
		// TODO Auto-generated method stub
		dbC = app.conn;
		fragMgr = getFragmentManager();

	}

	public Handler handlerLoading = new Handler() {

		public void handleMessage(android.os.Message msg) {

			if(msg.what == 0) {

				hideLoading();

			} else {

				showLoading();

			}

		};

	};

	public void hideHeaderFooter() {

		llHead.setVisibility(RelativeLayout.GONE);
		RelativeLayout.LayoutParams rParams = (RelativeLayout.LayoutParams) flBody.getLayoutParams();
		rParams.setMargins(0, 0, 0, 0);


	}

	public void showHeaderFooter() {

		llHead.setVisibility(RelativeLayout.VISIBLE);
		RelativeLayout.LayoutParams rParams = (RelativeLayout.LayoutParams) flBody.getLayoutParams();
		rParams.setMargins(0, dpToPixels(MainActivity.BODY_TOP_MARGIN_DP), 0, dpToPixels(MainActivity.BODY_BOTTOM_MARGIN_DP));


	}

	public void showLoading() {

		pageLoad = new ProgressDialog(MainActivity.this);
		pageLoad.setTitle("Loading...");
		pageLoad.setMessage("Please wait");
		pageLoad.setCanceledOnTouchOutside(false);
		pageLoad.show();

	}

	public void hideLoading() {

		pageLoad.dismiss();

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRunFlag(Boolean value) {
		// TODO Auto-generated method stub
		RUN_FLAG = value;
	}

	protected Handler threadHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {




		}

	};

	public boolean isTablet(Context context) {
		boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
		boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
		MLog.log("IS TABLET = " + (xlarge || large));
		return (xlarge || large);
	}


	public Boolean applyCouponToCart(String code, Boolean containsDiscount, String purchasePrice, String discountDifference) {

		MLog.log("Current Coupon = " + code);

		CouponRecord cr = getCouponRecordFromCode(code);

		if(cr != null) {

			MLog.log("Code " + cr.code);
			MLog.log("Double " + cr.allowDoubleDiscouting);

			MLog.log("Purchase=" + purchasePrice);
			MLog.log("Min Purchase=" + cr.minPurchase);

			if(cr.minPurchase != null) {

				if(cr.minPurchase.length() > 0) {

					if(Double.parseDouble(purchasePrice) < Double.parseDouble(cr.minPurchase)) {

						AlertDialog.Builder alert  = new AlertDialog.Builder(context);
						alert.setMessage(MainActivity.MSG_COUPON_MIN_PURCHASE_LIMIT + cr.minPurchase);
						alert.setTitle("Alert");
						alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								//dismiss the dialog
							}
						});
						alert.create().show();
						return false;

					}

				}

			}

			if(!cr.allowDoubleDiscouting.equals("1") && containsDiscount) {

				AlertDialog.Builder alert  = new AlertDialog.Builder(context);
				alert.setMessage(MainActivity.MSG_COUPON_DOUBLE_NOT_ALLOWED);
				alert.setTitle("Error");
				alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						//dismiss the dialog
					}
				});
				alert.create().show();
				return false;

			}

			HashMap<String, String> map = new HashMap<String, String>();
			map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_CART);
			ArrayList<HashMap<String, String>> recordsCart = null;
			if(dbC.isOpen()) {
				dbC.isAvailale();
				recordsCart = dbC.retrieveRecords(map);
			}

			if(recordsCart.size() > 0) {

				HashMap<String, String> mapExisting = recordsCart.get(0);
				mapExisting.put(MainActivity.DB_COL_DISCOUNT, cr.id);
				if(dbC.isOpen()) {
					dbC.isAvailale();
					dbC.updateRecord(mapExisting, map);
					return true;
				}

			}

		}

		AlertDialog.Builder alert  = new AlertDialog.Builder(context);
		alert.setMessage(MainActivity.MSG_COUPON_INVALID_LOCATION);
		alert.setTitle("Alert");
		alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//dismiss the dialog
			}
		});
		alert.create().show();

		return false;
	}

	public void removeAppliedCouponToCart() {

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_CART);
		ArrayList<HashMap<String, String>> recordsCart = null;
		if(dbC.isOpen()) {
			dbC.isAvailale();
			recordsCart = dbC.retrieveRecords(map);
		}

		if(recordsCart.size() > 0) {

			HashMap<String, String> mapExisting = recordsCart.get(0);
			mapExisting.put(MainActivity.DB_COL_DISCOUNT, "");
			if(dbC.isOpen()) {
				dbC.isAvailale();
				dbC.updateRecord(mapExisting, map);
			}

		}

	}


	public CouponRecord getCouponAppliedToCart() {

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_CART);
		ArrayList<HashMap<String, String>> recordsCart = null;
		if(dbC.isOpen()) {
			dbC.isAvailale();
			recordsCart = dbC.retrieveRecords(map);
		}

		if(recordsCart.size() > 0) {

			HashMap<String, String> mapExisting = recordsCart.get(0);
			String idCoupon = mapExisting.get(MainActivity.DB_COL_DISCOUNT);

			if(idCoupon != null) {

				if(idCoupon.length() > 0) {

					return getCouponRecordFromId(idCoupon);

				}

			}


		}

		return null;
	}

	public CouponRecord getCouponRecordFromCode(String code) {

		CouponRecord dr = null;

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_COUPON);
		ArrayList<HashMap<String, String>> recordsCoupons = null;
		if(dbC.isOpen()) {
			dbC.isAvailale();
			recordsCoupons = dbC.retrieveRecords(map);
		}

		for(int j = 0; j < recordsCoupons.size(); j++) {

			map = recordsCoupons.get(j);

			String discountCode = map.get(MainActivity.DB_COL_NAME);
			String discountType = map.get(MainActivity.DB_COL_TITLE);
			String discountValue = map.get(MainActivity.DB_COL_PRICE);
			String idDiscount = map.get(MainActivity.DB_COL_SRV_ID);
			String allowDouble = map.get(MainActivity.DB_COL_CAPTION);
			String maxVal = map.get(MainActivity.DB_COL_EXTRA_1);
			String minPurchase = map.get(MainActivity.DB_COL_EXTRA_2);

			if(code.equals(discountCode)) {

				MLog.log("Coupon Found");
				dr = new CouponRecord();
				dr.code = discountCode;
				dr.type = discountType;
				dr.value = discountValue;
				dr.id = idDiscount;
				dr.allowDoubleDiscouting = allowDouble;
				dr.maxVal = maxVal;
				dr.minPurchase = minPurchase;

				MLog.log("Coupon Found " + dr.code + " " + dr.type + " " + dr.value + " " + dr.id);

				return dr;

			}

		}

		return dr;

	}

	public CouponRecord getCouponRecordFromId(String discountId) {

		CouponRecord dr = null;

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_COUPON);
		ArrayList<HashMap<String, String>> recordsCoupons = null;
		if(dbC.isOpen()) {
			dbC.isAvailale();
			recordsCoupons = dbC.retrieveRecords(map);
		}

		for(int j = 0; j < recordsCoupons.size(); j++) {

			HashMap<String, String> map1 = null;

			map1 = recordsCoupons.get(j);

			String discountCode = map1.get(MainActivity.DB_COL_NAME);
			String discountType = map1.get(MainActivity.DB_COL_TITLE);
			String discountValue = map1.get(MainActivity.DB_COL_PRICE);
			String idDiscount = map1.get(MainActivity.DB_COL_SRV_ID);
			String allowDouble = map1.get(MainActivity.DB_COL_CAPTION);
			String maxVal = map1.get(MainActivity.DB_COL_EXTRA_1);
			String minPurchase = map1.get(MainActivity.DB_COL_EXTRA_2);

			if(idDiscount.equals(discountId)) {

				MLog.log("Coupon Found");
				dr = new CouponRecord();
				dr.code = discountCode;
				dr.type = discountType;
				dr.value = discountValue;
				dr.allowDoubleDiscouting = allowDouble;
				dr.id = discountId;
				dr.maxVal = maxVal;
				dr.minPurchase = minPurchase;

				MLog.log("Coupon Found " + dr.code + " " + dr.type + " " + dr.value + " " + dr.id + " " + minPurchase + " " + maxVal);

				return dr;

			}

		}

		return dr;

	}


	public DiscountRecord getDiscountRecordFromId(String discountId) {

		DiscountRecord dr = null;

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_DISCOUNT);
		ArrayList<HashMap<String, String>> recordsDiscounts = null;
		if(dbC.isOpen()) {
			dbC.isAvailale();
			recordsDiscounts = dbC.retrieveRecords(map);
		}

		for(int j = 0; j < recordsDiscounts.size(); j++) {

			map = recordsDiscounts.get(j);

			String discountCode = map.get(MainActivity.DB_COL_NAME);
			String discountType = map.get(MainActivity.DB_COL_TITLE);
			String discountValue = map.get(MainActivity.DB_COL_PRICE);
			String idDiscount = map.get(MainActivity.DB_COL_SRV_ID);

			if(idDiscount.equals(discountId)) {

				MLog.log("Discount Found");
				dr = new DiscountRecord();
				dr.code = discountCode;
				dr.type = discountType;
				dr.value = discountValue;
				dr.id = discountId;
				MLog.log("Discount Found " + dr.code + " " + dr.type + " " + dr.value + " " + dr.id);
				return dr;

			}

		}

		return dr;

	}

	public Boolean checkVersionCompat() {

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_STREAM);
		map.put(MainActivity.DB_COL_NAME, MainActivity.STREAM_NAME_VERSIONS);

		ArrayList<HashMap<String, String>> records = null;
		if(dbC.isOpen()) {
			dbC.isAvailale();
			records = dbC.retrieveRecords(map);
		}

		MLog.log("Version Records=" + records.size());

		if(records.size() > 0) {

			map = records.get(0);
			String streamKey = map.get(MainActivity.DB_COL_ID);

			MLog.log("Version Records key =" + streamKey);

			map = new HashMap<String, String>();
			map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_ITEM);
			map.put(MainActivity.DB_COL_FOREIGN_KEY, streamKey);
			if(dbC.isOpen()) {
				dbC.isAvailale();
				records = dbC.retrieveRecords(map);
			}

			MLog.log("Version Records Items key =" + records.size());

			if(records.size() > 0) {

				map = records.get(0);
				String versionWeb = map.get(MainActivity.DB_COL_TITLE);
				MLog.log("Web Version = " + versionWeb);
				try {

					String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
					Double dVersionWeb = Double.valueOf(versionWeb);
					Double dVersion = Double.valueOf(version);
					if(dVersion >= dVersionWeb) {

						return true;

					} else {

						return false;

					}

				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			} 
		}


		return false;

		//activity.getFragmentManager().beginTransaction().remove(FragmentLoading.this).commit();

	}

	public void addNewCoupon(CouponRecord cr, String timestamp) {

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_COUPON);

		if(dbC.isOpen()) {
			dbC.isAvailale();
			dbC.deleteRecord(map);
		}

		map = new HashMap<String, String>();
		map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_COUPON);
		map.put(MainActivity.DB_COL_NAME, cr.code);
		map.put(MainActivity.DB_COL_TITLE, cr.type);
		map.put(MainActivity.DB_COL_TIMESTAMP, timestamp);
		map.put(MainActivity.DB_COL_PRICE, cr.value);
		map.put(MainActivity.DB_COL_CAPTION, cr.allowDoubleDiscouting);
		map.put(MainActivity.DB_COL_SRV_ID, cr.id);
		map.put(MainActivity.DB_COL_EXTRA_1, cr.maxVal);
		map.put(MainActivity.DB_COL_EXTRA_2, cr.minPurchase);
		dbC.insertRecord(map);

	}

	public TaxRecord getTax1() {

		TaxRecord taxRecord = null;

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_TAX_1);

		if(dbC.isOpen()) {
			dbC.isAvailale();
			ArrayList<HashMap<String, String>> list = dbC.retrieveRecords(map);
			if(list.size() > 0) {
				map = list.get(0);
				taxRecord = new TaxRecord();
				taxRecord.label = map.get(MainActivity.DB_COL_TITLE);
				taxRecord.value = map.get(MainActivity.DB_COL_SUB);
			}
		}

		return taxRecord;

	}

	public TaxRecord getTax2() {

		TaxRecord taxRecord = null;

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_TAX_2);

		if(dbC.isOpen()) {
			dbC.isAvailale();
			ArrayList<HashMap<String, String>> list = dbC.retrieveRecords(map);
			if(list.size() > 0) {
				map = list.get(0);
				taxRecord = new TaxRecord();
				taxRecord.label = map.get(MainActivity.DB_COL_TITLE);
				taxRecord.value = map.get(MainActivity.DB_COL_SUB);
			}
		}

		return taxRecord;

	}

}
