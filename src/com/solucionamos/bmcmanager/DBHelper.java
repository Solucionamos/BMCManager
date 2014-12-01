package com.solucionamos.bmcmanager;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.solucionamos.bmcmanager.model.Server;

public class DBHelper extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "Android.DB";
	public static final String SERVER_TABLE_NAME = "Server";
	public static final String SERVER_COLUMN_PROTOCOL = "protocol";
	public static final String SERVER_COLUMN_MODEL = "model";
	public static final String SERVER_COLUMN_ID = "id";
	public static final String SERVER_COLUMN_ADDRESS = "address";
	public static final String SERVER_COLUMN_NAME = "name";
	public static final String SERVER_COLUMN_USERNAME = "username";
	public static final String SERVER_COLUMN_PASSWORD = "password";

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("create table Server "
				+ "(id integer primary key autoincrement, protocol text, model text, address text,name text,username text, password text);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS Server");
		onCreate(db);

	}

	public boolean insertServer(Server el) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		
		contentValues.put(SERVER_COLUMN_NAME, el.getName());
		contentValues.put(SERVER_COLUMN_PROTOCOL, el.getProtocol());
		contentValues.put(SERVER_COLUMN_MODEL, el.getModel());
		contentValues.put(SERVER_COLUMN_ADDRESS, el.getAddress());
		contentValues.put(SERVER_COLUMN_USERNAME, el.getUsername());
		contentValues.put(SERVER_COLUMN_PASSWORD, el.getPassword());

		db.insert(SERVER_TABLE_NAME, null, contentValues);
		db.close();
		return true;
	}

	public ArrayList<Server> getAllServers() {
		ArrayList<Server> array_list = new ArrayList<Server>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery("select * from " + SERVER_TABLE_NAME, null);
		res.moveToFirst();
		
		while (res.isAfterLast() == false) {
			array_list.add(new Server(res.getString(res
					.getColumnIndex(SERVER_COLUMN_PROTOCOL)), res.getString(res
					.getColumnIndex(SERVER_COLUMN_MODEL)), res.getString(res
					.getColumnIndex(SERVER_COLUMN_NAME)), res.getString(res
					.getColumnIndex(SERVER_COLUMN_ADDRESS)), res.getString(res
					.getColumnIndex(SERVER_COLUMN_USERNAME)), res.getString(res
					.getColumnIndex(SERVER_COLUMN_PASSWORD))));
			res.moveToNext();
		}
		db.close();
		return array_list;
	}

	public int deleteServer(String name) {
		SQLiteDatabase db = this.getWritableDatabase();

		int removed = db.delete("server", "name = ? ", new String[] { name });
		db.close();
		return removed;
	}

	public Server getServer(String name){
		Server s = null;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery("select * from " + SERVER_TABLE_NAME + " where name='"+ name +"';", null);
		res.moveToFirst();
		while (res.isAfterLast() == false) {
			s = new Server(res.getString(res
					.getColumnIndex(SERVER_COLUMN_PROTOCOL)), res.getString(res
					.getColumnIndex(SERVER_COLUMN_MODEL)), res.getString(res
					.getColumnIndex(SERVER_COLUMN_NAME)), res.getString(res
					.getColumnIndex(SERVER_COLUMN_ADDRESS)), res.getString(res
					.getColumnIndex(SERVER_COLUMN_USERNAME)), res.getString(res
					.getColumnIndex(SERVER_COLUMN_PASSWORD))
					);
			res.moveToNext();
		}
		db.close();
		return s;
	}
}
