package de.wangchao.musicplayer;

import java.io.File;

import com.commonsware.cwac.cache.AsyncCache;
import de.wangchao.musicplayer.thumbnail.MySimpleWebImageCache;
import de.wangchao.musicplayer.thumbnail.ThumbnailBus;
import de.wangchao.musicplayer.thumbnail.ThumbnailMessage;
import de.wangchao.musicplayer.util.Tools;

import android.app.Application;
import android.os.Environment;

public class OnlineMusicApplication extends Application{
	 public static final String CACHE_ROOT = Environment.getExternalStorageDirectory()+"/onlinemusic/cache/";
	 private OnlineMusicApi onlineMusicApi;
	 @Override
	 public void onCreate() {
		 Tools.checkFolderAvailable(CACHE_ROOT);
		 onlineMusicApi=new OnlineMusicApi();
	 }
	 @Override
     public void onTerminate() {
        super.onTerminate();
     }
	 
	 public OnlineMusicApi getOnlineMusicApi() {

	        return onlineMusicApi;
	 }
	 
	 private static final int MAX_SIZE = 101;
     private static final int MAX_STORE_TIME = 1000 * 60 * 60 * 24 * 7;
     private ThumbnailBus bus = new ThumbnailBus();
     private AsyncCache.DiskCachePolicy policy = new AsyncCache.DiskCachePolicy() {
         public boolean eject(File file) {

             return (System.currentTimeMillis() - file.lastModified() > MAX_STORE_TIME);
         }
     };
     private MySimpleWebImageCache<ThumbnailBus, ThumbnailMessage> cache = new MySimpleWebImageCache<ThumbnailBus, ThumbnailMessage>(
            new File(CACHE_ROOT), policy, MAX_SIZE, bus);

     public MySimpleWebImageCache<ThumbnailBus, ThumbnailMessage> getCache() {
        return (cache);
     }
}
