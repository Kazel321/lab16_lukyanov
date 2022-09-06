package com.example.lab16_lukyanov;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DB extends SQLiteOpenHelper {
    public DB(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE History (number INT, datetime TEXT, nick TEXT, ip TEXT, port INT, message TEXT);";
        db.execSQL(sql);
        sql = "CREATE TABLE Settings (nick TEXT, ip TEXT, portget INT, portsend INT);";
        db.execSQL(sql);
    }

    public void saveSettings(String nick, String ip, int portGet, int portSend)
    {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "DELETE FROM Settings;";
        db.execSQL(sql);
        sql = "INSERT INTO Settings VALUES ('" + nick + "', '" + ip + "', " + portGet + ", " + portSend + ");";
        db.execSQL(sql);
    }

    public String[] getSettings()
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM Settings;";
        Cursor cur = db.rawQuery(sql, null);
        if (cur.moveToFirst())
        {
            String[] settings = new String[4];
            settings[0] = cur.getString(0);
            settings[1]= cur.getString(1);
            settings[2] = String.valueOf(cur.getInt(2));
            settings[3] = String.valueOf(cur.getInt(3));
            return settings;
        }
        else return null;
    }

    public int getMaxId()
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT MAX(number) FROM History";
        Cursor cur = db.rawQuery(sql, null);
        if (cur.moveToFirst()) return cur.getInt(0);
        return -1;
    }

    public void addMessage(int number, String dateTime, String nick, String ip, int port, String message)
    {
        String sid = String.valueOf(number);
        SQLiteDatabase db = getWritableDatabase();
        String sql = "INSERT INTO History VALUES (" + sid + ", '" + dateTime + "', '" + nick + "', '" + ip + "', " + port + ", '" + message +"');";
        db.execSQL(sql);
    }

    public Message getMessage(int number)
    {
        String sid = String.valueOf(number);
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM History WHERE number = " + sid + ";";
        Cursor cur = db.rawQuery(sql, null);
        if (cur.moveToFirst()) {
            Message mes = new Message();
            mes.number = cur.getInt(0);
            mes.dateTime = cur.getString(1);
            mes.nick = cur.getString(2);
            mes.ip = cur.getString(3);
            mes.portGet = cur.getInt(4);
            mes.textMes = cur.getString(5);
            return mes;
        }
        else return null;
    }

    public void getAllHistory(ArrayList<Message> lst)
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM History;";
        Cursor cur = db.rawQuery(sql, null);
        if (cur.moveToFirst())
        {
            do {
                Message mes = new Message();
                mes.number = cur.getInt(0);
                mes.dateTime = cur.getString(1);
                mes.nick = cur.getString(2);
                mes.ip = cur.getString(3);
                mes.portGet = cur.getInt(4);
                mes.textMes = cur.getString(5);
                lst.add(mes);
            } while (cur.moveToNext());
        }
    }

    public void deleteHistory()
    {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "DELETE FROM History;";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
