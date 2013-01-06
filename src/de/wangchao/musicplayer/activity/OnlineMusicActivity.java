package de.wangchao.musicplayer.activity;

import java.io.IOException;
import java.util.ArrayList;

import de.wangchao.musicplayer.util.Tools;
import de.wangchao.musicplayer.activity.MediaPlayerActivity;
import de.wangchao.musicplayer.db.DataBase;
import de.wangchao.musicplayer.OnlineMusicApi;
import de.wangchao.musicplayer.OnlineMusicApplication;
import de.wangchao.musicplayer.service.MusicService;
import de.wangchao.musicplayer.service.MusicService.MusicBinder;
import de.wangchao.musicplayer.widget.MiniPlayPannelWrapper;
import de.wangchao.musicplayer.R;
import de.wangchao.musicplayer.thumbnail.ThumbnailAdapter;
import de.wangchao.musicplayer.type.Music;
import de.wangchao.musicplayer.widget.MiniPlayPannelWrapper.OnStatusChangedListener;
import de.wangchao.musicplayer.widget.MusicsAdapter;
import de.wangchao.musicplayer.widget.TrackAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class OnlineMusicActivity extends Activity{
	 private boolean mBound=false;
	 private MusicService mService;
	 private ArrayList<Music> mTrackList = new ArrayList<Music>();
	 
	 private MiniPlayPannelWrapper miniPlayPannelWrapper;
	 private ListView lv_music;
	 private ProgressDialog loadingDialog;
	 private GetMusicTask getMusicsTask;
	 private AddMusicToDBTask addMusicTask;
	 private static int MSG_GET_MUSIC=0;
	 private static int MSG_ADD_DATABASE=1;
	 private OnlineMusicApi onlineMusicApi;
	 private ThumbnailAdapter listThumbnailAdapter = null;
	 private MusicsAdapter musicWrapper = null;
	 private TrackAdapter trackAdapter=null;
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
	            miniPlayPannelWrapper.registerBroadcastReceiver(OnlineMusicActivity.this,
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
	 
	 private final Handler handler = new Handler() {
	        @Override
	        public void handleMessage(android.os.Message msg) {

	            if (msg.what == MSG_GET_MUSIC) {
	                if (getMusicsTask != null) {
	                	getMusicsTask.cancel(true);
	                	getMusicsTask = null;
	                }
	                getMusicsTask = new GetMusicTask();
	                getMusicsTask.execute();
	            }
	            if(msg.what==MSG_ADD_DATABASE){
	            	if(addMusicTask!=null){
	            		addMusicTask.cancel(true);
	            		addMusicTask=null;
	            	}
	            	addMusicTask=new AddMusicToDBTask();
	            	addMusicTask.execute(mTrackList);
	            }
	     }
	 };
	    
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	    	super.onCreate(savedInstanceState);
	    	 
	    	setContentView(R.layout.music_list);
	    	
	    	 View header=(View)findViewById(R.id.header);
		     header.setVisibility(View.INVISIBLE);
		     header.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 5));
	    	
	    	lv_music=(ListView)findViewById(R.id.listView_music);
	    	View panel=(View)findViewById(R.id.panel);
	    	miniPlayPannelWrapper=new MiniPlayPannelWrapper(panel);
	    	loadingDialog = new ProgressDialog(OnlineMusicActivity.this);
	    	loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    	
	        onlineMusicApi = ((OnlineMusicApplication)this.getApplication()).getOnlineMusicApi();
	        database=new DataBase(this);
	        
	        lv_music.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3) {
					// TODO Auto-generated method stub
					 mService.setOnlinePlayList(mTrackList);
					Intent intent = new Intent(OnlineMusicActivity.this,
                            MediaPlayerActivity.class);
                    intent.putExtra("position", position);
                    startActivity(intent);
				}
	        	
	        });
	        
	       handler.sendEmptyMessage(MSG_GET_MUSIC);
	    }
	        
	    @Override
	    protected void onStart() {

	        super.onStart();
	        // Bind to music service
	        Intent intent = new Intent(OnlineMusicActivity.this, MusicService.class);
	        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	    }
	    
	    @Override
	    public void onResume() {

	        super.onResume();
	       // MobclickAgent.onResume(this);
	    }

	    @Override
	    public void onPause() {

	        super.onPause();
	        //MobclickAgent.onPause(this);
	    }

	    @Override
	    protected void onStop() {

	        super.onStop();

	    }

	    @Override
	    protected void onDestroy() {

	        super.onDestroy();
	        if (loadingDialog != null && loadingDialog.isShowing()) {
	            loadingDialog.dismiss();
	        }

	        if (mBound) {
	            getApplicationContext().unbindService(mConnection);
	            mBound = false;
	        }
	       
	        miniPlayPannelWrapper.unregister();
	       /* if (listThumbnailAdapter != null) {
	            listThumbnailAdapter.close();
	        }*/
	    }
	    
	    @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {

	        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	            Tools.exitDialog(this);
	            return true;
	        }

	        return super.onKeyDown(keyCode, event);
	    }
	    
        private  class GetMusicTask extends AsyncTask<Integer, Void, ArrayList<Music>>{
		    
	    	private Exception exception = null;
	    	
		    @Override
		    protected void onPreExecute() {
		    	super.onPreExecute();
		    	
		    	ArrayList<Music> music=database.getOnlineMusic();
		    	if(music==null)
		    		music=new ArrayList<Music>();
		    	
	    		if (listThumbnailAdapter == null) {
		            musicWrapper = new MusicsAdapter(OnlineMusicActivity.this);
		            listThumbnailAdapter = new ThumbnailAdapter(OnlineMusicActivity.this,
		                    musicWrapper, ((OnlineMusicApplication) getApplication()).getCache(),
		                    LIST_IMAGE_IDS);
		        }
		
		        lv_music.setAdapter(listThumbnailAdapter);
		        musicWrapper.set(music);
		        listThumbnailAdapter.notifyDataSetChanged();
		
		        mTrackList.clear();
		        mTrackList.addAll(music);
		        
		        //loadingDialog.show(); 
		    }
		
		    @Override
		    protected ArrayList<Music> doInBackground(Integer... params) {
		
		    	ArrayList<Music> result = null;
		        try {
		            result = onlineMusicApi.getMusics();
		        } catch (IOException e) {
		            exception = e;
		        } catch (Exception e) {
		            exception = e;
		        }
		        return result;
		    }
		
		    @Override
		    protected void onPostExecute(ArrayList<Music> result) {
		
		        super.onPostExecute(result);
		        if (loadingDialog.isShowing()) {
		            loadingDialog.dismiss();
		        }
		
		        if (exception != null) {
		            Toast.makeText(getApplicationContext(), "加载失败"+exception.getMessage(), Toast.LENGTH_LONG).show();
		            return;
		        } 
		        if(result==null){
		        	Toast.makeText(getApplicationContext(), "没有数据", Toast.LENGTH_LONG).show();
		            return;
		        }
		        		        
		        if (listThumbnailAdapter == null) {
		            musicWrapper = new MusicsAdapter(OnlineMusicActivity.this);
		            listThumbnailAdapter = new ThumbnailAdapter(OnlineMusicActivity.this,
		                    musicWrapper, ((OnlineMusicApplication) getApplication()).getCache(),
		                    LIST_IMAGE_IDS);
		        }
		
		        lv_music.setAdapter(listThumbnailAdapter);
		        musicWrapper.set(result);
		        listThumbnailAdapter.notifyDataSetChanged();
		
		        mTrackList.clear();
		        mTrackList=result;
		        
		        handler.sendEmptyMessage(MSG_ADD_DATABASE);
	        }
     }
        private class AddMusicToDBTask extends AsyncTask<ArrayList<Music>,Void,Void>{

			@Override
			protected Void doInBackground(ArrayList<Music>... params) {
				// TODO Auto-generated method stub
				database.delOnlineMusic();
				database.addOnlineMusic(params[0]);
				return null;
			}
        	
        }
}
