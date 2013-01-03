package de.wangchao.musicplayer.activity;

import java.io.IOException;
import java.util.ArrayList;

import de.wangchao.musicplayer.util.Tools;
import de.wangchao.musicplayer.activity.MediaPlayerActivity;
import de.wangchao.musicplayer.OnlineMusicApi;
import de.wangchao.musicplayer.OnlineMusicApplication;
import de.wangchao.musicplayer.service.MusicService;
import de.wangchao.musicplayer.service.MusicService.MusicBinder;
import de.wangchao.musicplayer.type.Track;
import de.wangchao.musicplayer.widget.MiniPlayPannelWrapper;
import de.wangchao.musicplayer.R;
import de.wangchao.musicplayer.thumbnail.ThumbnailAdapter;
import de.wangchao.musicplayer.type.Music;
import de.wangchao.musicplayer.widget.MiniPlayPannelWrapper.OnStatusChangedListener;
import de.wangchao.musicplayer.widget.MusicListAdapter;
import de.wangchao.musicplayer.widget.MusicsAdapter;
import de.wangchao.musicplayer.widget.TrackAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OnlineMusicActivity extends Activity{
	 private boolean mBound=false;
	 private MusicService mService;
	 private ArrayList<Track> mTrackList = new ArrayList<Track>();
	 
	 private MiniPlayPannelWrapper miniPlayPannelWrapper;
	 private ListView lv_music;
	 private ProgressDialog loadingDialog;
	 
	 private OnlineMusicApi onlineMusicApi;
	 private ThumbnailAdapter listThumbnailAdapter = null;
	 private MusicsAdapter musicWrapper = null;
	 private TrackAdapter trackAdapter=null;
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
	    private GetNetWorkMusicsTask getMusicsTask;
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	    	super.onCreate(savedInstanceState);
	    	 
	    	setContentView(R.layout.music_list);
	    	
	    	View header=(View)findViewById(R.id.header);
	    	header.setVisibility(View.GONE);
	    	
	    	lv_music=(ListView)findViewById(R.id.listView_music);
	    	View panel=(View)findViewById(R.id.panel);
	    	miniPlayPannelWrapper=new MiniPlayPannelWrapper(panel);
	    	loadingDialog = new ProgressDialog(OnlineMusicActivity.this);
	        loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        loadingDialog.setTitle("正在加载歌曲...");
	        onlineMusicApi = ((OnlineMusicApplication)this.getApplication()).getOnlineMusicApi();
	      
	        
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
	        
	        getMusicsTask=new GetNetWorkMusicsTask();
	        getMusicsTask.execute();
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
	    private abstract class GetTask<Param, Progress, Result> extends AsyncTask<Param, Progress, Result>{
		    
	    	private Exception exception = null;
	    	
		    @Override
		    protected void onPreExecute() {
		
		        super.onPreExecute();
		        
		            loadingDialog.show();
		    }
		
		    @Override
		    protected Result doInBackground(Param... params) {
		
		        Result result = null;
		        try {
		            result = getData(params);
		        } catch (IOException e) {
		            exception = e;
		        } catch (Exception e) {
		            exception = e;
		        }
		        return result;
		    }
		
		    @Override
		    protected void onPostExecute(Result result) {
		
		        super.onPostExecute(result);
		        if (loadingDialog.isShowing()) {
		            loadingDialog.dismiss();
		        }
		
		        if (exception != null) {
		            Toast.makeText(getApplicationContext(), "加载失败"+exception.getMessage(), Toast.LENGTH_LONG).show();
		        } else {
		            onDataRecieved(result);
		        }
	        }

		    protected abstract Result getData(Param... params) throws Exception;
            protected abstract void onDataRecieved(Result result);
     }
	   
	    private abstract class GetMusicTask<Param, Progress, Result extends ArrayList<Music>> extends GetTask<Param, Progress, Result> {

		    @Override
		    protected void onPreExecute() {
		
		        super.onPreExecute();
		    }
		
		    @Override
		    protected void onDataRecieved(Result result) {
		
		        if (result == null) {
		            mTrackList.clear();
		            lv_music.setAdapter(null);
		            return;
		        }
		
		        if (listThumbnailAdapter == null) {
		            musicWrapper = new MusicsAdapter(OnlineMusicActivity.this);
		            listThumbnailAdapter = new ThumbnailAdapter(OnlineMusicActivity.this,
		                    musicWrapper, ((OnlineMusicApplication) getApplication()).getCache(),
		                    LIST_IMAGE_IDS);
		        }
		
		        lv_music.setAdapter(listThumbnailAdapter);
		        musicWrapper.add(result);
		        listThumbnailAdapter.notifyDataSetChanged();
		
		        mTrackList.clear();
		        for (int i = 0; i < result.size(); i++) {
		            mTrackList.add(Track.parseMusic(result.get(i)));
		            //Log.i("1","song:"+mTrackList.get(i).getTrackName());
		        }
		       
		    }
		}


	    private class GetNetWorkMusicsTask extends GetMusicTask<Integer, Void, ArrayList<Music>> {

	      
	        @Override
	        protected ArrayList<Music> getData(Integer... params) throws Exception {

	           return onlineMusicApi.getMusics();
	        	//return null;

	        }

	    }
	    
	    
}
