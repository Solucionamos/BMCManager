package com.solucionamos.bmcmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.solucionamos.bmcmanager.model.Server;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Android.DB";
    private static final String SERVER_TABLE_NAME = "Server";
    private static final String SERVER_COLUMN_PROTOCOL = "protocol";
    private static final String SERVER_COLUMN_MODEL = "model";
    private static final String SERVER_COLUMN_ADDRESS = "address";
    private static final String SERVER_COLUMN_NAME = "name";
    private static final String SERVER_COLUMN_USERNAME = "username";
    private static final String SERVER_COLUMN_PASSWORD = "password";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table Server "
                + "(id integer primary key autoincrement, protocol text, model text, address text,name text,username text, password text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Server");
        onCreate(db);

    }

    public void insertServer(Server el) {
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
    }

    public ArrayList<Server> getAllServers() {
        ArrayList<Server> array_list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + SERVER_TABLE_NAME, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
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

    public void deleteServer(String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("server", "name = ? ", new String[]{name});
        db.close();
    }

    public Server getServer(String name) {
        Server s = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + SERVER_TABLE_NAME + " where name='" + name + "';", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
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
