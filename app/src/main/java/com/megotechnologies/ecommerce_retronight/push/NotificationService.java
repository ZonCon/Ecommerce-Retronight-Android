package com.megotechnologies.ecommerce_retronight.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.R;
import com.megotechnologies.ecommerce_retronight.ZonConApplication;
import com.megotechnologies.ecommerce_retronight.db.DbConnection;
import com.megotechnologies.ecommerce_retronight.loading.SplashActivity;
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
import java.util.HashMap;
import java.util.List;

public class NotificationService extends Service {

	public static final int NOTIFICATION_ID = 0;
	Thread checkNotifThread;
	NotificationManager mNotificationManager;
	String previousNotifMsg = "";
	Boolean RUN_FLAG = true;
	DbConnection dbC;
	public ZonConApplication app;

	private class CheckNotifications implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			while(RUN_FLAG) {

				HashMap<String, String> map = new HashMap<String, String>();
				map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_STREAM);
				ArrayList<HashMap<String, String>> records =  null;
				if(dbC.isOpen()) {
					dbC.isAvailale();
					records =  dbC.retrieveRecords(map);
				}

				for(int i = 0; i < records.size(); i++) {

					map = records.get(i);
					String idStream = map.get(MainActivity.DB_COL_SRV_ID);
					String _idStream = map.get(MainActivity.DB_COL_ID);

					String jsonStr = "";
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost httppost = null;
					httppost = new HttpPost(MainActivity.API_NOTIFICATIONS);
					jsonStr = "[{\"idProject\": \"" + MainActivity.PID + "\", \"idStream\": \"" + idStream + "\"}]";

					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
					nameValuePairs.add(new BasicNameValuePair("params", jsonStr));
					MLog.log("Notifications API=" + jsonStr);

					String responseString = null;

					try {
						httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
						HttpResponse response = httpclient.execute(httppost);
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						response.getEntity().writeTo(out);
						out.close();
						responseString = out.toString();
						MLog.log("Notifications=" + responseString);

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

							MLog.log("Res=" + jsonStr);
							JSONObject jsonObj = new JSONObject(jsonStr);
							if(jsonObj.getString("result").equals("success")) {

								String value = jsonObj.getString("value");

								if(value != null && !value.equals("null")) {

									jsonObj = new JSONObject(value);

									String timestamp = jsonObj.getString("timestampPublish");
									String title = jsonObj.getString("title");
									String published = jsonObj.getString("published");
									String idItem = jsonObj.getString("idProductitems");

									map = new HashMap<String, String>();
									map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_ITEM);
									map.put(MainActivity.DB_COL_FOREIGN_KEY, _idStream);
									map.put(MainActivity.DB_COL_ID, idItem);
									if(dbC.isOpen()) {
										dbC.isAvailale();
										ArrayList<HashMap<String, String>> recordsItems = dbC.retrieveRecords(map);

										if(recordsItems.size() == 0) {

											map = new HashMap<String, String>();
											map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MESSAGESTREAM_PUSH);
											map.put(MainActivity.DB_COL_SRV_ID, idStream);
											map.put(MainActivity.DB_COL_TITLE, idItem);

											if(dbC.isOpen()) {
												dbC.isAvailale();

												ArrayList<HashMap<String, String>> recordsNotifs = dbC.retrieveRecords(map);
												if (recordsNotifs.size() == 0) {

													Message msg = new Message();
													msg.obj = title;
													notifHandler.sendMessage(msg);

													map = new HashMap<String, String>();
													map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MESSAGESTREAM_PUSH);
													map.put(MainActivity.DB_COL_SRV_ID, idStream);
													if(dbC.isOpen()) {
														dbC.isAvailale();
														dbC.deleteRecord(map);

													}

													map = new HashMap<String, String>();
													map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MESSAGESTREAM_PUSH);
													map.put(MainActivity.DB_COL_SRV_ID, idStream);
													map.put(MainActivity.DB_COL_TITLE, idItem);

													if(dbC.isOpen()) {
														dbC.isAvailale();
														dbC.insertRecord(map);
													}

												} else {

												}

											}

										}
									}

								}

							}

						}
						catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}



				try {
					Thread.sleep(MainActivity.NOTIF_SLEEP_TIME);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}


		}


	}

	@Override
	public void onCreate() {
		// Start up the thread running the service.  Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.  We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		app = (ZonConApplication)getApplicationContext();
		dbC = app.conn;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(checkNotifThread != null) {
			if(checkNotifThread.isAlive()) {
				RUN_FLAG = false;
			}
		}
		RUN_FLAG = true;
		checkNotifThread = new Thread(new CheckNotifications());
		checkNotifThread.start();
		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy() {
		RUN_FLAG = false;
	}

	public void showNotification(String msg) {

		int stringId = getApplicationContext().getApplicationInfo().labelRes;
		String appName = getApplicationContext().getString(stringId);

		mNotificationManager = (NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent newIntent = new Intent(this, SplashActivity.class);
		newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle(msg)
		.setDefaults(Notification.DEFAULT_ALL)
		.setStyle(new NotificationCompat.BigTextStyle()
		.bigText(msg))
		.setContentText("New updates for " + appName);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
		r.play();

	}

	public Handler notifHandler = new Handler() {

		public void handleMessage(Message msg) {

			showNotification(msg.obj + "");

		}
	};

}
