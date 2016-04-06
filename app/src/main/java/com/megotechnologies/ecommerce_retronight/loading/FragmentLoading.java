package com.megotechnologies.ecommerce_retronight.loading;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.megotechnologies.ecommerce_retronight.FragmentMeta;
import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.R;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCFragmentLifecycle;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCRunnable;
import com.megotechnologies.ecommerce_retronight.push.NotificationService;

import java.util.Random;

public class FragmentLoading extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	TextView tvPoweredBy;
	RelativeLayout rlLoadingC;
	ImageView ivLoading, ivLoadingBg;
	protected Thread threadChecker;
	protected ThreadStreams threadStream;
	protected ThreadCleanCart threadCleanCart;
	protected ThreadValidateLocation threadValidateLocation;
	long threadStartTime = 0;

	public Boolean isBegin = false;
	private AnimationDrawable animation;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		v =  inflater.inflate(R.layout.fragment_loading, container, false);

		activity.app.ENABLE_BACK = false;
		
		if(isBegin) {
			activity.IS_CLICKABLE_FRAME = false;
		}

		activity.populateLocation();
		threadStream = new ThreadStreams(activity.myCountryId, activity.myStateId, activity.myCityId, dbC);
		threadStream.start();

		threadCleanCart = new ThreadCleanCart(activity.getEmail(), activity.getToken(), activity.myCountryId, activity.myStateId, activity.myCityId, dbC);
		threadCleanCart.start();

		threadValidateLocation = new ThreadValidateLocation(activity.myCountryId, activity.myStateId, activity.myCityId, dbC);
		threadValidateLocation.start();

		threadStartTime = System.currentTimeMillis();
		threadChecker = new Thread(this);
		threadChecker.start();

		Intent i= new Intent(activity.context, NotificationService.class);
		activity.startService(i);

		activity.removeAppliedCouponToCart();
		
		activity.hideHeaderFooter();
		//activity.app.prepareMusicPlayer();

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
		if(isBegin) {
			activity.IS_CLICKABLE_FRAME = true;
		}
		animation.stop();
	}

	protected Handler threadHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			if(activity.checkVersionCompat()) {
				
				activity.app.ENABLE_BACK = true;
				activity.loadShop();

			} else {

				AlertDialog.Builder builder = new AlertDialog.Builder(activity.context);
				builder.setMessage(MainActivity.MSG_VERSION_UPDATE);
				builder.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						try {

							String url = MainActivity.MARKET_URL_PREFIX_2 + activity.context.getPackageName();
							Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
							startActivity(myIntent);
							activity.finish();

						} catch (IllegalStateException e) {

						}

					}
				});
				builder.show();

			}

		}

	};

	@Override
	public void run() {
		// TODO Auto-generated method stub

		Looper.prepare();

		Boolean RUN_FLAG = true;

		while(RUN_FLAG) {

			long currTime = System.currentTimeMillis();

			if(threadStream.getState().name().equals(MainActivity.TH_STATE_TERM) && threadValidateLocation.getState().name().equals(MainActivity.TH_STATE_TERM) && (currTime - threadStartTime) > MainActivity.TH_SPLASH_MIN_DURATION) {
				RUN_FLAG = false;
			}

			try {
				Thread.sleep(MainActivity.TH_CHECKER_DURATION);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		threadHandler.sendEmptyMessage(0);

	}

	@Override
	public void setRunFlag(Boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void storeClassVariables() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initUIHandles() {
		// TODO Auto-generated method stub
		ivLoading = (ImageView)v.findViewById(R.id.iv_loading);
		ivLoadingBg = (ImageView)v.findViewById(R.id.iv_loading_bg);
		rlLoadingC = (RelativeLayout)v.findViewById(R.id.rl_loading_c);
		tvPoweredBy = (TextView)v.findViewById(R.id.poweredby);
	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub

		Random r = new Random();
		int i1 = (r.nextInt(MainActivity.LOADING_PIC_COUNT) + 0);

		if(i1 == 0) {

			ivLoadingBg.setImageResource(R.drawable.loading0);

		} else if(i1 == 1) {

			ivLoadingBg.setImageResource(R.drawable.loading0);

		} else if(i1 == 2) {

			ivLoadingBg.setImageResource(R.drawable.loading0);

		}

		ivLoading.setBackgroundResource(R.drawable.loading_gif);
		animation = (AnimationDrawable) ivLoading.getBackground();
		animation.start();
	}

}
