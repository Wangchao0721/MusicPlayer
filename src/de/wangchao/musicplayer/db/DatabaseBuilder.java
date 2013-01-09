package de.wangchao.musicplayer.db;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class DatabaseBuilder <T>{

	public abstract T build(Cursor c);
	public abstract ContentValues deconstruct(T t);
}
