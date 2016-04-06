package com.megotechnologies.ecommerce_retronight;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.megotechnologies.ecommerce_retronight.db.DbConnection;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;

import java.util.Timer;
import java.util.TimerTask;

public class FragmentMeta extends Fragment{

	protected View v;
	protected MainActivity activity;
	protected DbConnection dbC;
	protected Boolean LOCATION_SELECTED = false;
	protected Boolean IS_SIGNEDIN = false;
	protected Boolean IS_CLICKED = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		activity = (MainActivity)getActivity();
		dbC = activity.app.conn;

		activity.populateLocation();

		if(activity.myCity!= null) {
			LOCATION_SELECTED = true;
		}

		activity.IS_CONNECTED = false;
		ConnectivityManager check = (ConnectivityManager) activity.context.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo[] info = check.getAllNetworkInfo();
		for (int i = 0; i<info.length; i++){
			if (info[i].getState() == NetworkInfo.State.CONNECTED){
				activity.IS_CONNECTED = true;
			}
		}
		MLog.log("Showing Connected... " + activity.IS_CONNECTED);
		activity.showConnected(activity.IS_CONNECTED);

		String email = activity.getEmail();
		String token = activity.getToken();

		if(email.trim().length() > 0 && token.trim().length() > 0) {

			IS_SIGNEDIN = true;

		} else {

			IS_SIGNEDIN = false;

		}

		activity.showCartNotification();
		activity.showAlertNotification();
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				activity.IS_CLICKED = false;		
			}
			
		}, 1000);
		
		if(activity.app.APP_EXIT_ON_BACK) {
			activity.app.APP_EXIT_ON_BACK = false;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		IS_CLICKED = false;
	}

	
	public void setHeaderTitle(String titleText) {
		TextView tvHead = (TextView)v.findViewById(R.id.tv_label_title);
		tvHead.setText(titleText + " " + tvHead.getText());
	}

}
