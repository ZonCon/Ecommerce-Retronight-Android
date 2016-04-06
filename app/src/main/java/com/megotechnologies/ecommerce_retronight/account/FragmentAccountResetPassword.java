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

public class FragmentAccountResetPassword extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	public String email = "";
	Boolean RUN_FLAG = false;
	Thread resetTh, changeTh;

	TextView tvEmail, tvTitle, tvPassword, tvRepassword, tvConfpassword;
	EditText editPassword, editRepassword, editConfpassword;

	Button butSave;

	String[] arrNil = new String[]{""};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		v =  inflater.inflate(R.layout.fragment_account_reset_password, container, false);
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
		tvTitle = (TextView)v.findViewById(R.id.tv_label_title);
		tvPassword = (TextView)v.findViewById(R.id.tv_label_password);
		tvRepassword = (TextView)v.findViewById(R.id.tv_label_new_password);
		tvConfpassword = (TextView)v.findViewById(R.id.tv_label_confirm_password);
		editPassword = (EditText)v.findViewById(R.id.edit_password);
		editRepassword = (EditText)v.findViewById(R.id.edit_new_password);
		editConfpassword = (EditText)v.findViewById(R.id.edit_confirm_password);
		tvEmail = (TextView)v.findViewById(R.id.tv_label_email);
		butSave = (Button)v.findViewById(R.id.but_save);
	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub

		/*

		butSave1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(editPassword.getText().toString().trim().length() > 0 && 
						editRepassword.getText().toString().trim().length() > 0) {

					if(editPassword.getText().toString().trim().equals(editRepassword.getText().toString().trim())) {

						setRunFlag(true);
						changeTh = new Thread(FragmentAccountResetPassword.this);
						changeTh.setName(MainActivity.TH_NAME_PASSWORD_CHANGE);
						changeTh.start();

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

		 */

		butSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(editPassword.getText().toString().trim().length() > 0 && editRepassword.getText().toString().trim().length() > 0 && editConfpassword.getText().toString().trim().length() > 0) {

					if(editConfpassword.getText().toString().trim().equals(editRepassword.getText().toString().trim())) {
						
						if(Validator.isValidPassword(editRepassword.getText())) {

							setRunFlag(true);
							changeTh = new Thread(FragmentAccountResetPassword.this);
							changeTh.start();
							
						} else {
							
							AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
							alert.setMessage(MainActivity.MSG_PASSWORDS_INVALID);
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

		tvPassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		tvRepassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		tvConfpassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		editPassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		editRepassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		editConfpassword.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));

		butSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		butSave.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.dark_shadow_background));
		butSave.setPadding(MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2);

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
						alert.setMessage(MainActivity.MSG_PASSWORDS_CHANGE_SUCCESS);
						alert.setTitle("Error");
						alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								//dismiss the dialog  
							}
						});
						alert.create().show();
						activity.getFragmentManager().beginTransaction().remove(FragmentAccountResetPassword.this).commit();

					} else {

						AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
						alert.setMessage(MainActivity.MSG_PASSWORDS_CHANGE_FAILURE);
						alert.setTitle("Error");
						alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								//dismiss the dialog  
							}
						});
						alert.create().show();
						activity.getFragmentManager().beginTransaction().remove(FragmentAccountResetPassword.this).commit();

					}


				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;


			case 1:

				jsonStr = (String)msg.obj;

				try {

					JSONObject jsonObj = new JSONObject(jsonStr);

					if(jsonObj.getString("result").equals("success")) {

						AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
						alert.setMessage(MainActivity.MSG_ZONCON_PASSWORD_RESET_SUCCESS);
						alert.setTitle("Error");
						alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								//dismiss the dialog
								activity.clearEmail();
								activity.clearToken();
								activity.getFragmentManager().beginTransaction().remove(FragmentAccountResetPassword.this).commit();
								activity.loadShop();
							}
						});
						alert.create().show();

					} else {

						AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
						alert.setMessage(MainActivity.MSG_ZONCON_PASSWORD_RESET_ERROR);
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
		httppost = new HttpPost(MainActivity.API_FORGOT_CHANGE_PASSWORD);
		nameValuePairs.add(new BasicNameValuePair("params", "[{\"email\": \"" + activity.getEmail().trim() + "\", \"token\": \"" + activity.getToken().trim() + "\", \"oldpassword\": \"" + editPassword.getText().toString().trim() + "\", \"newpassword\": \"" + editRepassword.getText().toString().trim() + "\"}]"));
		MLog.log("[{\"email\": \"" + activity.getEmail().trim() + "\", \"token\": \"" + activity.getToken().trim() + "\", \"oldpassword\": \"" + editPassword.getText().toString().trim() + "\", \"newpassword\": \"" + editRepassword.getText().toString().trim() + "\"}]");

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
			msg.what = 0;

			threadHandler.sendMessage(msg);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setRunFlag(true);
			changeTh = new Thread(FragmentAccountResetPassword.this);
			changeTh.start();

		}

		setRunFlag(false);

		a.handlerLoading.sendEmptyMessage(0);
		//a.hideLoading();

	}

	@Override
	public void setRunFlag(Boolean value) {
		// TODO Auto-generated method stub
		RUN_FLAG = value;
	}

}
