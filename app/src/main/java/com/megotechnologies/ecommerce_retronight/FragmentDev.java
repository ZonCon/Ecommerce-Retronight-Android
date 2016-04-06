package com.megotechnologies.ecommerce_retronight;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.megotechnologies.ecommerce_retronight.interfaces.ZCFragmentLifecycle;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCRunnable;

public class FragmentDev extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	public Boolean isBegin = false;
	Boolean RUN_FLAG = false;
	Thread loginTh;

	TextView tvEmailLab, tvEmail, tvTitle, tvPhoneLab, tvPhone;

	Button butSave, butForgot, butCreate;

	String[] arrNil = new String[]{""};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		v =  inflater.inflate(R.layout.fragment_dev, container, false);
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
		tvTitle = (TextView)v.findViewById(R.id.tv_label_title);
		tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
		
		tvEmail = (TextView)v.findViewById(R.id.tv_email);
		tvEmailLab = (TextView)v.findViewById(R.id.tv_label_email);
		tvPhone = (TextView)v.findViewById(R.id.tv_phone);
		tvPhoneLab = (TextView)v.findViewById(R.id.tv_label_phone);
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

				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.megotechnologies.com"));
				startActivity(browserIntent);
				
			}

		});

	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub

		tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
		tvEmailLab.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvEmail.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
		tvPhoneLab.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvPhone.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));

		butSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		butSave.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.dark_shadow_background));
		butSave.setPadding(MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2, MainActivity.SPACING/2);
		
	}

	@Override
	public void storeClassVariables() {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRunFlag(Boolean value) {
		// TODO Auto-generated method stub
		
	};


}
