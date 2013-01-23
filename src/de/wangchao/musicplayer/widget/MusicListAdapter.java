package de.wangchao.musicplayer.widget;

import java.util.ArrayList;
import java.util.Map;

import de.wangchao.musicplayer.activity.LocalMusicActivity;
import de.wangchao.musicplayer.type.Music;
import de.wangchao.musicplayer.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MusicListAdapter extends BaseAdapter{
    private Context context;
	private LayoutInflater inflater;
	private ArrayList<Map<String,Object>> list;
	private String tag;

	public MusicListAdapter(Context context){
        tag="";
		list=new ArrayList<Map<String,Object>>();
		this.context=context;
		this.inflater = LayoutInflater.from(context);
    }
	
	public String getTag(){
		return tag;
	}
	
	public void setList(ArrayList<Map<String,Object>> _list, String _tag){
		list=_list;
		this.tag=_tag;
		notifyDataSetChanged();
	}
	
	public ArrayList<Map<String,Object>> getList() {
        return list;
    }
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if(convertView==null){
			holder=new ViewHolder();
			convertView=inflater.inflate(R.layout.online_list_item, null);
			
			holder.img_album=(ImageView)convertView.findViewById(R.id.img_album);
			holder.song=(TextView)convertView.findViewById(R.id.Song);
			holder.singer=(TextView)convertView.findViewById(R.id.Singer);
			holder.img_select=(ImageView)convertView.findViewById(R.id.img_select);
			
			convertView.setTag(holder);
		}
		else{
			holder=(ViewHolder)convertView.getTag();
		}
		
		holder.song.setText(list.get(position).get("txt1").toString());
		holder.singer.setText(list.get(position).get("txt2").toString());
		
		if(tag.equals(LocalMusicActivity.ALBUM_MUSIC)||tag.equals(LocalMusicActivity.ALL_MUSIC)){
		  if(list.get(position).get("art")!=null){
			 String album_art= list.get(position).get("art").toString();
		     Bitmap bm = BitmapFactory.decodeFile(album_art);
             BitmapDrawable bmpDraw = new BitmapDrawable(bm);
             holder.img_album.setImageDrawable(bmpDraw);
		  }
		  else{
	   	     holder.img_album.setImageResource(R.drawable.default_mini_album);
		  }
		  
		}
		else if(tag.equals(LocalMusicActivity.SINGER_MUSIC))
			holder.img_album.setImageResource(R.drawable.large_person);
		else if(tag.equals(LocalMusicActivity.FILE_MUSIC))
			holder.img_album.setImageResource(R.drawable.large_file);
		else if(tag.equals(LocalMusicActivity.PLAYLIST_MUSIC))
			holder.img_album.setImageResource(R.drawable.large_list);
		return convertView;
	}
	
	class ViewHolder{
    	ImageView img_album;
    	TextView  song;
    	TextView  singer;
    	ImageView img_select;
    }
}
