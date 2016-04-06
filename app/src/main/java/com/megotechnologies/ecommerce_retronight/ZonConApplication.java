package com.megotechnologies.ecommerce_retronight;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.megotechnologies.ecommerce_retronight.db.DbConnection;

import java.util.Timer;
import java.util.TimerTask;

public class ZonConApplication extends Application implements ActivityLifecycleCallbacks, ComponentCallbacks2{

	public SharedPreferences sharedPreferences;

	public boolean wasInBackground = false;
	public String stateOfLifeCycle = "";
	public boolean ENABLE_SYNC = true;
	public boolean APP_EXIT_ON_BACK = false;
	public boolean ENABLE_BACK = true;
	public boolean ISGOINGOUTOFAPP = false;
	public boolean PREVENT_CLOSE_AND_SYNC = false;
	
	public Timer timer = null;
	
	Context context = null;

	public DbConnection conn;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		registerActivityLifecycleCallbacks(this);

		sharedPreferences = getSharedPreferences(MainActivity.MyPREFERENCES, Context.MODE_PRIVATE);

		conn = new DbConnection(getApplicationContext());
		if(!conn.isOpen()) conn.open();

	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();

		if(conn.isOpen()) conn.close();

	}


	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		wasInBackground = false;
		stateOfLifeCycle = "Create";
	}


	@Override
	public void onActivityStarted(Activity activity) {
		// TODO Auto-generated method stub
		stateOfLifeCycle = "Start";
	}


	@Override
	public void onActivityResumed(Activity activity) {
		// TODO Auto-generated method stub
		stateOfLifeCycle = "Resume";
		if(timer != null) {

			timer.cancel();
			timer = null;

		}
	}


	@Override
	public void onActivityPaused(Activity activity) {
		// TODO Auto-generated method stub
		stateOfLifeCycle = "Pause";
		if(!ISGOINGOUTOFAPP && !PREVENT_CLOSE_AND_SYNC) {

			if(timer == null) {

				timer = new Timer();
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						if(context != null) {

							((Activity)context).finish();
		
						}

					}

				}, 2000);

			} else {

				timer.cancel();
				timer = null;

			}

		}
	}


	@Override
	public void onActivityStopped(Activity activity) {
		// TODO Auto-generated method stub
		stateOfLifeCycle = "Stop";
	}


	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onActivityDestroyed(Activity activity) {
		// TODO Auto-generated method stub
		wasInBackground = false;
		stateOfLifeCycle = "Destroy";
	}

	@Override
	public void onTrimMemory(int level) {
		// TODO Auto-generated method stub
		if (stateOfLifeCycle.equals("Stop")) {
			wasInBackground = true;
		}
		super.onTrimMemory(level);
	}


}
