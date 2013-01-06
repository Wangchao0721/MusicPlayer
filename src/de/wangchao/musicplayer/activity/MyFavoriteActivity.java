package de.wangchao.musicplayer.activity;

import java.util.ArrayList;

import de.wangchao.musicplayer.OnlineMusicApplication;
import de.wangchao.musicplayer.R;
import de.wangchao.musicplayer.db.DataBase;
import de.wangchao.musicplayer.service.MusicService;
import de.wangchao.musicplayer.service.MusicService.MusicBinder;
import de.wangchao.musicplayer.thumbnail.ThumbnailAdapter;
import de.wangchao.musicplayer.type.Music;
import de.wangchao.musicplayer.util.Tools;
import de.wangchao.musicplayer.widget.MiniPlayPannelWrapper;
import de.wangchao.musicplayer.widget.MusicsAdapter;
import de.wangchao.musicplayer.widget.TrackAdapter;
import de.wangchao.musicplayer.widget.MiniPlayPannelWrapper.OnStatusChangedListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;

public class MyFavoriteActivity extends Activity {
	private boolean mBound=false;
	 private MusicService mService;
	 private ArrayList<Music> mTrackList = new ArrayList<Music>();
	 private MiniPlayPannelWrapper miniPlayPannelWrapper;
	 private ListView lv_music;
	 private ThumbnailAdapter listThumbnailAdapter = null;
	 private MusicsAdapter musicWrapper = null;
	 private DataBase database;
	 private static final int[] LIST_IMAGE_IDS = {
	        R.id.img_album
	 };
	 private ServiceConnection mConnection = new ServiceConnection() {
	        @Override
	        public void onServiceConnected(ComponentName name, IBinder service) {
	            MusicBinder binder = (MusicBinder) service;
	            mService = binder.getService();
	            mBound = true;
	            
	            miniPlayPannelWrapper.bindService(mService);
	            miniPlayPannelWrapper.registerBroadcastReceiver(MyFavoriteActivity.this,
	                    new OnStatusChangedListener() {
	                        @Override
	                        public void onStatusChanged() {

	                            miniPlayPannelWrapper.bindService(mService);
	                        }
	                    });
	        }
	        @Override
	        public void onServiceDisconnected(ComponentName name) {
	            mBound = false;
	        }
	    };
     
	 @Override
     protected void onCreate(Bundle savedInstanceState){
    	 super.onCreate(savedInstanceState);
    	 setContentView(R.layout.music_list);
    	 
    	 View header=(View)findViewById(R.id.header);
	     header.setVisibility(View.INVISIBLE);
	     header.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 5));
	     
	     
	     database=new DataBase(this);  
	     View panel=(View)findViewById(R.id.panel);
	     miniPlayPannelWrapper=new MiniPlayPannelWrapper(panel);
	     
	     lv_music=(ListView)findViewById(R.id.listView_music);
	     lv_music.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3) {
					// TODO Auto-generated method stub
					 mService.setOnlinePlayList(mTrackList);
					Intent intent = new Intent(MyFavoriteActivity.this,MediaPlayerActivity.class);
                 intent.putExtra("position", position);
                 startActivity(intent);
				}
	        	
	        });
	     lv_music.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				delDialog(mTrackList.get(position));
				return true;
			}
	    	 
	     });
	   
	     getFavData();
     }
     
	 private void getFavData(){
		    ArrayList<Music> music=database.getFav();
		    if(music==null)
	             music=new ArrayList<Music>();
		    
    		if (listThumbnailAdapter == null) {
	            musicWrapper = new MusicsAdapter(MyFavoriteActivity.this);
	            listThumbnailAdapter = new ThumbnailAdapter(MyFavoriteActivity.this,
	                    musicWrapper, ((OnlineMusicApplication) getApplication()).getCache(),
	                    LIST_IMAGE_IDS);
	        }
	
	        lv_music.setAdapter(listThumbnailAdapter);
	        musicWrapper.set(music);
	        listThumbnailAdapter.notifyDataSetChanged();
	
	        mTrackList.clear();
	        mTrackList.addAll(music);
	     
	 }
	 
	 private void delDialog(final Music music) {
	        AlertDialog.Builder builder = new Builder(MyFavoriteActivity.this);
	        builder.setMessage("确定要取消该收藏?");
	        builder.setTitle("提示");

	        builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                dialog.dismiss();
	                boolean is=database.delFav(music);
	                getFavData();
	            }
	        });
	        builder.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {

	                dialog.dismiss();
	            }
	        });
	        
	        builder.create().show();
	    }
	 
     @Override
	 protected void onStart() {
	      super.onStart();
	      // Bind to music service
	      Intent intent = new Intent(MyFavoriteActivity.this, MusicService.class);
	      getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	 }
	    
	 @Override
	 public void onResume() {
		 getFavData();
	     super.onResume();
	    
	 }

	 @Override
	 public void onPause() {
	     super.onPause();
	 }
	 
	 @Override
	 protected void onStop() {
	     super.onStop();
	 }

	 @Override
	 protected void onDestroy() {
	     super.onDestroy();
	     if (mBound) {
	          getApplicationContext().unbindService(mConnection);
	          mBound = false;
	     }  
	     miniPlayPannelWrapper.unregister();
	     if (listThumbnailAdapter != null) {
	            listThumbnailAdapter.close();
	     }
	 }
	    
	 @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {

	      if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	            Tools.exitDialog(this);
	            return true;
	      }

	      return super.onKeyDown(keyCode, event);
	 }
}
