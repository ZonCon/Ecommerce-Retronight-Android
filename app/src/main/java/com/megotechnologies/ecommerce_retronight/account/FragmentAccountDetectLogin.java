package com.megotechnologies.ecommerce_retronight.account;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.megotechnologies.ecommerce_retronight.FragmentMeta;
import com.megotechnologies.ecommerce_retronight.payment.FragmentPaymentConfirm;
import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.R;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCFragmentLifecycle;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCRunnable;
import com.megotechnologies.ecommerce_retronight.dataobjects.PGRecord;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;
import com.megotechnologies.ecommerce_retronight.utilities.Validator;

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
import java.util.List;

public class FragmentAccountDetectLogin extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	public Boolean isBegin = false;
	Boolean RUN_FLAG = false;
	Thread loginTh, accountTh;

	Boolean newAccount = false;

	public PGRecord pgRecord;
	TextView tvInfo, tvEmail, tvTitle, tvPassword, tvConfirm;
	EditText editEmail, editPassword, editConfirm;

	Button butNext, butBack, butForgot;
	Thread appsTh;

	String[] arrNil = new String[]{""};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		v =  inflater.inflate(R.layout.fragment_account_detectlogin, container, false);
		return v;

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		storeClassVariables();
		initUIHandles();
		initUIListeners();
		formatUI();

		if(!IS_SIGNEDIN) {

			setRunFlag(true);
			appsTh = new Thread(FragmentAccountDetectLogin.this);
			appsTh.setName(MainActivity.TH_NAME_APPS_USING);
			appsTh.start();

		} else {

			getActivity().getFragmentManager().popBackStack();

			FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
			FragmentPaymentConfirm fragment = new FragmentPaymentConfirm();
			fragment.pgRecord = pgRecord;
			fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_CONFIRM)
					.addToBackStack(MainActivity.SCREEN_CONFIRM)
					.commit();

		}

		//activity.clearEmail();
		//activity.clearToken();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void initUIHandles() {
		// TODO Auto-generated method stub

		tvInfo = (TextView)v.findViewById(R.id.tv_label_info);
		tvEmail = (TextView)v.findViewById(R.id.tv_label_email);
		tvTitle = (TextView)v.findViewById(R.id.tv_label_title);
		tvPassword = (TextView)v.findViewById(R.id.tv_label_password);
		tvConfirm = (TextView)v.findViewById(R.id.tv_label_confirm);

		editEmail = (EditText)v.findViewById(R.id.edit_email);
		editPassword = (EditText)v.findViewById(R.id.edit_password);
		editConfirm = (EditText)v.findViewById(R.id.edit_confirm);

		butNext = (Button)v.findViewById(R.id.but_next);
		butBack = (Button)v.findViewById(R.id.but_back);
		butForgot = (Button)v.findViewById(R.id.but_forgot);

	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub

		butBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				activity.onBackPressed();

			}

		});

		butForgot.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
				FragmentAccountForgotPassword fragment = new FragmentAccountForgotPassword();
				fragment.email = editEmail.getText().toString().trim();
				fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_FORGOT)
						.addToBackStack(MainActivity.SCREEN_FORGOT)
						.commit();

			}

		});

		butNext.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(newAccount) {


					if(editEmail.getText().toString().trim().length() > 0 && editPassword.getText().toString().trim().length() > 0 && editConfirm.getText().toString().trim().length() > 0) {

						if(editPassword.getText().toString().trim().equals(editConfirm.getText().toString().trim())) {

							if(Validator.isValidEmail(editEmail.getText().toString().trim()) && Validator.isValidPassword(editPassword.getText().toString().trim())) {

								setRunFlag(true);
								accountTh = new Thread(FragmentAccountDetectLogin.this);
								accountTh.setName(MainActivity.TH_NAME_ACCOUNT_CREATE);
								accountTh.start();

							} else {

								AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
								alert.setMessage(MainActivity.MSG_INVALID_EMAIL);
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
							alert.setMessage(MainActivity.MSG_PASSWORDS_NOT_MATCHING);
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


				} else {

					if(editEmail.getText().toString().trim().length() > 0 && editPassword.getText().toString().trim().length() > 0) {

						if(Validator.isValidEmail(editEmail.getText().toString().trim()) && Validator.isValidPassword(editPassword.getText().toString().trim())) {

							setRunFlag(true);
							loginTh = new Thread(FragmentAccountDetectLogin.this);
							loginTh.setName(MainActivity.TH_NAME_LOGIN);
							loginTh.start();

						} else {

							AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
							alert.setMessage(MainActivity.MSG_INVALID_EMAIL_PASSWORD);
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

			}

		});

	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub

		tvInfo.setTextColor(activity.getResources().getColor(R.color.gray));

		editEmail.setText(pgRecord.billingEmail);

		butNext.setTextColor(getResources().getColor(R.color.white));
		butNext.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		butNext.setText("PROCEED TO CONFIRM");
		butNext.setGravity(Gravity.CENTER);
		butNext.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_shadow_background));
		butNext.setPadding(MainActivity.SPACING, MainActivity.SPACING / 2, MainActivity.SPACING, MainActivity.SPACING / 2);

		butBack.setTextColor(getResources().getColor(R.color.black));
		butBack.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		butBack.setText("BACK TO SHIPPING");
		butBack.setGravity(Gravity.CENTER);
		butBack.setBackgroundDrawable(getResources().getDrawable(R.drawable.yellow_shadow_background));
		butBack.setPadding(MainActivity.SPACING, MainActivity.SPACING/2, MainActivity.SPACING, MainActivity.SPACING/2);

		butForgot.setTextColor(getResources().getColor(R.color.black));
		butForgot.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		butForgot.setText("FORGOT PASSWORD");
		butForgot.setGravity(Gravity.CENTER);
		butForgot.setBackgroundDrawable(getResources().getDrawable(R.drawable.yellow_shadow_background));
		butForgot.setPadding(MainActivity.SPACING, MainActivity.SPACING/2, MainActivity.SPACING, MainActivity.SPACING/2);

	}

	@Override
	public void storeClassVariables() {
		// TODO Auto-generated method stub

	};

	protected Handler threadHandler = new Handler() {

		public void handleMessage(Message msg) {

			MLog.log("Inside thread handler " + msg.what);

			switch (msg.what) {

				case 0:

					String jsonStr = (String)msg.obj;

					try{

						JSONObject jsonObj = new JSONObject(jsonStr);

						if(jsonObj.getString("result").equals("success")) {

							String valueStr = jsonObj.getString("value");

							JSONArray jsonArr = new JSONArray(valueStr);
							if(jsonArr.length() > 0) {

								newAccount = false;
								tvTitle.setText("Registered Email");
								tvInfo.setVisibility(LinearLayout.VISIBLE);
								tvPassword.setText("Password");
								tvConfirm.setVisibility(LinearLayout.GONE);
								editConfirm.setVisibility(LinearLayout.GONE);
								butNext.setText("SIGN IN AND PROCEED TO CONFIRM");
								butForgot.setVisibility(LinearLayout.VISIBLE);

							} else {

								newAccount = true;
								tvTitle.setText("New Email");
								tvInfo.setVisibility(LinearLayout.VISIBLE);
								tvInfo.setText("Dont' have an account yet? Create one quickly and proceed to purchase.");
								tvPassword.setText("Choose Password");
								tvConfirm.setText("Confirm Password");
								tvConfirm.setVisibility(LinearLayout.VISIBLE);
								editConfirm.setVisibility(LinearLayout.VISIBLE);
								butNext.setText("CREATE ACCOUNT AND PROCEED TO CONFIRM");
								butForgot.setVisibility(LinearLayout.GONE);

							}

							tvTitle.setVisibility(LinearLayout.VISIBLE);
							tvEmail.setVisibility(LinearLayout.VISIBLE);
							editEmail.setVisibility(LinearLayout.VISIBLE);
							tvPassword.setVisibility(LinearLayout.VISIBLE);
							editPassword.setVisibility(LinearLayout.VISIBLE);
							butBack.setVisibility(LinearLayout.VISIBLE);
							butNext.setVisibility(LinearLayout.VISIBLE);

						}



					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;


				case 1:

					jsonStr = (String)msg.obj;

					try{

						JSONObject jsonObj = new JSONObject(jsonStr);

						if(jsonObj.getString("result").equals("success")) {

							String valueStr = jsonObj.getString("value");
							MLog.log(valueStr);

							jsonObj = new JSONObject(valueStr);
							String token = jsonObj.getString("token");
							activity.updateToken(token);
							activity.updateEmail(editEmail.getText().toString().trim());

							getActivity().getFragmentManager().popBackStack();

							FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
							FragmentPaymentConfirm fragment = new FragmentPaymentConfirm();
							fragment.pgRecord = pgRecord;
							fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_CONFIRM)
									.addToBackStack(MainActivity.SCREEN_CONFIRM)
									.commit();

						} else {

							AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
							alert.setMessage(MainActivity.MSG_LOGIN_FAIL);
							alert.setTitle("Error");
							alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									//dismiss the dialog
								}
							});
							alert.create().show();

						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;

				case 2:

					jsonStr = (String)msg.obj;
					//MLog.log(jsonStr);
					try{

						JSONObject jsonObj = new JSONObject(jsonStr);

						if(jsonObj.getString("result").equals("success")) {

							String valueStr = jsonObj.getString("value");

							if(valueStr.equals("null")) {

								newAccount = false;
								butNext.performClick();

							}

						} else {

							AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
							alert.setMessage(MainActivity.MSG_ZONCON_ACCOUNT_DUPLICATE);
							alert.setTitle("Alert");
							alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									//dismiss the dialog
								}
							});
							alert.create().show();

						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;

			}

		};

	};

	@Override
	public void run() {

		Looper.prepare();

		MainActivity a = (MainActivity)getActivity();
		a.handlerLoading.sendEmptyMessage(1);

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

			if(tName.equals(MainActivity.TH_NAME_APPS_USING)) {
				httppost = new HttpPost(MainActivity.API_USER_APPS);
				nameValuePairs.add(new BasicNameValuePair("params", "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + editEmail.getText().toString().trim() + "\"}]"));
				MLog.log("[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + editEmail.getText().toString().trim() + "\"}]");
			} else if(tName.equals(MainActivity.TH_NAME_LOGIN)) {
				httppost = new HttpPost(MainActivity.API_LOGIN);
				nameValuePairs.add(new BasicNameValuePair("params", "[{ \"email\": \"" + editEmail.getText().toString().trim() + "\", \"password\": \"" + editPassword.getText() + "\", \"idProject\": \"" + MainActivity.PID + "\"}]"));
				MLog.log("[{ \"email\": \"" + editEmail.getText().toString().trim() + "\", \"password\": \"" + editPassword.getText() + "\", \"idProject\": \"" + MainActivity.PID + "\"}]");
			} else if(tName.equals(MainActivity.TH_NAME_ACCOUNT_CREATE)) {
				httppost = new HttpPost(MainActivity.API_CREATE_ACCOUNT);
				nameValuePairs.add(new BasicNameValuePair("params", "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + editEmail.getText().toString().trim() + "\", \"password\": \"" + editPassword.getText() + "\"}]"));
				MLog.log("[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + editEmail.getText() + "\", \"password\": \"" + editPassword.getText() + "\"}]");
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
				if(tName.equals(MainActivity.TH_NAME_APPS_USING)) {
					msg.what = 0;
				} else if(tName.equals(MainActivity.TH_NAME_ACCOUNT_CREATE)) {
					msg.what = 2;
				} else {
					msg.what = 1;
				}
				threadHandler.sendMessage(msg);

			} catch (Exception e) {
				e.printStackTrace();
				setRunFlag(true);
				if(tName.equals(MainActivity.TH_NAME_APPS_USING)) {
					appsTh = new Thread(FragmentAccountDetectLogin.this);
					appsTh.setName(MainActivity.TH_NAME_APPS_USING);
					appsTh.start();
				}
			}
			/*
			catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/

			setRunFlag(false);

		}

		a.handlerLoading.sendEmptyMessage(0);
		//a.hideLoading();

	}

	@Override
	public void setRunFlag(Boolean value) {
		// TODO Auto-generated method stub
		RUN_FLAG = value;
	}

}
