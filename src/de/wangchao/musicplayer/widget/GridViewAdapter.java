package de.wangchao.musicplayer.widget;

import de.wangchao.musicplayer.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridViewAdapter extends BaseAdapter {
    Context context;
    private int[] img_array={
			R.drawable.list_topbar_online,R.drawable.list_topbar_favorite,R.drawable.list_topbar_ring,
			R.drawable.list_topbar_online,R.drawable.list_topbar_favorite,R.drawable.list_topbar_ring};
	private String[] txt_array={"歌曲","歌手","专辑","文件夹","播放列表","扫描音乐"};
	private int width;
	public GridViewAdapter(Context context,int width){
		this.context=context;
		this.width=width;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return img_array.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup arg2) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if(view==null)  
		{  
		   holder=new ViewHolder();
		   view=LayoutInflater.from(context).inflate(R.layout.main_tab_item, null);
		   view.setBackgroundResource(R.drawable.mini_player_panel_bg);
		   view.setLayoutParams(new AbsListView.LayoutParams(width,width));
		   holder.img=(ImageView)view.findViewById(R.id.img_tab_item);
		   holder.txt=(TextView)view.findViewById(R.id.tv_tab_item);
		   //view.setPadding(8,8,8,8);  
		   view.setTag(holder);
		}  
		else  
		{  
		   holder=(ViewHolder)view.getTag();  
		}  
		holder.img.setImageResource(img_array[position]);
		holder.txt.setText(txt_array[position]);
		
		return view;
	}

	class ViewHolder{
		ImageView img;
		TextView txt;
	}
	
}
