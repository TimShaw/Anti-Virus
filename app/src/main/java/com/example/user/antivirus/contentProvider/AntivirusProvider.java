package com.example.user.antivirus.contentProvider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Created by Max on 11/02/2015.
 */
public class AntivirusProvider extends ContentProvider{
    static final String PROVIDER_NAME = "com.example.contentproviderexample.MyProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/cte";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    static final UriMatcher uriMatcher;
    private static HashMap<String, String> values;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "SMS", table.Sms.uriCodeSMS);
        uriMatcher.addURI(PROVIDER_NAME, "SMS/#", table.Sms.uriCodeSMS);
        uriMatcher.addURI(PROVIDER_NAME, "BATTERY", table.Battery.uriCodeBATTERY);
        uriMatcher.addURI(PROVIDER_NAME, "BATTERY/#", table.Battery.uriCodeBATTERY);
        uriMatcher.addURI(PROVIDER_NAME, "CONTACT", table.Contact.uriCodeCONTACT);
        uriMatcher.addURI(PROVIDER_NAME, "CONTACT/#", table.Contact.uriCodeCONTACT);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        SQLiteDatabase db = this.getDataBase(uri);
        count = db.delete(this.getDataBaseName(db), selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/cte";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = getDataBase(uri);
        long rowID = db.insert(this.getDataBaseName(db), "", values);
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(uri, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);

    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        table.Sms sms = new table.Sms(context);
        smsDb = sms.getWritableDatabase();
        if (smsDb == null){
            return false;
        }
        table.Battery battery = new table.Battery(context);
        batteryDb = battery.getWritableDatabase();
        if (batteryDb == null){
            return false;
        }

        table.Contact contact = new table.Contact(context);
        contactDb = contact.getWritableDatabase();
        if (contactDb == null){
            return false;
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = getDataBase(uri);
        qb.setTables(this.getDataBaseName(db));
                qb.setProjectionMap(values);
        /*if (sortOrder == null || sortOrder == "") {
            sortOrder = String.valueOf(SharedInformation.BatteryInformation.DATE);
        }*/
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        SQLiteDatabase db = getDataBase(uri);
        count = db.update(this.getDataBaseName(db), values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private SQLiteDatabase smsDb;
    private SQLiteDatabase batteryDb;
    private SQLiteDatabase contactDb;

    private SQLiteDatabase getDataBase (Uri uri){
        switch (uriMatcher.match(uri)){
            case table.Sms.uriCodeSMS:
                return smsDb;
            case table.Battery.uriCodeBATTERY:
                return batteryDb;
            case table.Contact.uriCodeCONTACT:
                return contactDb;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    private String getDataBaseName (SQLiteDatabase db){
        if (db == smsDb){
            return table.Sms.DATABASE_NAME;
        }
        if (db == batteryDb){
            return table.Battery.DATABASE_NAME;
        }
        if (db == contactDb){
            return table.Contact.DATABASE_NAME;
        }
        else
            throw  new IllegalArgumentException("Unsupported DATABASE: " + db);
    }
}