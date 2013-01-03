package de.wangchao.musicplayer.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.wangchao.musicplayer.R;
import de.wangchao.musicplayer.service.MusicService;
import de.wangchao.musicplayer.service.MusicService.MusicBinder;
import de.wangchao.musicplayer.type.Music;
import de.wangchao.musicplayer.type.Track;
import de.wangchao.musicplayer.widget.MiniPlayPannelWrapper;
import de.wangchao.musicplayer.widget.MusicListAdapter;
import de.wangchao.musicplayer.widget.MiniPlayPannelWrapper.OnStatusChangedListener;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LocalMusicListActivity extends Activity{
	 private boolean mBound=false;
	 private ListView lv_music;
	 private MusicService mService;
     private ArrayList<Track> mTrackList = new ArrayList<Track>();
	 private MiniPlayPannelWrapper miniPlayPannelWrapper;
	 private MusicListAdapter adapter;
	 private boolean inSinger=false;
	 private boolean inAlbum=false;
	 private boolean inPlayList=false;
	 private TextView tv_count;
	 private TextView tv_content;
	 private ImageView img_back;
	 private Button btn_playlist;
	 
	 private ServiceConnection mConnection = new ServiceConnection() {

	        @Override
	        public void onServiceConnected(ComponentName name, IBinder service) {

	            MusicBinder binder = (MusicBinder) service;
	            mService = binder.getService();
	            mBound = true;

	            miniPlayPannelWrapper.bindService(mService);
	            miniPlayPannelWrapper.registerBroadcastReceiver(LocalMusicListActivity.this,
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
    	 
    	 lv_music=(ListView)findViewById(R.id.listView_music);
    	 View panel=(View)findViewById(R.id.panel);
	     miniPlayPannelWrapper=new MiniPlayPannelWrapper(panel);
	     tv_count=(TextView)findViewById(R.id.tv_count);
	     tv_content=(TextView)findViewById(R.id.tv_content);
	     img_back=(ImageView)findViewById(R.id.iv_header_back);
	     btn_playlist=(Button)findViewById(R.id.btn_list);
	     
	     img_back.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
	    	 
	     });
	     
	     btn_playlist.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ArrayList<Track> playlist=mService.getOnlinePlayList();
				ArrayList<Map<String,Object>> maplist=new ArrayList<Map<String,Object>>();
				if(playlist==null||playlist.size()==0){
					Toast.makeText(getApplicationContext(), "没有歌曲在播放", Toast.LENGTH_SHORT).show();
					return;
				}
				for(int i=0;i<playlist.size();i++){
					Music music=Music.parseTrack(playlist.get(i));
					Map<String,Object> map=new HashMap<String,Object>();
					map.put("txt1", music.getSongName());
					map.put("txt2", music.getSingerName());
					map.put("music", music);
					maplist.add(map);
				}
				adapter.setList(maplist, "song");
				inPlayList=true;
				setHeader();
				tv_content.setText(tv_content.getText()+"正在播放");
			}
	    	 
	     });
	     
	     adapter=new MusicListAdapter(LocalMusicListActivity.this);
	    	
    	 lv_music.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				if(adapter.getTag().equals("song")){
				   mTrackList.clear();
				   ArrayList<Map<String,Object>>list=adapter.getList();
				   for(int i=0;i<list.size();i++){
					   mTrackList.add(Track.parseMusic((Music)list.get(i).get("music")));
				   }
				   mService.setOnlinePlayList(mTrackList);
				   Intent intent = new Intent(LocalMusicListActivity.this,MediaPlayerActivity.class);
                   intent.putExtra("position", position);
                   startActivity(intent);
				}
				if(adapter.getTag().equals("singer")||adapter.getTag().equals("album")){
					ArrayList<Map<String,Object>> newlist=new ArrayList<Map<String,Object>>();
					ArrayList<Music> musiclist=(ArrayList<Music>)adapter.getList().get(position).get("list");
					for(int i=0;i<musiclist.size();i++){
						Map<String,Object> map=new HashMap<String,Object>();
						map.put("txt1", musiclist.get(i).getSongName());
						map.put("txt2", musiclist.get(i).getSingerName());
						map.put("music", musiclist.get(i));
						newlist.add(map);
					}
					if(adapter.getTag().equals("singer"))
					    inSinger=true;
					if(adapter.getTag().equals("album"))
						inAlbum=true;
					adapter.setList(newlist, "song");
					setHeader();
				}
			}
    		 
    	 });
    	 
    	 init_list();
     }
     @Override
	 protected void onStart() {

	        super.onStart();
	        // Bind to music service
	        Intent intent = new Intent(LocalMusicListActivity.this, MusicService.class);
	        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	        
	 }
     
     private void init_list(){
    	    ArrayList<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
    	    adapter.setList(list,"");
			String id = getIntent().getStringExtra("id");
			if(id.equals("song")){
				adapter.setList(LocalMusicActivity.allMusicMap,id);
			}
			if(id.equals("singer")){
				adapter.setList(LocalMusicActivity.singerMusicMap,id);
			}
			if(id.equals("album")){
				adapter.setList(LocalMusicActivity.albumMusicMap,id);
			}
			if(id.equals("playlist")){
				btn_playlist.performClick();
			}
			lv_music.setAdapter(adapter);
			setHeader();
			
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
     
     @Override
     public void onBackPressed() {
    	 if(inSinger){
    		 inSinger=false;
    		 adapter.setList(LocalMusicActivity.singerMusicMap,"singer");
    		 setHeader();
    		 return;
    	 }
    	 if(inAlbum){
    		 inAlbum=false;
    		 adapter.setList(LocalMusicActivity.albumMusicMap,"album");
    		 setHeader();
    		 return;
    	 }
         super.onBackPressed();
     }
     
     private void setHeader(){
    	tv_count.setText(String.valueOf(adapter.getCount()));
    	String content=adapter.getTag();
    	if(content.equals("song"))
    		tv_content.setText("首歌曲");
    	if(content.equals("singer"))
    		tv_content.setText("位歌手");
    	if(content.equals("album"))
    		tv_content.setText("张专辑");
    		
     }
}
