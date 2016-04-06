package com.megotechnologies.ecommerce_retronight.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;

public class DbConnection {

	// Database fields
	
	private SQLiteDatabase database;
	private ZCSQLiteHelper dbHelper;
	
	public DbConnection(Context context) {
		dbHelper = new ZCSQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}
	
	public Boolean isOpen() {
		
		if(database == null) {
			return false;
		} else {
			return database.isOpen();
		}
		
	}
	
	public Boolean isAvailale() {
		
		while(database.isDbLockedByCurrentThread() || database.isDbLockedByOtherThreads()) {
	        //db is locked, keep looping
			try {
				Random r = new Random();
				int i1 = r.nextInt(200 - 0) + 0;
				
				Thread.sleep(i1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		return true;
		
	}

	public void insertRecord(HashMap<String, String> map) {

		ContentValues values = new ContentValues();

		Iterator i = map.keySet().iterator();
		while(i.hasNext()) {

			String key = (String)i.next();
			String value = (String)map.get(key);
			values.put(key, value);

		}

		database.insert(MainActivity.DB_TABLE, null, values);

	}

	public void deleteRecord(HashMap<String, String> map) {

		String WHERE_STR = "";
		Iterator i = map.keySet().iterator();
		int j = 0;
		while(i.hasNext()) {

			String key = (String)i.next();
			String value = (String)map.get(key);

			if(j == (map.size()- 1)) {

				WHERE_STR += (key + " = '" + value + "'");

			} else {

				WHERE_STR += (key + " = '" + value + "' and ");

			}

			j++;
		}

		database.delete(MainActivity.DB_TABLE, WHERE_STR, null);

	}

	public void updateRecord(HashMap<String, String> map, HashMap<String, String> mapWhere) {

		ContentValues values = new ContentValues();

		Iterator i = map.keySet().iterator();
		while(i.hasNext()) {

			String key = (String)i.next();
			String value = (String)map.get(key);
			values.put(key, value);

		}

		String WHERE_STR = "";
		i = mapWhere.keySet().iterator();
		int j = 0;
		while(i.hasNext()) {

			String key = (String)i.next();
			String value = (String)mapWhere.get(key);

			if(j == (mapWhere.size()- 1)) {

				WHERE_STR += (key + " = '" + value + "'");

			} else {

				WHERE_STR += (key + " = '" + value + "' and ");

			}

			j++;
		}

		MLog.log(WHERE_STR);

		database.update(MainActivity.DB_TABLE, values, WHERE_STR, null);

	}

	public ArrayList<HashMap<String, String>> retrieveDB() {

		Cursor cursor = database.query(MainActivity.DB_TABLE, MainActivity.DB_ALL_COL, null, null, null, null, null);

		ArrayList<HashMap<String, String>> arrRet = new ArrayList<HashMap<String,String>>();

		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {

			HashMap<String, String> mapRet = new HashMap<String, String>();
			for(int j = 0; j < cursor.getColumnCount(); j++) {

				String key = cursor.getColumnName(j);
				String value = cursor.getString(j);
				mapRet.put(key, value);

			}
			arrRet.add(mapRet);

		}

		return arrRet;

	}

	public ArrayList<HashMap<String, String>> retrieveRecords(HashMap<String, String> map) {

		String WHERE_STR = "";
		Iterator i = map.keySet().iterator();
		int j = 0;
		while(i.hasNext()) {

			String key = (String)i.next();
			String value = (String)map.get(key);

			if(j == (map.size()- 1)) {

				WHERE_STR += (key + " = '" + value + "'");

			} else {

				WHERE_STR += (key + " = '" + value + "' and ");

			}

			j++;
		}

		Cursor cursor = database.query(MainActivity.DB_TABLE, MainActivity.DB_ALL_COL, WHERE_STR, null, null, null, null);

		ArrayList<HashMap<String, String>> arrRet = new ArrayList<HashMap<String,String>>();

		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {

			HashMap<String, String> mapRet = new HashMap<String, String>();
			for(j = 0; j < cursor.getColumnCount(); j++) {

				String key = cursor.getColumnName(j);
				String value = cursor.getString(j);
				mapRet.put(key, value);
				//MLog.log("Retrieving " + key + " " + value);

			}
			arrRet.add(mapRet);

			cursor.moveToNext();

		}

		return arrRet;

	}

	public String retrieveId(HashMap<String, String> map) {

		String WHERE_STR = "";
		Iterator i = map.keySet().iterator();
		int j = 0;
		while(i.hasNext()) {

			String key = (String)i.next();
			String value = (String)map.get(key);

			if(j == (map.size()- 1)) {

				WHERE_STR += (key + " = '" + value + "'");

			} else {

				WHERE_STR += (key + " = '" + value + "' and ");

			}

			j++;
		}

		//MLog.log("Retrieving ID where " + WHERE_STR);

		Cursor cursor = database.query(MainActivity.DB_TABLE, MainActivity.DB_ALL_COL, WHERE_STR, null, null, null, null);
		if(cursor.getCount() > 0) {
			cursor.moveToFirst();
			return cursor.getString(0);
		} else {
			return null;
		}

	}

	public void clearPendingCartRecords() {

		MLog.log("Insode clear pending cart records");
		
		String WHERE_CART_STR = "";

		WHERE_CART_STR += MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_CART + "' and ";
		WHERE_CART_STR += MainActivity.DB_COL_CART_CART_ISOPEN + " = '" + MainActivity.DB_RECORD_VALUE_CART_OPEN + "'";
		
		Cursor cursorCart = database.query(MainActivity.DB_TABLE, MainActivity.DB_ALL_COL, WHERE_CART_STR, null, null, null, null);
		if(cursorCart.getCount() > 0) {

			cursorCart.moveToFirst();

			long timeCurrent = System.currentTimeMillis();
			String strTime = cursorCart.getString(cursorCart.getColumnIndex(MainActivity.DB_COL_TIMESTAMP));


			MLog.log("Cart time =" + strTime);

			
			if(strTime != null) {

				long cartTime = Long.parseLong(strTime);

				MLog.log("Cart time difference = " + (timeCurrent - cartTime));
				
				if((timeCurrent - cartTime) > MainActivity.TIMEOUT_CART_CLEAR) {

					String _idCart = cursorCart.getString(cursorCart.getColumnIndex(MainActivity.DB_COL_ID));
					String WHERE_STR = "";
					WHERE_STR += MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_CART_ITEM + "' and ";
					WHERE_STR += MainActivity.DB_COL_FOREIGN_KEY + " = '" + _idCart + "'";
					database.delete(MainActivity.DB_TABLE, WHERE_STR, null);

					database.delete(MainActivity.DB_TABLE, WHERE_CART_STR, null);
				}

			} else {
				
				String _idCart = cursorCart.getString(cursorCart.getColumnIndex(MainActivity.DB_COL_ID));
				String WHERE_STR = "";
				WHERE_STR += MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_CART_ITEM + "' and ";
				WHERE_STR += MainActivity.DB_COL_FOREIGN_KEY + " = '" + _idCart + "'";
				database.delete(MainActivity.DB_TABLE, WHERE_STR, null);

				database.delete(MainActivity.DB_TABLE, WHERE_CART_STR, null);
				
			}

		}


	}

	public void clearDynamicRecords() {

		String WHERE_STR = MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_ITEM + "' or "
				+ MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_PICTURE + "' or "
				+ MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_URL + "' or "
				+ MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_LOCATION + "' or "
				+ MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_CONTACT + "' or "
				+ MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_ATTACHMENT + "' or "
				+ MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_DISCOUNT + "' or "
				+ MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_COUPON + "' or "
				+ MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_TAX_1 + "' or "
				+ MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_TAX_2 + "' or "
				+ MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_CART_ITEM + "' or "
				+ MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_CART + "' or "
				+ MainActivity.DB_COL_TYPE + " = '" + MainActivity.DB_RECORD_TYPE_STREAM + "'";

		database.delete(MainActivity.DB_TABLE, WHERE_STR, null);

		// Delete open cart if it is expired...
		clearPendingCartRecords();

	}

	public void printRecords() {

		MLog.log("Printing...");
		Cursor cursor = database.query(MainActivity.DB_TABLE, MainActivity.DB_ALL_COL, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()) {

			String str = "";
			for(int i = 0; i < cursor.getColumnCount(); i++) {

				str += cursor.getColumnName(i) + "=" + cursor.getString(i) + " "; 

			}

			MLog.log(str);
			cursor.moveToNext();
		}


	}

}
