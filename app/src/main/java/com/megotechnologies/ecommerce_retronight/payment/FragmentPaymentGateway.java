package com.megotechnologies.ecommerce_retronight.payment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.megotechnologies.ecommerce_retronight.FragmentMeta;
import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.R;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCFragmentLifecycle;
import com.megotechnologies.ecommerce_retronight.interfaces.ZCRunnable;
import com.megotechnologies.ecommerce_retronight.dataobjects.PGRecord;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class FragmentPaymentGateway extends FragmentMeta implements ZCFragmentLifecycle, ZCRunnable {

	String currentUrl = "";

	public PGRecord pgRecord;
	public WebView wv;

	Thread orderTh;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		v =  inflater.inflate(R.layout.fragment_payment_gateway, container, false);

		if(activity.IS_CONNECTED) {

			storeClassVariables();
			initUIHandles();
			initUIListeners();
			formatUI();

			String url = MainActivity.PG_IFRAME_URL;
			String postData = "";
			try {
				postData += "channel=10";
				postData += "&account_id=" + URLEncoder.encode(String.valueOf(MainActivity.PG_MERCHANT_ID), "utf-8");
				postData += "&reference_no=" + URLEncoder.encode(String.valueOf(pgRecord.idOrder), "utf-8");
				//postData += "&amount=" + URLEncoder.encode(String.valueOf(pgRecord.amount), "utf-8");
				//postData += "&amount=" + URLEncoder.encode(String.valueOf(12), "utf-8");
				postData += "&mode=" + URLEncoder.encode(String.valueOf("LIVE"), "utf-8");
				postData += "&currency=" + URLEncoder.encode(String.valueOf("INR"), "utf-8");
				postData += "&description=" + URLEncoder.encode(String.valueOf("INR"), "utf-8");
				postData += "&return_url=" + URLEncoder.encode(MainActivity.PG_REDIRECT_URL, "utf-8");
				postData += "&name=" + URLEncoder.encode(pgRecord.billingName, "utf-8");
				postData += "&address=" + URLEncoder.encode(pgRecord.billingAddress, "utf-8");
				postData += "&city=" + URLEncoder.encode(pgRecord.billingCity, "utf-8");
				postData += "&state=" + URLEncoder.encode(pgRecord.billingState, "utf-8");
				postData += "&country=" + URLEncoder.encode("IND", "utf-8");
				postData += "&postal_code=" + URLEncoder.encode(pgRecord.billingZip, "utf-8");
				postData += "&phone=" + URLEncoder.encode(pgRecord.billingTel, "utf-8");
				postData += "&email=" + URLEncoder.encode(pgRecord.billingEmail, "utf-8");



				MLog.log(postData);

				WebSettings ws = wv.getSettings();
				ws.getPluginState();
				ws.setPluginState(PluginState.ON);
				ws.setJavaScriptEnabled(true);
				ws.setJavaScriptCanOpenWindowsAutomatically(true);
				wv.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
				wv.setVerticalScrollBarEnabled(true);
				wv.setWebViewClient(new WebViewClient() {
					public boolean shouldOverrideUrlLoading(WebView view, String url){
						// do your handling codes here, which url is the requested url
						// probably you need to open that url rather than redirect:
						view.loadUrl(url);
						return true; // then it is not handled by default action

					}

					@Override
					public void onPageFinished(WebView view, String url) {
						// TODO Auto-generated method stub
						super.onPageFinished(view, url);
						wv.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
						MLog.log("Loaded " + url);
						currentUrl = url;

					}

				});
				wv.setWebChromeClient(new WebChromeClient() {});

				wv.postUrl(url, EncodingUtils.getBytes(postData, "base64"));


			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		} else {
			AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
			alert.setMessage(MainActivity.MSG_CART_DISCONNECTED);
			alert.setTitle("Error");
			alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//dismiss the dialog
					activity.loadShop();
				}
			});
			alert.create().show();
		}
		return v;
	}

	@Override
	public void storeClassVariables() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initUIHandles() {
		// TODO Auto-generated method stub
		wv = (WebView)v.findViewById(R.id.wv_container);

	}

	@Override
	public void initUIListeners() {
		// TODO Auto-generated method stub



	}

	@Override
	public void formatUI() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
	}

	void parseHtmlResponse(String html) {


		if(html.indexOf("Payment Status : SUCCESS") >= 0) {

			threadHandler.sendEmptyMessage(0);

		} else if(html.indexOf("Payment Status :") >= 0) {

			threadHandler.sendEmptyMessage(1);
			
		}


	}

	class MyJavaScriptInterface
	{
		@JavascriptInterface
		@SuppressWarnings("unused")
		public void processHTML(String html)
		{
			// process the html as needed by the app

			MLog.log("HTML=" + html);
			parseHtmlResponse(html);
			/*
			if(currentUrl.equals(MainActivity.PG_REDIRECT_URL)) {

				MLog.log("HTML=" + html);
				parseHtmlResponse(html);

			}
			 */

		}
	}

	protected Handler threadHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case 0:

				AlertDialog.Builder alert  = new AlertDialog.Builder(v.getContext());
				alert.setMessage(MainActivity.MSG_ORDER_SUCCESS);
				alert.setTitle("Success");
				alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						//dismiss the dialog  
						activity.loadShop();
					}
				});
				alert.create().show();

				break;

			case 1:

				alert  = new AlertDialog.Builder(v.getContext());
				alert.setMessage(MainActivity.MSG_ORDER_FAILURE);
				alert.setTitle("Alert");
				alert.setPositiveButton(MainActivity.MSG_OK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						//dismiss the dialog  
						activity.loadShop();
					}
				});
				alert.create().show();

				break;

			default:
				break;
			}


		};

	};

	@Override
	public void run() {
		// TODO Auto-generated method stub

		Looper.prepare();

		MainActivity a = (MainActivity)getActivity();
		a.handlerLoading.sendEmptyMessage(1);

		String jsonStr = "";
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;

		Thread t = Thread.currentThread();
		String tName = t.getName();
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

		if(tName.equals(MainActivity.TH_NAME_CONFIRM_ORDER)) {
			httppost = new HttpPost(MainActivity.API_ORDER_CONFIRM);
		} else {
			httppost = new HttpPost(MainActivity.API_ORDER_CANCEL);
		}
		nameValuePairs.add(new BasicNameValuePair("params", "[{ \"idProject\": \"" + MainActivity.PID + "\", \"idOrder\": \"" + pgRecord.idOrder + "\", \"email\": \"" + pgRecord.billingEmail + "\"}]"));
		MLog.log("[{ \"idProject\": \"" + MainActivity.PID + "\", \"idOrder\": \"" + pgRecord.idOrder + "\", \"email\": \"" + pgRecord.billingEmail + "\"}]"); 

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
			if(tName.equals(MainActivity.TH_NAME_CONFIRM_ORDER)) {
				msg.what = 0;
			} else if(tName.equals(MainActivity.TH_NAME_CANCEL_ORDER)) {
				msg.what = 1;
			}
			threadHandler.sendMessage(msg);
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

		a.handlerLoading.sendEmptyMessage(0);

	}

	@Override
	public void setRunFlag(Boolean value) {
		// TODO Auto-generated method stub

	}


}
