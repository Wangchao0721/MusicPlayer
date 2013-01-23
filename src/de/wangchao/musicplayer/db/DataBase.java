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
	//private static final String TABLE_PLAY_LIST = "myplaylist";
	//private static final String TABLE_MUSIC_IN_PLAY_LIST = "mylistmusic";
	private static final String TABLE_PLAYLIST = "playlist";
	private static final String TABLE_MYLIST_PREFIX = "myplaylist";
	private Context context;
	
	public DataBase(Context _context) {

        this.context = _context;
        create();
    }
	
	private void create(){
		 SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		/* db.execSQL("DROP TABLE IF EXISTS "+TABLE_ONLINE_MUSIC);
		 db.execSQL("DROP TABLE IF EXISTS "+TABLE_MY_FAV);
		 db.execSQL("DROP TABLE IF EXISTS "+TABLE_PLAYLIST);*/
		 db.execSQL("CREATE TABLE IF NOT EXISTS "
	                + TABLE_ONLINE_MUSIC
	                + " (uid INTEGER PRIMARY KEY AUTOINCREMENT, songid INTEGER, songname VARCHAR, album VARCHAR, length INTEGER, fileurl VARCHAR NOT NULL UNIQUE,"
	                + " lrcurl VARCHAR, pic VARCHAR, sname VARCHAR, fromnet VARCHAR)");
		 db.execSQL("CREATE TABLE IF NOT EXISTS "
	                + TABLE_MY_FAV
	                + " (uid INTEGER PRIMARY KEY AUTOINCREMENT, songid INTEGER, songname VARCHAR, album VARCHAR, length INTEGER, fileurl VARCHAR NOT NULL UNIQUE,"
	                + " lrcurl VARCHAR, pic VARCHAR, sname VARCHAR, fromnet VARCHAR)");
		/* db.execSQL("CREATE TABLE IF NOT EXISTS "
				    + TABLE_PLAY_LIST
				    +" (listid INTEGER PRIMARY KEY AUTOINCREMENT, listname VARCHAR)");
		 db.execSQL("CREATE TABLE IF NOT EXISTS "
				    + TABLE_MUSIC_IN_PLAY_LIST
				    +" (uid INTEGER PRIMARY KEY AUTOINCREMENT, listid INTEGER, songid INTEGER)");*/
		 db.execSQL("CREATE TABLE IF NOT EXISTS "
				    +TABLE_PLAYLIST
				    +" (uid INTEGER PRIMARY KEY AUTOINCREMENT, listtable VARCHAR, listname VARCHAR)");
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
	
	public boolean creatNewPlayList(String listname){
		 SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		 int node=1;
		 Cursor query = db.query(TABLE_PLAYLIST, null, null, null, null, null, null);
		 if (query != null) {
	            query.moveToFirst();
	            while (!query.isAfterLast()) {
	                if(listname.equals(query.getString(query.getColumnIndex("listname"))))
	                	return false;
	                if(query.getString(query.getColumnIndex("listtable")).equals(TABLE_MYLIST_PREFIX+node))
	                	node++;
	                query.moveToNext();
	            }
	     }
		 try{
			//db.beginTransaction();
		    //create new playlist table to store music
		    String table_name=TABLE_MYLIST_PREFIX+ (node);
		    db.execSQL("CREATE TABLE IF NOT EXISTS "
	                + table_name
	                + " (uid INTEGER PRIMARY KEY AUTOINCREMENT, songid INTEGER, songname VARCHAR, album VARCHAR, length INTEGER, fileurl VARCHAR NOT NULL UNIQUE,"
	                + " lrcurl VARCHAR, pic VARCHAR, sname VARCHAR, fromnet VARCHAR)");
		    //add the new playlist table to the table list
		    db.execSQL("insert into "+ TABLE_PLAYLIST +" (listtable, listname) values ('"+table_name+"', '"+listname+"')");
		    //db.endTransaction();
		    query.close();
		    db.close();
		    return true;
		 }
		 catch (Exception ex) {
			    query.close();
	            db.close();
	            return false;
	     }
	}
	
	public ArrayList<String> getMyPlayList(){
		 ArrayList<String> playlistnamelist=new ArrayList<String>();
		 SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		 Cursor query = db.query(TABLE_PLAYLIST, null, null, null, null, null, null);
		 if (query != null) {
	            query.moveToFirst();
	            while (!query.isAfterLast()) {
	            	playlistnamelist.add(query.getString(query.getColumnIndex("listname")));
	                query.moveToNext();
	            }
	     }
		 query.close();
		 db.close();
		 return playlistnamelist;
	}

	public boolean delMyPlayList(String listname){
		 SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		 Cursor query = db.query(TABLE_PLAYLIST, null, null, null, null, null, null);
		 String table_name="";
		 if (query != null) {
	            query.moveToFirst();
	            while (!query.isAfterLast()) {
	                if(listname.equals(query.getString(query.getColumnIndex("listname")))){
	                	table_name=query.getString(query.getColumnIndex("listtable"));
	                	break;
	                }
	                query.moveToNext();
	            }
	     }
		 query.close();
		 if(table_name==null||table_name.equals("")){
			 db.close();
			 return false;
		 }
			 
		 try{
		     db.execSQL("DROP TABLE IF EXISTS "+table_name);
		     db.execSQL("delete from "+TABLE_PLAYLIST+" where listname='"+listname+"'");
		     db.close();
		     return true;
		 }catch(Exception ex){
			db.close();
			return false;
		 }
	}
	
	public boolean addMusicToPlayList(ArrayList<Music> musiclist,String listname){
		SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		 Cursor query = db.query(TABLE_PLAYLIST, null, null, null, null, null, null);
		 String table_name="";
		 if (query != null) {
	            query.moveToFirst();
	            while (!query.isAfterLast()) {
	                if(listname.equals(query.getString(query.getColumnIndex("listname")))){
	                	table_name=query.getString(query.getColumnIndex("listtable"));
	                	break;
	                }
	                query.moveToNext();
	            }
	     }
		 query.close();
		 
		 //ArrayList<Music> data=getMusicInPlayList(listname);
		 try {
		    for(Music music : musiclist){
			// boolean insert=true;
			 //judge the music is already in the list?
			/* for(Music m : data){
				 if(m.getWebFile().equals(music.getWebFile())){
					 insert=false;
					 break;
				 }
			 }*/
			 //if(insert){
				 
		             ContentValues values = new ContentValues();
		             values.putAll(new MusicBuilder().deconstruct(music));
		             db.insert(table_name, null, values);
		            
		         
			// }
		     }
		    db.close();
            return true;
		 } catch (Exception e) {
             e.printStackTrace();
             return false;
         }    
	}
	
	public ArrayList<Music> getMusicInPlayList(String listname){
		 SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		 String table_name="";
		 Cursor query = db.query(TABLE_PLAYLIST, null, null, null, null, null, null);
		 if (query != null) {
	            query.moveToFirst();
	            while (!query.isAfterLast()) {
	                if(listname.equals(query.getString(query.getColumnIndex("listname")))){
	                	table_name=query.getString(query.getColumnIndex("listtable"));
	                	break;
	                }
	                query.moveToNext();
	            }
	     }
		 query.close();		 
		 if(table_name==null||table_name.equals("")){
			 db.close();
			 return null;
		 }
		//get music from playlist
		 ArrayList<Music> music=new ArrayList<Music>();
		 query=db.query(table_name, null, null, null, null, null, null);
		 if (query != null) {
	            query.moveToFirst();
	            while (!query.isAfterLast()) {
	            	music.add(new MusicBuilder().build(query));
	                query.moveToNext();
	            }
	     }
		 query.close();
		 db.close();
		 return music;
	}

	public boolean delMusicInPlayList(ArrayList<Music> musiclist,String listname){
		SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		Cursor query = db.query(TABLE_PLAYLIST, null, null, null, null, null, null);
		String table_name="";
		 if (query != null) {
	            query.moveToFirst();
	            while (!query.isAfterLast()) {
	                if(listname.equals(query.getString(query.getColumnIndex("listname")))){
	                	table_name=query.getString(query.getColumnIndex("listtable"));
	                	break;
	                }
	                query.moveToNext();
	            }
	     }
		 query.close();
        try {
        	for(Music music:musiclist){
        		 String sql = "delete from " + table_name + " where fileurl='" + music.getWebFile()+"'";
                 db.execSQL(sql);
        	}
            db.close();
            return true;
        } catch (Exception ex) {
            db.close();
            return false;
        }
	}
}
