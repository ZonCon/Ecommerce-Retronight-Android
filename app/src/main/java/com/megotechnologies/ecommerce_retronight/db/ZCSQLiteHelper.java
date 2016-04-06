package com.megotechnologies.ecommerce_retronight.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.megotechnologies.ecommerce_retronight.MainActivity;
import com.megotechnologies.ecommerce_retronight.utilities.MLog;

public class ZCSQLiteHelper extends SQLiteOpenHelper {

	public ZCSQLiteHelper(Context context) {
		super(context, MainActivity.DB_NAME, null, MainActivity.DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
		String CREATE_SQL = "";
		
		CREATE_SQL += "create table if not exists " + MainActivity.DB_TABLE + " (";
		
		for(int i = 0; i < MainActivity.DB_ALL_COL.length; i++) {
			
			if(i == 0) {
			
				CREATE_SQL += (MainActivity.DB_ALL_COL[i] + " integer primary key autoincrement,");
				
			} else if(i == (MainActivity.DB_ALL_COL.length - 1)) {
				
				CREATE_SQL += (MainActivity.DB_ALL_COL[i] + " text");
				
			} else {
				
				CREATE_SQL += (MainActivity.DB_ALL_COL[i] + " text,");
				
			}
			
		}
		
		CREATE_SQL += ")";

		MLog.log(CREATE_SQL);
		db.execSQL(CREATE_SQL);
		
		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		MLog.log("Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + MainActivity.DB_TABLE);
	    onCreate(db);
	}

} 

