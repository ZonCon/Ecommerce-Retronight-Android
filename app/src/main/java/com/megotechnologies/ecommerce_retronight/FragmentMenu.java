package com.megotechnologies.ecommerce_retronight;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.megotechnologies.ecommerce_retronight.account.FragmentAccountForgotPassword;
import com.megotechnologies.ecommerce_retronight.account.FragmentAccountLogin;
import com.megotechnologies.ecommerce_retronight.account.FragmentAccountResetPassword;
import com.megotechnologies.ecommerce_retronight.account.FragmentShippingInfo;
import com.megotechnologies.ecommerce_retronight.account.FragmentShippingLocation;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCFragmentLifecycle;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCRunnable;
import com.megotechnologies.ecommerce_retronight.payment.FragmentOrdersList;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;

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
import java.util.List;

public class FragmentMenu extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	Thread thLoginChecker;

	TextView tvTitle, tvAs, tvAccount;
	TextView tvLabLocation, tvLabShipping, tvLabShare, tvLabRate, tvLabPolicy, tvLabLogout, tvLabReset, tvLabOrders, tvLabLogin, tvLabForgot, tvVersion, tvLabAbout, tvLabAlerts;
	TextView tvSectionAccount, tvSectionShop, tvSectionOther;
	LinearLayout llContainer;

	int opcode = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		activity.lastCreatedActivity = MainActivity.SCREEN_ACCOUNT;

		v =  inflater.inflate(R.layout.fragment_menu, container, false);

		storeClassVariables();
		initUIHandles();
		initUIListeners();
		formatUI();

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
		tvLabLogin = (TextView)v.findViewById(R.id.tv_login);
		tvLabForgot = (TextView)v.findViewById(R.id.tv_forgot);
		tvTitle = (TextView)v.findViewById(R.id.tv_label_title);
		tvAs = (TextView)v.findViewById(R.id.tv_label_as);
		tvAccount = (TextView)v.findViewById(R.id.tv_label_account);
		tvLabLocation = (TextView)v.findViewById(R.id.tv_location);
		tvLabShipping = (TextView)v.findViewById(R.id.tv_shipping);
		tvLabShare = (TextView)v.findViewById(R.id.tv_share);
		tvLabPolicy = (TextView)v.findViewById(R.id.tv_policies);
		tvLabRate = (TextView)v.findViewById(R.id.tv_rate);
		tvLabLogout = (TextView)v.findViewById(R.id.tv_logout);
		tvLabReset = (TextView)v.findViewById(R.id.tv_reset);
		tvLabOrders = (TextView)v.findViewById(R.id.tv_orders);
		tvLabAbout = (TextView)v.findViewById(R.id.tv_about);
		tvLabAlerts = (TextView)v.findViewById(R.id.tv_alerts);
		tvVersion = (TextView)v.findViewById(R.id.tv_version);

		tvSectionAccount = (TextView)v.findViewById(R.id.tv_section_account);
		tvSectionShop = (TextView)v.findViewById(R.id.tv_section_shop);
		tvSectionOther = (TextView)v.findViewById(R.id.tv_section_other);

	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub

		tvLabLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(activity.IS_CONNECTED) {

					FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
					FragmentAccountLogin fragment = new FragmentAccountLogin();
					fragment.isBegin = true;
					fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_LOGIN_ACCOUNT)
					.addToBackStack(MainActivity.SCREEN_LOGIN_ACCOUNT)
					.commit();

				} else {
					AlertDialog.Builder alert  = new AlertDialog.Builder(activity.context);
					alert.setMessage(MainActivity.MSG_DISCONNECTED);
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

		tvLabLogout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				AlertDialog.Builder builder = new AlertDialog.Builder(activity.context);
				builder.setMessage(MainActivity.MSG_CONFIRM);
				builder.setPositiveButton(MainActivity.MSG_YES, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						if(activity.IS_CONNECTED) {

							activity.clearEmail();
							activity.clearToken();
							activity.getFragmentManager().beginTransaction().remove(FragmentMenu.this).commit();
							activity.loadSplash();

						} else {
							AlertDialog.Builder alert  = new AlertDialog.Builder(activity.context);
							alert.setMessage(MainActivity.MSG_DISCONNECTED);
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
				builder.setNegativeButton(MainActivity.MSG_NO, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});
				builder.show();

			}

		});


		tvLabReset.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(activity.IS_CONNECTED) {

					thLoginChecker = new Thread(FragmentMenu.this);
					thLoginChecker.setName(MainActivity.TH_NAME_VERIFY_LOGIN);
					thLoginChecker.start();
					opcode = 2;

				} else {
					AlertDialog.Builder alert  = new AlertDialog.Builder(activity.context);
					alert.setMessage(MainActivity.MSG_DISCONNECTED);
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

		tvLabForgot.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(activity.IS_CONNECTED) {

					FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
					FragmentAccountForgotPassword fragment = new FragmentAccountForgotPassword();
					fragment.email = activity.getEmail();
					//fragment.isBegin = true;
					fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_FORGOT)
					.addToBackStack(MainActivity.SCREEN_FORGOT)
					.commit();

				} else {
					AlertDialog.Builder alert  = new AlertDialog.Builder(activity.context);
					alert.setMessage(MainActivity.MSG_DISCONNECTED);
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

		tvLabLocation.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(activity.IS_CONNECTED) {

					FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
					FragmentShippingLocation fragment = new FragmentShippingLocation();
					fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_LOCATION)
					.addToBackStack(MainActivity.SCREEN_LOCATION)
					.commit();

				} else {
					AlertDialog.Builder alert  = new AlertDialog.Builder(activity.context);
					alert.setMessage(MainActivity.MSG_DISCONNECTED);
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

		tvLabShipping.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(activity.IS_CONNECTED) {

					FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
					FragmentShippingInfo fragment = new FragmentShippingInfo();
					fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_SHIPPING)
					.addToBackStack(MainActivity.SCREEN_SHIPPING)
					.commit();

				} else {
					AlertDialog.Builder alert  = new AlertDialog.Builder(activity.context);
					alert.setMessage(MainActivity.MSG_DISCONNECTED);
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

		tvLabRate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(activity.IS_CONNECTED) {

					Uri uri = Uri.parse(MainActivity.MARKET_URL_PREFIX_1 + activity.context.getPackageName());
					Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
					try {
						startActivity(goToMarket);
					} catch (ActivityNotFoundException e) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.MARKET_URL_PREFIX_2 + activity.context.getPackageName())));
					}

				} else {
					AlertDialog.Builder alert  = new AlertDialog.Builder(activity.context);
					alert.setMessage(MainActivity.MSG_DISCONNECTED);
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

		tvLabShare.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(activity.IS_CONNECTED) {

					Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
					sharingIntent.setType("text/plain");
					sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, MainActivity.SHARE_SUBJECT);
					sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, MainActivity.SHARE_CONTENT + MainActivity.MARKET_URL_PREFIX_2 + activity.context.getPackageName());
					startActivity(Intent.createChooser(sharingIntent, "Share This App"));

				} else {
					AlertDialog.Builder alert  = new AlertDialog.Builder(activity.context);
					alert.setMessage(MainActivity.MSG_DISCONNECTED);
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

		tvLabPolicy.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				activity.loadPolicy();

			}

		});


		tvLabOrders.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				thLoginChecker = new Thread(FragmentMenu.this);
				thLoginChecker.setName(MainActivity.TH_NAME_VERIFY_LOGIN);
				thLoginChecker.start();
				opcode = 1;

			}

		});

		tvLabAbout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				activity.loadAbout();

			}
		});

		tvLabAlerts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				activity.loadAlerts();


			}
		});

	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub
		tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
		tvTitle.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvTitle.setGravity(Gravity.CENTER);
		tvAs.setPadding(MainActivity.SPACING, 0, MainActivity.SPACING, 0);
		tvAs.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvAs.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvAs.setGravity(Gravity.CENTER);
		tvAccount.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
		tvAccount.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvAccount.setGravity(Gravity.CENTER);


		if(IS_SIGNEDIN) {
			tvAccount.setText(activity.getEmail());
			tvLabLogin.setVisibility(LinearLayout.GONE);
			tvLabForgot.setVisibility(LinearLayout.GONE);
			tvLabOrders.setVisibility(LinearLayout.VISIBLE);
		} else {
			tvLabReset.setVisibility(LinearLayout.GONE);
			tvLabLogout.setVisibility(LinearLayout.GONE);
			tvLabOrders.setVisibility(LinearLayout.GONE);
		}

		try {

			PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
			String version = pInfo.versionName;
			tvVersion.setText("Version " + version);
			tvVersion.setTypeface(activity.tf);
			tvVersion.setPadding(MainActivity.SPACING, MainActivity.SPACING*2, MainActivity.SPACING, 0);
			tvVersion.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE - 3));

		} catch (PackageManager.NameNotFoundException e) {

		}

		tvSectionAccount.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE-3));
		tvSectionAccount.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);

		tvSectionShop.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE-3));
		tvSectionShop.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);

		tvSectionOther.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE - 3));
		tvSectionOther.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);

		tvLabLogin.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabLogin.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabLogin.setGravity(Gravity.LEFT);

		tvLabForgot.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabForgot.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabForgot.setGravity(Gravity.LEFT);

		tvLabLogout.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabLogout.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabLogout.setGravity(Gravity.LEFT);

		tvLabReset.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabReset.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabReset.setGravity(Gravity.LEFT);

		tvLabLocation.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabLocation.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabLocation.setGravity(Gravity.LEFT);

		tvLabShipping.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabShipping.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabShipping.setGravity(Gravity.LEFT);

		tvLabShare.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabShare.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabShare.setGravity(Gravity.LEFT);

		tvLabOrders.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabOrders.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabOrders.setGravity(Gravity.LEFT);

		tvLabRate.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabRate.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabRate.setGravity(Gravity.LEFT);

		tvLabPolicy.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabPolicy.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabPolicy.setGravity(Gravity.LEFT);

		tvLabAbout.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabAbout.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabAbout.setGravity(Gravity.LEFT);

		tvLabAlerts.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvLabAlerts.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
		tvLabAlerts.setGravity(Gravity.LEFT);

		activity.app.ENABLE_SYNC = false;
		activity.app.PREVENT_CLOSE_AND_SYNC = true;

	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		activity.app.ENABLE_SYNC = true;
		activity.app.PREVENT_CLOSE_AND_SYNC = false;
	}
	
	protected Handler displayHandler = new Handler() {

		public void handleMessage(Message msg) {

			MLog.log("inside display handler " + msg.what);

			if(msg.what == 4) {

				if(!IS_SIGNEDIN) {

					FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
					FragmentAccountLogin fragment = new FragmentAccountLogin();
					fragment.isBegin = true;
					fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_LOGIN_ACCOUNT)
					.addToBackStack(MainActivity.SCREEN_LOGIN_ACCOUNT)
					.commit();


				} else {

					if(opcode == 1) {

						FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
						FragmentOrdersList fragment = new FragmentOrdersList();
						fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_ORDER_LIST)
						.addToBackStack(MainActivity.SCREEN_ORDER_LIST)
						.commit();
	
					} else {
						
						FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
						FragmentAccountResetPassword fragment = new FragmentAccountResetPassword();
						fragment.email = activity.getEmail();
						//fragment.isBegin = true;
						fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_RESET)
						.addToBackStack(MainActivity.SCREEN_RESET)
						.commit();
						
					}
					
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

		// Check if current thread is cart download thread or picture download thread
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

		}

		activity.handlerLoading.sendEmptyMessage(0);

	}

	@Override
	public void setRunFlag(Boolean value) {
		// TODO Auto-generated method stub

	}


}
