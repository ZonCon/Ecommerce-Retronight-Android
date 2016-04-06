package com.megotechnologies.ecommerce_retronight.shop;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.megotechnologies.ecommerce_retronight.FragmentMeta;
import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.R;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCFragmentLifecycle;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCRunnable;
import com.megotechnologies.ecommerce_retronight.utilities.ImageProcessingFunctions;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class FragmentShopCategories extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	int IV_ID_PREFIX = 4000;

	Boolean RUN_FLAG = false;
	Thread thPictDownload;

	LinearLayout llContainer;
	VideoView videoGhana;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		v =  inflater.inflate(R.layout.fragment_shop_categories, container, false);
		activity.lastCreatedActivity = MainActivity.SCREEN_SHOP_CATEGORIES;
		activity.showHeaderFooter();

		storeClassVariables();
		initUIHandles();
		initUIListeners();
		formatUI();
		
		try {
			loadFromLocalDB();
		} catch (IllegalStateException e) {
			
		}

		return v;

	}
	
	void loadFromLocalDB() {

		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_STREAM);

		ArrayList<HashMap<String, String>> records = null;
		if(dbC.isOpen()) {
			dbC.isAvailale();
			records = dbC.retrieveRecords(map);
		}

		if(records.size() < MainActivity.NUM_INIT_STREAMS) {
			return;
		}

		llContainer.removeAllViews();

		for(int i = MainActivity.NUM_INIT_STREAMS; i < records.size();) {

			LinearLayout llRowC = new LinearLayout(activity.context);
			llRowC.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout.LayoutParams paramsLL = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			paramsLL.bottomMargin = 2;
			llRowC.setLayoutParams(paramsLL);
			llContainer.addView(llRowC);

			if(i == MainActivity.NUM_INIT_STREAMS) {

				map = records.get(i);

				//Last min test case
				if (map == null) {
					break;
				}

				final int idStreamLeft = Integer.parseInt(map.get(MainActivity.DB_COL_SRV_ID));

				int rowHeight = (int) (MainActivity.SCREEN_WIDTH / 2);
				int rowHeightLarge = (int) (MainActivity.SCREEN_WIDTH * 0.7);

				RelativeLayout rlRow = new RelativeLayout(activity.context);
				paramsLL = new LayoutParams(0, rowHeightLarge);
				paramsLL.weight = 2;
				paramsLL.setMargins(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
				rlRow.setLayoutParams(paramsLL);
				rlRow.setBackgroundColor(activity.getResources().getColor(R.color.dark_gray));
				llRowC.addView(rlRow);
				llRowC.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						if (!activity.IS_CLICKED) {

							activity.IS_CLICKED = true;

							FragmentShopItemsList frag = new FragmentShopItemsList();
							frag.idStream = idStreamLeft;
							activity.fragMgr.beginTransaction()
									.add(((ViewGroup) getView().getParent()).getId(), frag, MainActivity.SCREEN_SHOP_ITEM_LIST)
									.addToBackStack(MainActivity.SCREEN_SHOP_ITEM_LIST)
									.commit();

						}


					}

				});

				MLog.log("Name = " + map.get(MainActivity.DB_COL_NAME));

				ImageView iv = new ImageView(activity.context);
				RelativeLayout.LayoutParams rParamsIv = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
						(rowHeightLarge - (rowHeight / 5)));
				rParamsIv.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
				iv.setLayoutParams(rParamsIv);
				iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
				iv.setScaleType(ScaleType.CENTER_CROP);
				iv.setId(IV_ID_PREFIX + i);
				iv.setPadding(3, 3, 3, 0);
				iv.setCropToPadding(true);
				rlRow.addView(iv);

				TextView tv = new TextView(activity.context);
				RelativeLayout.LayoutParams rParamsTv = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, rowHeight / 5);
				rParamsTv.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
				tv.setLayoutParams(rParamsTv);
				tv.setGravity(Gravity.CENTER | Gravity.LEFT);
				tv.setText(map.get(MainActivity.DB_COL_NAME).toUpperCase());
				tv.setTextColor(getResources().getColor(R.color.text_color));
				tv.setBackgroundColor(getResources().getColor(R.color.dark_gray));
				tv.setPadding(MainActivity.SPACING, 0, 0, 0);
				tv.setTextSize(rowHeight / 15);
				tv.setLineSpacing(0.0f, 1.2f);
				rlRow.addView(tv);

				HashMap<String, String> mapItems = new HashMap<String, String>();
				mapItems.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_ITEM);
				mapItems.put(MainActivity.DB_COL_FOREIGN_KEY, map.get(MainActivity.DB_COL_ID));
				String _idItem = null;
				if (dbC.isOpen()) {
					dbC.isAvailale();
					_idItem = dbC.retrieveId(mapItems);
				}

				MLog.log("Item Id = " + _idItem);

				HashMap<String, String> mapPictures = new HashMap<String, String>();
				mapPictures.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_PICTURE);
				mapPictures.put(MainActivity.DB_COL_FOREIGN_KEY, _idItem);
				ArrayList<HashMap<String, String>> recordsPictures = null;
				if (dbC.isOpen()) {
					dbC.isAvailale();
					recordsPictures = dbC.retrieveRecords(mapPictures);
				}

				MLog.log("Pictures found = " + recordsPictures.size());

				String picture = "";
				if (recordsPictures.size() > 0) {
					mapPictures = recordsPictures.get(0);
					picture = mapPictures.get(MainActivity.DB_COL_PATH_PROC);
				}

				if (picture.length() > 0) {

					if (activity.checkIfExistsInExternalStorage(picture)) {

						String filePath = MainActivity.STORAGE_PATH + "/" + picture;
						Bitmap bmp = (new ImageProcessingFunctions()).decodeSampledBitmapFromFile(filePath, MainActivity.IMG_TH_MAX_SIZE, MainActivity.IMG_TH_MAX_SIZE);
						Message msg = new Message();
						msg.obj = bmp;
						msg.what = (IV_ID_PREFIX + i);
						MLog.log("Sending message to " + msg.what);
						threadHandler.sendMessage(msg);

					} else {

						thPictDownload = new Thread(this);
						thPictDownload.setName(picture + ";" + (IV_ID_PREFIX + i));
						thPictDownload.start();

					}

				} else {

					Message msg = new Message();
					msg.obj = null;
					msg.what = (IV_ID_PREFIX + i);
					MLog.log("Sending message to " + msg.what);
					threadHandler.sendMessage(msg);

				}
				i++;

			} else {


				for (int j = i; j < records.size() && j < i + 2; j++) {

					map = records.get(j);

					//Last min test case
					if (map == null) {
						break;
					}

					final int idStreamLeft = Integer.parseInt(map.get(MainActivity.DB_COL_SRV_ID));

					int rowHeight = (int) (MainActivity.SCREEN_WIDTH / 2);

					RelativeLayout rlRow = new RelativeLayout(activity.context);
					paramsLL = new LayoutParams(0, rowHeight);
					paramsLL.weight = 2;
					if (j % 2 == 1) {
						paramsLL.setMargins(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING / 2, 0);
					} else {
						paramsLL.setMargins(MainActivity.SPACING / 2, MainActivity.SPACING, MainActivity.SPACING, 0);
					}
					rlRow.setLayoutParams(paramsLL);
					rlRow.setBackgroundColor(activity.getResources().getColor(R.color.dark_gray));
					llRowC.addView(rlRow);
					llRowC.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub

							if (!activity.IS_CLICKED) {

								activity.IS_CLICKED = true;

								FragmentShopItemsList frag = new FragmentShopItemsList();
								frag.idStream = idStreamLeft;
								activity.fragMgr.beginTransaction()
										.add(((ViewGroup) getView().getParent()).getId(), frag, MainActivity.SCREEN_SHOP_ITEM_LIST)
										.addToBackStack(MainActivity.SCREEN_SHOP_ITEM_LIST)
										.commit();

							}


						}

					});

					MLog.log("Name = " + map.get(MainActivity.DB_COL_NAME));

					ImageView iv = new ImageView(activity.context);
					RelativeLayout.LayoutParams rParamsIv = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
							(rowHeight - (rowHeight / 5)));
					rParamsIv.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
					iv.setLayoutParams(rParamsIv);
					iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
					iv.setScaleType(ScaleType.CENTER_CROP);
					iv.setId(IV_ID_PREFIX + j);
					iv.setCropToPadding(true);
					//iv.setPadding(MainActivity.SPACING, MainActivity.SPACING, MainActivity.SPACING, 0);
					iv.setPadding(3, 3, 3, 0);
					rlRow.addView(iv);

					TextView tv = new TextView(activity.context);
					RelativeLayout.LayoutParams rParamsTv = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, rowHeight / 5);
					rParamsTv.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
					tv.setLayoutParams(rParamsTv);
					tv.setGravity(Gravity.CENTER | Gravity.LEFT);
					tv.setText(map.get(MainActivity.DB_COL_NAME).toUpperCase());
					tv.setTextColor(getResources().getColor(R.color.text_color));
					tv.setPadding(MainActivity.SPACING, 0, 0, 0);
					tv.setBackgroundColor(activity.getResources().getColor(R.color.dark_gray));
					tv.setTextSize(rowHeight / 15);
					tv.setLineSpacing(0.0f, 1.2f);
					rlRow.addView(tv);

					HashMap<String, String> mapItems = new HashMap<String, String>();
					mapItems.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_ITEM);
					mapItems.put(MainActivity.DB_COL_FOREIGN_KEY, map.get(MainActivity.DB_COL_ID));
					String _idItem = null;
					if (dbC.isOpen()) {
						dbC.isAvailale();
						_idItem = dbC.retrieveId(mapItems);
					}

					MLog.log("Item Id = " + _idItem);

					HashMap<String, String> mapPictures = new HashMap<String, String>();
					mapPictures.put(MainActivity.DB_COL_TYPE, MainActivity.DB_RECORD_TYPE_PICTURE);
					mapPictures.put(MainActivity.DB_COL_FOREIGN_KEY, _idItem);
					ArrayList<HashMap<String, String>> recordsPictures = null;
					if (dbC.isOpen()) {
						dbC.isAvailale();
						recordsPictures = dbC.retrieveRecords(mapPictures);
					}

					MLog.log("Pictures found = " + recordsPictures.size());

					String picture = "";
					if (recordsPictures.size() > 0) {
						mapPictures = recordsPictures.get(0);
						picture = mapPictures.get(MainActivity.DB_COL_PATH_PROC);
					}

					if (picture.length() > 0) {

						if (activity.checkIfExistsInExternalStorage(picture)) {

							String filePath = MainActivity.STORAGE_PATH + "/" + picture;
							Bitmap bmp = (new ImageProcessingFunctions()).decodeSampledBitmapFromFile(filePath, MainActivity.IMG_TH_MAX_SIZE, MainActivity.IMG_TH_MAX_SIZE);
							Message msg = new Message();
							msg.obj = bmp;
							msg.what = (IV_ID_PREFIX + j);
							MLog.log("Sending message to " + msg.what);
							threadHandler.sendMessage(msg);

						} else {

							thPictDownload = new Thread(this);
							thPictDownload.setName(picture + ";" + (IV_ID_PREFIX + j));
							thPictDownload.start();

						}

					} else {

						Message msg = new Message();
						msg.obj = null;
						msg.what = (IV_ID_PREFIX + j);
						MLog.log("Sending message to " + msg.what);
						threadHandler.sendMessage(msg);

					}

				}

				i+=2;

			}

		}
		
	}
	
	@Override
	public void initUIHandles() {
		// TODO Auto-generated method stub

		llContainer = (LinearLayout)v.findViewById(R.id.ll_container);
		
	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub
		activity.highlightFooter(1);
		activity.app.ENABLE_SYNC = true;
		
		//tvRight.setVisibility(RelativeLayout.GONE);
		
		//String path = "android.resource://" + activity.getPackageName() + "/" + R.raw.oil;
		//videoGhana.setVideoURI(Uri.parse(path));
		//videoGhana.start();
	}

	@Override
	public void storeClassVariables() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub

	}

	protected Handler threadHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {


			Bitmap bmp = (Bitmap)msg.obj;
			if(bmp != null) {

				MLog.log("Displaying = " + msg.what);

				ImageView iv = (ImageView)v.findViewById(msg.what);
				iv.setImageBitmap(bmp);

			} else {

				ImageView iv = (ImageView)v.findViewById(msg.what);
				iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));

			}


		}

	};

	@Override
	public void run() {
		// TODO Auto-generated method stub

		Looper.prepare();

		//activity.handlerLoading.sendEmptyMessage(1);

		Thread t = Thread.currentThread();
		String tName = t.getName();
		String[] strArr = tName.split(";");

		String url = MainActivity.UPLOADS + "/" + strArr[0];
		int id = Integer.parseInt(strArr[1]);
		Bitmap bmp = (new ImageProcessingFunctions()).decodeSampledBitmapFromStream(url, MainActivity.IMG_TH_MAX_SIZE, MainActivity.IMG_TH_MAX_SIZE);
		Message msg = new Message();
		msg.obj = bmp;
		msg.what = id;
		MLog.log("Sending message to " + id);
		threadHandler.sendMessage(msg);

		String filePath = MainActivity.STORAGE_PATH + "/" + strArr[0];
		File file = new File(filePath);
		if(file.exists()) {
			file.delete();
		}

		try {

			FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
			bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		//activity.handlerLoading.sendEmptyMessage(0);

	}


	@Override
	public void setRunFlag(Boolean value) {
		// TODO Auto-generated method stub
		RUN_FLAG = value;
	}



}
