package de.wangchao.musicplayer.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.wangchao.musicplayer.R;
import de.wangchao.musicplayer.db.DataBase;
import de.wangchao.musicplayer.service.MusicService;
import de.wangchao.musicplayer.service.MusicService.MusicBinder;
import de.wangchao.musicplayer.type.Music;
import de.wangchao.musicplayer.util.Tools;
import de.wangchao.musicplayer.widget.MiniPlayPannelWrapper;
import de.wangchao.musicplayer.widget.MusicListAdapter;
import de.wangchao.musicplayer.widget.MiniPlayPannelWrapper.OnStatusChangedListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LocalMusicListActivity extends Activity{
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
	 private DataBase db;
	 private View footer;
	 
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
    	 
    	 db=new DataBase(this);
    	 lv_music=(ListView)findViewById(R.id.listView_music);
    	 View panel=(View)findViewById(R.id.panel);
	     miniPlayPannelWrapper=new MiniPlayPannelWrapper(panel);
	     tv_count=(TextView)findViewById(R.id.tv_count);
	     tv_content=(TextView)findViewById(R.id.tv_content);
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
				Tools.ShowPlayListDialog(LocalMusicListActivity.this,mService.getOnlinePlayList());
			    
			}
	    	 
	     });
	     
	     adapter=new MusicListAdapter(LocalMusicListActivity.this);
	     
    	 lv_music.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				    // TODO Auto-generated method stub
				    String tag=adapter.getTag();
				    Intent intent=new Intent(LocalMusicListActivity.this,SongsListActivity.class);
				    intent.putExtra("id", tag);
				    intent.putExtra("pos", position);
				    startActivity(intent);
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
    	    adapter.setList(new ArrayList<Map<String,Object>>(),"");
			String id = getIntent().getStringExtra("id");
			if(id.equals(LocalMusicActivity.SINGER_MUSIC)){
				adapter.setList(LocalMusicActivity.singerMusicMap,id);
			}
			if(id.equals(LocalMusicActivity.ALBUM_MUSIC)){
				adapter.setList(LocalMusicActivity.albumMusicMap,id);
			}
			if(id.equals(LocalMusicActivity.FILE_MUSIC)){
				adapter.setList(LocalMusicActivity.fileMusicMap,id);
			}
			if(id.equals(LocalMusicActivity.PLAYLIST_MUSIC)){
				adapter.setList(LocalMusicActivity.playListMusicMap, id);
				lv_music.setOnItemLongClickListener(new OnItemLongClickListener(){
					@Override
					public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						// TODO Auto-generated method stub
						delDialog(adapter.getList().get(position).get("txt1").toString());
						return true;
					}
				});
				footer = getLayoutInflater().inflate(R.layout.list_footer, null);
			    Button btnFooter = (Button) footer.findViewById(R.id.btn_footer);
				lv_music.addFooterView(footer);
				btnFooter.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						createNewPlayListEvent();
					}		
				});
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
     
     private void createNewPlayListEvent(){
    	 String name[]={null};
			int index=1;
			for(int i=0;i<LocalMusicActivity.playListMusicMap.size();i++){
				if(LocalMusicActivity.playListMusicMap.get(i).get("txt1").equals("播放列表"+index))
				{
					index++;
					i=0;
					continue;
				}	
			}
			newPlayListDialog(index,name);
     }
     
     private void newPlayListDialog(int index,final String name[]){
     	AlertDialog.Builder builder=new Builder(LocalMusicListActivity.this);
     	builder.setTitle("播放列表名称"); 
     	final EditText tv=new EditText(LocalMusicListActivity.this);
     	tv.setText("播放列表"+index);
     	builder.setView(tv);
     	builder.setPositiveButton("确定", new android.content.DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// TODO Auto-generated method stub
 				dialog.dismiss();
 				name[0]=tv.getText().toString();
 				if(name[0]!=null){
 				   db.creatNewPlayList(name[0]);
 				   LocalMusicActivity.refreshPlayList();
 				   adapter.setList(LocalMusicActivity.playListMusicMap, LocalMusicActivity.FILE_MUSIC);
 				}
 			}
 		});
     	builder.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// TODO Auto-generated method stub
 				dialog.dismiss();
 				name[0]=null;
 			}
 		});
     	builder.create().show();
     }
     
     private void setHeader(){
    	tv_count.setText(String.valueOf(adapter.getCount()));
    	String tag=adapter.getTag();
    	if(tag.equals(LocalMusicActivity.SINGER_MUSIC))
    		tv_content.setText("位歌手");
    	if(tag.equals(LocalMusicActivity.ALBUM_MUSIC))
    		tv_content.setText("张专辑");
    	if(tag.equals(LocalMusicActivity.FILE_MUSIC))
    		tv_content.setText("个文件夹");
    	if(tag.equals(LocalMusicActivity.PLAYLIST_MUSIC))
    		tv_content.setText("个播放列表");
     }
     
     private void delDialog(final String listname) {
	        AlertDialog.Builder builder = new Builder(LocalMusicListActivity.this);
	        builder.setMessage("确定要删除该列表?");
	        builder.setTitle("提示");

	        builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                dialog.dismiss();
	                if(db.delMyPlayList(listname)){
	                   LocalMusicActivity.refreshPlayList();
	 				   adapter.setList(LocalMusicActivity.playListMusicMap, LocalMusicActivity.FILE_MUSIC);
	                }
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
}
