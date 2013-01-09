package de.wangchao.musicplayer.db;

import de.wangchao.musicplayer.type.Music;
import android.content.ContentValues;
import android.database.Cursor;

public class MusicBuilder extends DatabaseBuilder<Music> {
	private static final String MUSIC_ID="songid";
	private static final String MUSIC_NAME="songname";
	private static final String MUSIC_ALBUM="album";
	private static final String MUSIC_LENGTH="length";
	private static final String MUSIC_URL="fileurl";
	private static final String MUSIC_LRC_URL="lrcurl";
	private static final String MUSIC_PIC="pic";
	private static final String MUSIC_SINGER_NAME="sname";

	@Override
	public Music build(Cursor c) {
		// TODO Auto-generated method stub
		int columnId = c.getColumnIndex(MUSIC_ID);
		int columnName = c.getColumnIndex(MUSIC_NAME);
		int columnAlbum = c.getColumnIndex(MUSIC_ALBUM);
		int columnLength = c.getColumnIndex(MUSIC_LENGTH);
		int columnUrl = c.getColumnIndex(MUSIC_URL);
		int columnLrcUrl = c.getColumnIndex(MUSIC_LRC_URL);
		int columnPic = c.getColumnIndex(MUSIC_PIC);
		int columnSname = c.getColumnIndex(MUSIC_SINGER_NAME);
		
		Music music = new Music();
		
		music.setSongId(c.getInt(columnId));
		music.setSongName(c.getString(columnName));
		music.setAlbum(c.getString(columnAlbum));
		music.setLength(c.getInt(columnLength));
		music.setWebFile(c.getString(columnUrl));
		music.setLrcUrl(c.getString(columnLrcUrl));
		music.setPic(c.getString(columnPic));
		music.setSingerName(c.getString(columnSname));
		
		return music;
	}

	@Override
	public ContentValues deconstruct(Music t) {
		// TODO Auto-generated method stub
        ContentValues contentValues = new ContentValues();
	    
		contentValues.put(MUSIC_ID, t.getSongId());
		contentValues.put(MUSIC_NAME, t.getSongName());
		contentValues.put(MUSIC_ALBUM, t.getAlbum());
		contentValues.put(MUSIC_LENGTH, t.getLength());
		contentValues.put(MUSIC_URL, t.getWebFile());
		contentValues.put(MUSIC_LRC_URL, t.getLrcUrl());
		contentValues.put(MUSIC_PIC, t.getPic());
		contentValues.put(MUSIC_SINGER_NAME, t.getSingerName());
		
		return contentValues;
	}

}
