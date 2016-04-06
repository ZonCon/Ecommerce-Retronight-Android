package com.megotechnologies.ecommerce_retronight.account;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FragmentAccountCreate extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	public Boolean isBegin = false;
	Boolean RUN_FLAG = false;
	Thread accountTh, appsTh;

	TextView tvEmail, tvPassword, tvRepassword, tvTitle;
	EditText editEmail, editPassword, editRepassword;

	Button butSave;

	String[] arrNil = new String[]{""};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		v =  inflater.inflate(R.layout.fragment_account_create, container, false);
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
		editRepassword = (EditText)v.findViewById(R.id.edit_repassword);
		tvTitle = (TextView)v.findViewById(R.id.tv_label_title);
		tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
		tvEmail = (TextView)v.findViewById(R.id.tv_label_email);
		tvPassword = (TextView)v.findViewById(R.id.tv_label_password);
		tvRepassword = (TextView)v.findViewById(R.id.tv_label_repassword);
		butSave = (Button)v.findViewById(R.id.but_save);
	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub

		editPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub

				MLog.log("Focus=" + hasFocus);

				if(hasFocus && editEmail.getText().toString().trim().length() > 0 && Validator.isValidEmail(editEmail.getText().toString().trim())) {

					setRunFlag(true);
					appsTh = new Thread(FragmentAccountCreate.this);
					appsTh.setName(MainActivity.TH_NAME_APPS_USING);
					appsTh.start();

				}
			}

		});


		editEmail.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				MLog.log("Keypressed...");

				if(editEmail.length() > 0) {

					editPassword.setText("");
					editPassword.setEnabled(true);
					editPassword.setBackgroundColor(getResources().getColor(R.color.white));

					editRepassword.setText("");
					editRepassword.setEnabled(true);
					editRepassword.setBackgroundColor(getResources().getColor(R.color.white));

				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		butSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(editEmail.getText().toString().trim().length() > 0 && editPassword.getText().toString().trim().length() > 0 && editRepassword.getText().toString().trim().length() > 0) {

					if(editPassword.getText().toString().trim().equals(editRepassword.getText().toString().trim())) {

						if(Validator.isValidEmail(editEmail.getText().toString().trim()) && Validator.isValidPassword(editPassword.getText().toString().trim())) {

							setRunFlag(true);
							accountTh = new Thread(FragmentAccountCreate.this);
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

			}

		});

	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub
		tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));

		tvEmail.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		editEmail.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		tvPassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		tvRepassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		editPassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		editRepassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		butSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		butSave.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.dark_shadow_background));
		butSave.setPadding(MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2);

		editPassword.setEnabled(false);
		editPassword.setBackgroundColor(getResources().getColor(R.color.light_gray));
		editRepassword.setEnabled(false);
		editRepassword.setBackgroundColor(getResources().getColor(R.color.light_gray));
	}

	@Override
	public void storeClassVariables() {
		// TODO Auto-generated method stub

	};

	public void loadLogin() {
		FragmentTransaction fragmentTransaction = activity.fragMgr.beginTransaction();
		FragmentAccountLogin fragment = new FragmentAccountLogin();
		//fragment.isBegin = true;
		fragmentTransaction.add(R.id.ll_body, fragment, MainActivity.SCREEN_LOGIN_ACCOUNT)
		.addToBackStack(MainActivity.SCREEN_LOGIN_ACCOUNT)
		.commit();
	}

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

						JSONArray jsonArr = new JSONArray(valueStr);
						String apps = "";
						for(int i = 0; i < jsonArr.length(); i++) {

							jsonObj = jsonArr.getJSONObject(i);
							String nameProject = jsonObj.getString("nameProject");
							String idProject = jsonObj.getString("idProjects");

							if(!idProject.equals(MainActivity.PID)) {


								if(i == (jsonArr.length() - 1)) {

									apps = (apps + nameProject + ". ");

								} else {

									apps = (apps + nameProject + ", ");

								}

							}

						}

						if(jsonArr.length() > 0) {

							AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
							alert.setMessage("Your email is already registered with the ZonCon App Management System. Use the same to login. If you don't remember the password, please follow the 'Forgot Password' button.");
							alert.setTitle("Alert");
							alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									//dismiss the dialog  
									activity.getFragmentManager().beginTransaction().remove(FragmentAccountCreate.this).commit();
								}
							});
							alert.create().show();

						}

					}



				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;

			case 1:

				jsonStr = (String)msg.obj;
				//MLog.log(jsonStr);
				try{

					JSONObject jsonObj = new JSONObject(jsonStr);

					if(jsonObj.getString("result").equals("success")) {

						String valueStr = jsonObj.getString("value");

						if(valueStr.equals("null")) {

							AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
							alert.setMessage(MainActivity.MSG_ZONCON_ACCOUNT_SUCCESS);
							alert.setTitle("Alert");
							alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									//dismiss the dialog  
									activity.getFragmentManager().beginTransaction().remove(FragmentAccountCreate.this).commit();
								}
							});
							alert.create().show();


						} 

					} else {

						AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
						alert.setMessage(MainActivity.MSG_ZONCON_ACCOUNT_DUPLICATE);
						alert.setTitle("Alert");
						alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								//dismiss the dialog  
								activity.getFragmentManager().beginTransaction().remove(FragmentAccountCreate.this).commit();
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

			if(tName.equals(MainActivity.TH_NAME_ACCOUNT_CREATE)) {
				httppost = new HttpPost(MainActivity.API_CREATE_ACCOUNT);
				nameValuePairs.add(new BasicNameValuePair("params", "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + editEmail.getText().toString().trim() + "\", \"password\": \"" + editPassword.getText() + "\"}]"));
				MLog.log("[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + editEmail.getText() + "\", \"password\": \"" + editPassword.getText() + "\"}]");
			} else if(tName.equals(MainActivity.TH_NAME_APPS_USING)) {
				httppost = new HttpPost(MainActivity.API_USER_APPS);
				nameValuePairs.add(new BasicNameValuePair("params", "[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + editEmail.getText().toString().trim() + "\"}]"));
				MLog.log("[{\"idProject\": \"" + MainActivity.PID + "\", \"email\": \"" + editEmail.getText().toString().trim() + "\"}]");
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
					msg.what = 1;
				}
				threadHandler.sendMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setRunFlag(true);
				if(tName.equals(MainActivity.TH_NAME_APPS_USING)) {

					setRunFlag(true);
					appsTh = new Thread(FragmentAccountCreate.this);
					appsTh.setName(MainActivity.TH_NAME_APPS_USING);
					appsTh.start();

				} else if(tName.equals(MainActivity.TH_NAME_ACCOUNT_CREATE)) {

					setRunFlag(true);
					accountTh = new Thread(FragmentAccountCreate.this);
					accountTh.setName(MainActivity.TH_NAME_ACCOUNT_CREATE);
					accountTh.start();

				}
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
