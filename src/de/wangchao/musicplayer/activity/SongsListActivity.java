package de.wangchao.musicplayer.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.wangchao.musicplayer.R;
import de.wangchao.musicplayer.service.MusicService;
import de.wangchao.musicplayer.service.MusicService.MusicBinder;
import de.wangchao.musicplayer.type.Music;
import de.wangchao.musicplayer.util.Tools;
import de.wangchao.musicplayer.widget.MiniPlayPannelWrapper;
import de.wangchao.musicplayer.widget.MusicListAdapter;
import de.wangchao.musicplayer.widget.MiniPlayPannelWrapper.OnStatusChangedListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SongsListActivity extends Activity {
	 private boolean mBound=false;
	 private ListView lv_music;
	 private MusicService mService;
     private ArrayList<Music> mTrackList = new ArrayList<Music>();
	 private MiniPlayPannelWrapper miniPlayPannelWrapper;
	 private MusicListAdapter adapter;
	 private TextView tv_count;
	 private TextView tv_content;
	 private ImageView img_back;
	 private ImageView btn_playlist;
	 private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MusicBinder binder = (MusicBinder) service;
            mService = binder.getService();
            mBound = true;

            miniPlayPannelWrapper.bindService(mService);
            miniPlayPannelWrapper.registerBroadcastReceiver(SongsListActivity.this,
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
    	 this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	 setContentView(R.layout.music_list);
    	 
    	 adapter=new MusicListAdapter(SongsListActivity.this);
    	 
    	 View panel=(View)findViewById(R.id.panel);
	     miniPlayPannelWrapper=new MiniPlayPannelWrapper(panel);
	     
	     tv_count=(TextView)findViewById(R.id.tv_count);
	     tv_content=(TextView)findViewById(R.id.tv_content);
	     tv_content.setText("首歌曲");
	     
	     img_back=(ImageView)findViewById(R.id.iv_header_back);
	     img_back.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
	    	 
	     });
	     
	     btn_playlist=(ImageView)findViewById(R.id.btn_list);
	     btn_playlist.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Tools.ShowPlayListDialog(SongsListActivity.this,mService.getOnlinePlayList());
				}
		     });
	     
	     lv_music=(ListView)findViewById(R.id.listView_music);
	     lv_music.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				if(adapter.getTag().equals(LocalMusicActivity.ALL_MUSIC)){
					   mTrackList.clear();
					   ArrayList<Map<String,Object>>list=adapter.getList();
					   for(int i=0;i<list.size();i++){
						   mTrackList.add((Music)list.get(i).get("music"));
					   }
					   mService.setOnlinePlayList(mTrackList);
					   Intent intent = new Intent(SongsListActivity.this,MediaPlayerActivity.class);
	                   intent.putExtra("position", position);
	                   startActivity(intent);
				}
			}
	    	 
	   });
	   
	   initData();
	    
     }
     @Override
   	 protected void onStart() {
   	        super.onStart();
   	        // Bind to music service
   	        Intent intent = new Intent(SongsListActivity.this, MusicService.class);
   	        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
   	        
   	 }
     @Override
     protected void onDestroy() {

         super.onDestroy();
         if (mBound) {
             getApplicationContext().unbindService(mConnection);
             mBound = false;
         }
         miniPlayPannelWrapper.unregister();
     }
     
     
     private void initData(){
    	 String id=getIntent().getStringExtra("id");
    	 int pos=getIntent().getIntExtra("pos", -1);
    	 if(id.equals(LocalMusicActivity.ALL_MUSIC))
    		 adapter.setList(LocalMusicActivity.allMusicMap,LocalMusicActivity.ALL_MUSIC);
    	 if(pos>-1){
    		ArrayList<Map<String,Object>> newlist=new ArrayList<Map<String,Object>>();
    		ArrayList<Music> musiclist=new ArrayList<Music>();
    		if(id.equals(LocalMusicActivity.SINGER_MUSIC))
    			 musiclist=(ArrayList<Music>)LocalMusicActivity.singerMusicMap.get(pos).get("list");
    		if(id.equals(LocalMusicActivity.ALBUM_MUSIC))
   			     musiclist=(ArrayList<Music>)LocalMusicActivity.albumMusicMap.get(pos).get("list");
    		if(id.equals(LocalMusicActivity.FILE_MUSIC))
   			     musiclist=(ArrayList<Music>)LocalMusicActivity.fileMusicMap.get(pos).get("list");
    		if(id.equals(LocalMusicActivity.PLAYLIST_MUSIC))
       			 musiclist=(ArrayList<Music>)LocalMusicActivity.playListMusicMap.get(pos).get("list");
			for(int i=0;i<musiclist.size();i++){
			     Map<String,Object> map=new HashMap<String,Object>();
				 map.put("txt1", musiclist.get(i).getSongName());
				 map.put("txt2", musiclist.get(i).getSingerName());
				 map.put("music", musiclist.get(i));
				 newlist.add(map);
			 }
			 adapter.setList(newlist, LocalMusicActivity.ALL_MUSIC);
    	 }
    	lv_music.setAdapter(adapter);
		tv_count.setText(""+adapter.getCount());
     }
     
}
