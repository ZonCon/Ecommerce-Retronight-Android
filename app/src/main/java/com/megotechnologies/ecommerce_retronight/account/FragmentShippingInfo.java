package com.megotechnologies.ecommerce_retronight.account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.megotechnologies.ecommerce_retronight.FragmentMeta;
import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.R;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCFragmentLifecycle;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCRunnable;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;
import com.megotechnologies.ecommerce_retronight.utilities.Validator;

import java.util.ArrayList;
import java.util.HashMap;

public class FragmentShippingInfo extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	Boolean RUN_FLAG = false;
	Thread countryTh, stateTh, cityTh;

	TextView tvName, tvPhone, tvTitle, tvAddress, tvPincode;
	EditText editName, editPhone, editAddress, editPincode;
	TextView butSave;

	String[] arrNil = new String[]{""};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		v =  inflater.inflate(R.layout.fragment_shipping_info, container, false);

		storeClassVariables();
		initUIHandles();
		initUIListeners();
		formatUI();

		loadContact();

		return v;

	}

	public void loadContact() {

		HashMap<String, String> mapName = new HashMap<String, String>();
		mapName.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_NAME);

		HashMap<String, String> mapAddress = new HashMap<String, String>();
		mapAddress.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_ADDRESS);

		HashMap<String, String> mapPincode = new HashMap<String, String>();
		mapPincode.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_PINCODE);

		HashMap<String, String> mapPhone = new HashMap<String, String>();
		mapPhone.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_PHONE);

		ArrayList<HashMap<String, String>> recordsName = null, recordsPhone = null, recordsPincode = null, recordsAddress = null;

		if(dbC.isOpen()) {
			dbC.isAvailale();
			recordsName = dbC.retrieveRecords(mapName);
			recordsPhone = dbC.retrieveRecords(mapPhone);
			recordsPincode = dbC.retrieveRecords(mapPincode);
			recordsAddress = dbC.retrieveRecords(mapAddress);
		}


		if(recordsName.size() > 0) {

			mapName = recordsName.get(0);
			MLog.log("Name=" + mapName.get(MainActivity.DB_COL_NAME));
			editName.setText(mapName.get(MainActivity.DB_COL_NAME));

		}

		if(recordsPhone.size() > 0) {

			mapPhone = recordsPhone.get(0);
			editPhone.setText(mapPhone.get(MainActivity.DB_COL_PHONE));

		}

		if(recordsPincode.size() > 0) {

			mapPincode = recordsPincode.get(0);
			editPincode.setText(mapPincode.get(MainActivity.DB_COL_NAME));

		}

		if(recordsAddress.size() > 0) {


			mapAddress = recordsAddress.get(0);
			editAddress.setText(mapAddress.get(MainActivity.DB_COL_NAME));
			MLog.log("Retrieved Address = " + editAddress.getText());

		}

		if(recordsPhone.size() > 0) {

			mapPhone = recordsPhone.get(0);
			editPhone.setText(mapPhone.get(MainActivity.DB_COL_PHONE));

		}
	}

	@Override
	public void initUIHandles() {
		// TODO Auto-generated method stub
		editName = (EditText)v.findViewById(R.id.edit_name);
		editPhone = (EditText)v.findViewById(R.id.edit_phone);
		editAddress = (EditText)v.findViewById(R.id.edit_address);
		editPincode = (EditText)v.findViewById(R.id.edit_pincode);
		tvName = (TextView)v.findViewById(R.id.tv_label_name);
		tvPhone = (TextView)v.findViewById(R.id.tv_label_phone);
		tvAddress = (TextView)v.findViewById(R.id.tv_label_address);
		tvPincode = (TextView)v.findViewById(R.id.tv_label_pincode);
		tvTitle = (TextView)v.findViewById(R.id.tv_label_title);
		butSave = (TextView)v.findViewById(R.id.but_save);
	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub

		butSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if(editPhone.getText().toString().length() > 0 && editName.getText().toString().length() > 0 && editAddress.getText().toString().length() > 0 && editPincode.getText().toString().length() > 0) {

					
					if(!Validator.isValidPhone(editPhone.getText().toString())) {

						AlertDialog.Builder builder = new AlertDialog.Builder(activity.context);
						builder.setMessage(MainActivity.MSG_PHONE_INVALID);
						builder.setPositiveButton(MainActivity.MSG_OK, null);
						builder.show();
						return;

					}

					if(!Validator.isValidPincode(editPincode.getText().toString())) {

						AlertDialog.Builder builder = new AlertDialog.Builder(activity.context);
						builder.setMessage(MainActivity.MSG_PINCODE_INVALID);
						builder.setPositiveButton(MainActivity.MSG_OK, null);
						builder.show();
						return;

					}
					
					AlertDialog.Builder builder = new AlertDialog.Builder(activity.context);
					builder.setMessage(MainActivity.MSG_CONFIRM);
					builder.setPositiveButton(MainActivity.MSG_YES, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

							if(dbC.isOpen()) {
								dbC.isAvailale();
								HashMap<String, String> mapName = new HashMap<String, String>();
								HashMap<String, String> mapAddress = new HashMap<String, String>();
								HashMap<String, String> mapPhone = new HashMap<String, String>();
								HashMap<String, String> mapPincode = new HashMap<String, String>();

								mapName.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_NAME);
								dbC.deleteRecord(mapName);
								mapName.put(MainActivity.DB_COL_NAME, editName.getText().toString());
								dbC.insertRecord(mapName);

								mapAddress.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_ADDRESS);
								dbC.deleteRecord(mapAddress);
								mapAddress.put(MainActivity.DB_COL_NAME, editAddress.getText().toString());
								dbC.insertRecord(mapAddress);

								mapPhone.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_PHONE);
								dbC.deleteRecord(mapPhone);
								mapPhone.put(MainActivity.DB_COL_PHONE, editPhone.getText().toString());
								dbC.insertRecord(mapPhone);

								mapPincode.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_MY_PINCODE);
								dbC.deleteRecord(mapPincode);
								mapPincode.put(MainActivity.DB_COL_NAME, editPincode.getText().toString());
								dbC.insertRecord(mapPincode);							

								MLog.log("Name=" + editName.getText().toString());
								MLog.log("Address=" + editAddress.getText().toString());
								MLog.log("Phone=" + editPhone.getText().toString());
								MLog.log("Pincode=" + editPincode.getText().toString());

								dbC.printRecords();
							}

							activity.getFragmentManager().beginTransaction().remove(FragmentShippingInfo.this).commit();

						}

					});
					builder.setNegativeButton(MainActivity.MSG_NO, null);
					builder.show();

				} else {

					AlertDialog.Builder builder = new AlertDialog.Builder(activity.context);
					builder.setMessage(MainActivity.MSG_BLANK_FIELDS);
					builder.setPositiveButton(MainActivity.MSG_OK, null);
					builder.show();

				}

			}

		});

	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub
		tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvPhone.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvPincode.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvAddress.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TITLE));
		editName.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		editPhone.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		editPincode.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
		editAddress.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int) (MainActivity.TEXT_SIZE_TILE));
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
