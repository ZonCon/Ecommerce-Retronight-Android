package com.megotechnologies.ecommerce_retronight;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.megotechnologies.ecommerce_retronight.utilities.ImageProcessingFunctions;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;


public class ZoomImageActivity extends Activity implements OnScaleGestureListener, Runnable {

	public static String IMAGE_INTENT_KEY = "image";
	public static String IMAGE_INTENT_TRYOUT = "tryout";
	public static String IMAGE_INTENT_ZOOMABLE = "zoomable";
	Boolean IS_TRYOUT = false;
	TextView tv;
	Boolean TOUCH_FLAG = false;
	Bitmap bmp;
	ImageView iv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_zoomimage);

		TOUCH_FLAG = false;

		final String imgUrl = ZoomImageActivity.this.getIntent().getStringExtra(IMAGE_INTENT_KEY);
		IS_TRYOUT = ZoomImageActivity.this.getIntent().getBooleanExtra(IMAGE_INTENT_TRYOUT, false);

		bmp =  (new ImageProcessingFunctions()).decodeSampledBitmapFromFile(imgUrl, MainActivity.IMG_DETAIL_MAX_SIZE, MainActivity.IMG_DETAIL_MAX_SIZE);
		iv = (ImageView)findViewById(R.id.zoomImage);
		iv.setImageBitmap(bmp);

		ScaleGestureDetector gd = new ScaleGestureDetector(this, this);

		if(!IS_TRYOUT) {

			tv = (TextView)findViewById(R.id.butSave);
			tv.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					//MediaStore.Images.Media.insertImage(getContentResolver(), bmp, "" , "");
					insertImage(getContentResolver(), bmp, getApplicationContext().getString(getApplicationContext().getApplicationInfo().labelRes).replace(" ", "") + Calendar.getInstance().getTimeInMillis(), "");
					Toast.makeText(ZoomImageActivity.this, "Image Saved in Gallery Successfully!", Toast.LENGTH_SHORT).show();

				}

			});
			
			tv = (TextView)findViewById(R.id.butShare);
			tv.setVisibility(RelativeLayout.GONE);
			
		} else {
			
			tv = (TextView)findViewById(R.id.butShare);
			tv.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					Intent share = new Intent(Intent.ACTION_SEND);
					share.setType("image/jpeg");
					share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imgUrl));
					startActivity(Intent.createChooser(share, "Share Image"));
					
					//MediaStore.Images.Media.insertImage(getContentResolver(), bmp, "" , "");
					insertImage(getContentResolver(), bmp, getApplicationContext().getString(getApplicationContext().getApplicationInfo().labelRes).replace(" ", "") + Calendar.getInstance().getTimeInMillis(), "");
					Toast.makeText(ZoomImageActivity.this, "Image Saved in Gallery Successfully!", Toast.LENGTH_SHORT).show();

				}

			});
			
			tv = (TextView)findViewById(R.id.butSave);
			tv.setVisibility(RelativeLayout.GONE);

		}

		addListeners();
	}

	private static final Bitmap storeThumbnail(
			ContentResolver cr,
			Bitmap source,
			long id,
			float width, 
			float height,
			int kind) {

		// create the matrix to scale it
		Matrix matrix = new Matrix();

		float scaleX = width / source.getWidth();
		float scaleY = height / source.getHeight();

		matrix.setScale(scaleX, scaleY);

		Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
				source.getWidth(),
				source.getHeight(), matrix,
				true
				);

		ContentValues values = new ContentValues(4);
		values.put(Images.Thumbnails.KIND,kind);
		values.put(Images.Thumbnails.IMAGE_ID,(int)id);
		values.put(Images.Thumbnails.HEIGHT,thumb.getHeight());
		values.put(Images.Thumbnails.WIDTH,thumb.getWidth());

		Uri url = cr.insert(Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

		try {
			OutputStream thumbOut = cr.openOutputStream(url);
			thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
			thumbOut.close();
			return thumb;
		} catch (FileNotFoundException ex) {
			return null;
		} catch (IOException ex) {
			return null;
		}
	}

	public static final String insertImage(ContentResolver cr, 
			Bitmap source, 
			String title, 
			String description) {

		ContentValues values = new ContentValues();
		values.put(Images.Media.TITLE, title);
		values.put(Images.Media.DISPLAY_NAME, title);
		values.put(Images.Media.DESCRIPTION, description);
		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		// Add the date meta data to ensure the image is added at the front of the gallery
		values.put(Images.Media.DATE_ADDED, System.currentTimeMillis());
		values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());

		Uri url = null;
		String stringUrl = null;    /* value to be returned */

		try {
			url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

			if (source != null) {
				OutputStream imageOut = cr.openOutputStream(url);
				try {
					source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
				} finally {
					imageOut.close();
				}

				long id = ContentUris.parseId(url);
				// Wait until MINI_KIND thumbnail is generated.
				Bitmap miniThumb = Images.Thumbnails.getThumbnail(cr, id, Images.Thumbnails.MINI_KIND, null);
				// This is for backward compatibility.
				storeThumbnail(cr, miniThumb, id, 50F, 50F,Images.Thumbnails.MICRO_KIND);
			} else {
				cr.delete(url, null, null);
				url = null;
			}
		} catch (Exception e) {
			if (url != null) {
				cr.delete(url, null, null);
				url = null;
			}
		}

		if (url != null) {
			stringUrl = url.toString();
		}

		return stringUrl;
	}

	void addListeners() {

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub

	}

	android.os.Handler threadHandler = new android.os.Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			BitmapDrawable bitmapDrawable = new BitmapDrawable(bmp);
			MLog.log("Setting image..." + bmp.getWidth() + "," + bmp.getHeight());
			iv.setImageDrawable(bitmapDrawable);
			iv.postInvalidate();

		}
	};

	@Override
	public void run() {

	}
}
