package de.wangchao.musicplayer.db;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.wangchao.musicplayer.type.Music;

public class DataBase {
	private static final String DB_NAME = "de.wangchao.musicplayer.db";
	private static final String TABLE_ONLINE_MUSIC = "onlinemusic";
	private static final String TABLE_MY_FAV = "myfav";
	private Context context;
	
	public DataBase(Context _context) {

        this.context = _context;
        create();
    }
	
	private void create(){
		 SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		 db.execSQL("CREATE TABLE IF NOT EXISTS "
	                + TABLE_ONLINE_MUSIC
	                + " (uid INTEGER PRIMARY KEY AUTOINCREMENT, songid INTEGER, songname VARCHAR, album VARCHAR, length INTEGER, fileurl VARCHAR,"
	                + " lrcurl VARCHAR, pic VARCHAR, sname VARCHAR)");
		 db.execSQL("CREATE TABLE IF NOT EXISTS "
	                + TABLE_MY_FAV
	                + " (uid INTEGER PRIMARY KEY AUTOINCREMENT, songid INTEGER, songname VARCHAR, album VARCHAR, length INTEGER, fileurl VARCHAR,"
	                + " lrcurl VARCHAR, pic VARCHAR, sname VARCHAR)");
		 db.close();
	}
	
	public void addOnlineMusic(ArrayList<Music> list){
		 SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
         for (int i = 0; i < list.size(); i++) {
            try {
                ContentValues values = new ContentValues();
                values.putAll(new MusicBuilder().deconstruct(list.get(i)));
                db.insert(TABLE_ONLINE_MUSIC, null, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
         }
         db.close();
	}
	
	public ArrayList<Music> getOnlineMusic(){
		ArrayList<Music> music = new ArrayList<Music>();
        MusicBuilder builder = new MusicBuilder();
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        Cursor query = db.query(TABLE_ONLINE_MUSIC, null, null, null, null, null, null);
        if (query != null) {
            query.moveToFirst();
            while (!query.isAfterLast()) {
                music.add(builder.build(query));
                query.moveToNext();
            }
        }
        query.close();
        db.close();
        return music;
	}
	
	public void delOnlineMusic(){
		 SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
	     db.delete(TABLE_ONLINE_MUSIC, null, null);
	     db.close();
	}
	
	public boolean addFav(Music music){
		 ArrayList<Music> list=getFav();
		 for(int i=0;i<list.size();i++){
			 if(list.get(i).getWebFile().equals(music.getWebFile()))
				 return false;
		 }
		 SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);        
         try {
             ContentValues values = new ContentValues();
             values.putAll(new MusicBuilder().deconstruct(music));
             db.insert(TABLE_MY_FAV, null, values);
             db.close();
             return true;
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }    
	}
	
	public ArrayList<Music> getFav(){
		ArrayList<Music> fav = new ArrayList<Music>();
        MusicBuilder builder = new MusicBuilder();
        SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        Cursor query = db.query(TABLE_MY_FAV, null, null, null, null, null, null);
        if (query != null) {
            query.moveToFirst();
            while (!query.isAfterLast()) {
                fav.add(builder.build(query));
                query.moveToNext();
            }
        }
        query.close();
        db.close();
        return fav;
	}
	
	public boolean delFav(Music music){
		SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        String sql = "delete from " + TABLE_MY_FAV + " where fileurl='" + music.getWebFile()+"'";
        try {
            db.execSQL(sql);
            db.close();
            return true;
        } catch (Exception ex) {
            db.close();
            return false;
        }
	}
	
	

}
