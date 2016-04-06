package com.megotechnologies.ecommerce_retronight.account;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.megotechnologies.ecommerce_retronight.FragmentMeta;
import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.R;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCFragmentLifecycle;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCRunnable;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FragmentAccountLogin extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	public Boolean isBegin = false;
	Boolean RUN_FLAG = false;
	Thread loginTh;

	TextView tvEmail, tvPassword, tvTitle, tvAccountInfo;
	EditText editEmail, editPassword;

	Button butSave, butForgot, butCreate;

	String[] arrNil = new String[]{""};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		v =  inflater.inflate(R.layout.fragment_account_login, container, false);
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
		
		activity.clearEmail();
		activity.clearToken();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void initUIHandles() {
		// TODO Auto-generated method stub
		editEmail = (EditText)v.findViewById(R.id.edit_email);
		editPassword = (EditText)v.findViewById(R.id.edit_password);
		tvTitle = (TextView)v.findViewById(R.id.tv_label_title);
		tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));

		tvAccountInfo = (TextView)v.findViewById(R.id.tv_label_accountinfo);
		tvEmail = (TextView)v.findViewById(R.id.tv_label_email);
		tvPassword = (TextView)v.findViewById(R.id.tv_label_password);
		butSave = (Button)v.findViewById(R.id.but_save);
		butForgot = (Button)v.findViewById(R.id.but_forgot);
		butCreate = (Button)v.findViewById(R.id.but_create);
	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub
		
		butSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(editEmail.getText().toString().trim().length() > 0 && editPassword.getText().toString().trim().length() > 0) {

					if(Validator.isValidEmail(editEmail.getText().toString().trim()) && Validator.isValidPassword(editPassword.getText().toString().trim())) {

						setRunFlag(true);
						loginTh = new Thread(FragmentAccountLogin.this);
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

		});

		butCreate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
				FragmentAccountCreate fragment = new FragmentAccountCreate();
				fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_CREATE_ACCOUNT)
				.addToBackStack(MainActivity.SCREEN_CREATE_ACCOUNT)
				.commit();

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

	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub

		tvAccountInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));

		tvEmail.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		editEmail.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		tvPassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		editPassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		butSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		butSave.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.dark_shadow_background));
		butSave.setPadding(MainActivity.SPACING / 2, MainActivity.SPACING / 2, MainActivity.SPACING / 2, MainActivity.SPACING / 2);

		butForgot.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		butForgot.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.yellow_shadow_background));
		butForgot.setPadding(MainActivity.SPACING / 2, MainActivity.SPACING / 2, MainActivity.SPACING / 2, MainActivity.SPACING / 2);

		butCreate.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		butCreate.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.red_shadow_background));
		butCreate.setPadding(MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2);
		//butProceed.setTypeface(activity.tfAllegraya);
		//butProceed.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.dark_shadow_background));
	}

	@Override
	public void storeClassVariables() {
		// TODO Auto-generated method stub

	};

	protected Handler threadHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			MLog.log("Inside thread handler " + msg.what);

			String jsonStr = null;

			switch (msg.what) {

			case 0: 

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
						activity.getFragmentManager().beginTransaction().remove(FragmentAccountLogin.this).commit();
						if(isBegin) {
							activity.loadSplash();
						} else {
							activity.loadCart();
						}

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

			default:
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

			if(tName.equals(MainActivity.TH_NAME_LOGIN)) {
				httppost = new HttpPost(MainActivity.API_LOGIN);
				nameValuePairs.add(new BasicNameValuePair("params", "[{ \"email\": \"" + editEmail.getText().toString().trim() + "\", \"password\": \"" + editPassword.getText() + "\", \"idProject\": \"" + MainActivity.PID + "\"}]"));
				MLog.log("[{ \"email\": \"" + editEmail.getText().toString().trim() + "\", \"password\": \"" + editPassword.getText() + "\", \"idProject\": \"" + MainActivity.PID + "\"}]");
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
				if(tName.equals(MainActivity.TH_NAME_LOGIN)) {
					msg.what = 0;
				} 
				threadHandler.sendMessage(msg);

			} catch (Exception e) {
				e.printStackTrace();
				setRunFlag(true);
				loginTh = new Thread(FragmentAccountLogin.this);
				loginTh.setName(MainActivity.TH_NAME_LOGIN);
				loginTh.start();
			}

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
