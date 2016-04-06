package com.megotechnologies.ecommerce_retronight.account;

import android.app.AlertDialog;
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

public class FragmentAccountForgotPassword extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	public String email = "";
	Boolean RUN_FLAG = false;
	Thread resetTh, changeTh;

	TextView tvEmail, tvTitle, tvPassword, tvRepassword, tvCode, tvEmail1;
	EditText editEmail, editPassword, editRepassword, editCode, editEmail1;

	Button butSave, butSave1, butBack;

	String[] arrNil = new String[]{""};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		v =  inflater.inflate(R.layout.fragment_account_forgot_password, container, false);
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
		editEmail1 = (EditText)v.findViewById(R.id.edit_email_1);
		tvTitle = (TextView)v.findViewById(R.id.tv_label_title);
		tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
		tvPassword = (TextView)v.findViewById(R.id.tv_label_password);
		tvRepassword = (TextView)v.findViewById(R.id.tv_label_repassword);
		tvCode = (TextView)v.findViewById(R.id.tv_label_code);
		editPassword = (EditText)v.findViewById(R.id.edit_password);
		editRepassword = (EditText)v.findViewById(R.id.edit_repassword);
		editCode = (EditText)v.findViewById(R.id.edit_code);
		tvEmail = (TextView)v.findViewById(R.id.tv_label_email);
		tvEmail1 = (TextView)v.findViewById(R.id.tv_label_email_1);
		butSave = (Button)v.findViewById(R.id.but_save);
		butBack = (Button)v.findViewById(R.id.but_back);
		butSave1 = (Button)v.findViewById(R.id.but_save_1);
	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub

		butSave1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(editEmail1.getText().toString().trim().length() > 0 && 
						editPassword.getText().toString().trim().length() > 0 && 
						editRepassword.getText().toString().trim().length() > 0 &&
						editCode.getText().toString().trim().length() > 0) {

					if(editPassword.getText().toString().trim().equals(editRepassword.getText().toString().trim())) {

						if(Validator.isValidEmail(editEmail.getText().toString().trim()) && Validator.isValidPassword(editRepassword.getText().toString().trim())) {

							setRunFlag(true);
							changeTh = new Thread(FragmentAccountForgotPassword.this);
							changeTh.setName(MainActivity.TH_NAME_PASSWORD_CHANGE);
							changeTh.start();

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

		butSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(editEmail.getText().toString().trim().length() > 0) {

					if(Validator.isValidEmail(editEmail.getText().toString().trim())) {

						editEmail1.setText(editEmail.getText().toString().trim());

						setRunFlag(true);
						resetTh = new Thread(FragmentAccountForgotPassword.this);
						resetTh.setName(MainActivity.TH_NAME_PASSWORD_RESET);
						resetTh.start();

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

		butBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				activity.onBackPressed();

			}
		});

	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub

		tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));

		tvEmail.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		tvPassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		tvRepassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		tvCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		editEmail1.setText(email);

		editEmail1.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		editEmail.setText(email);

		editEmail.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		editPassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		editRepassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		editCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		butBack.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.yellow_shadow_background));
		butBack.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		butBack.setTextColor(activity.getResources().getColor(R.color.black));
		butBack.setPadding(MainActivity.SPACING / 2, MainActivity.SPACING / 2, MainActivity.SPACING / 2, MainActivity.SPACING / 2);

		butSave.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.dark_shadow_background));
		butSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		butSave.setPadding(MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2);

		butSave1.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		butSave1.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.dark_shadow_background));
		butSave1.setPadding(MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2);
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

						AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
						alert.setMessage("Password reset link is sent to your registered email address.");
						alert.setTitle("Alert");
						alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								//dismiss the dialog
								activity.onBackPressed();
							}
						});
						alert.create().show();


					} else {

						AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
						alert.setMessage(MainActivity.MSG_ZONCON_ACCOUNT_NOT_EXISTS);
						alert.setTitle("Error");
						alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								//dismiss the dialog
								activity.onBackPressed();
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

			if(tName.equals(MainActivity.TH_NAME_PASSWORD_RESET)) {

				httppost = new HttpPost(MainActivity.API_RESET_PASSWORD);
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
				if(tName.equals(MainActivity.TH_NAME_PASSWORD_RESET)) {
					msg.what = 0;
				} else {
					msg.what = 1;
				}

				threadHandler.sendMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setRunFlag(true);
				if(tName.equals(MainActivity.TH_NAME_PASSWORD_RESET)) {

					setRunFlag(true);
					resetTh = new Thread(FragmentAccountForgotPassword.this);
					resetTh.setName(MainActivity.TH_NAME_PASSWORD_RESET);
					resetTh.start();

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
