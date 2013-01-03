package de.wangchao.musicplayer.activity;

import de.wangchao.musicplayer.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

public class MainTabActivity extends TabActivity {
	TabHost mTabHost;
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         mTabHost= getTabHost();
         addTab("本地音乐", R.drawable.list_topbar_ring, LocalMusicActivity.class);
         addTab("在线音乐", R.drawable.list_topbar_online, OnlineMusicActivity.class);
         addTab("我的收藏", R.drawable.list_topbar_favorite, MyFavoriteActivity.class);
	 }
	 
	 private void addTab(String label, int drawableId, Class<?> cls) {

	        Intent intent = new Intent(this, cls);
	        TabHost.TabSpec spec = mTabHost.newTabSpec(label);

	        View tabIndicator = LayoutInflater.from(this).inflate(R.layout.main_tab_item,
	                getTabWidget(), false);
	        TextView title = (TextView) tabIndicator.findViewById(R.id.tv_tab_item);
	        title.setText(label);
	        ImageView icon = (ImageView) tabIndicator.findViewById(R.id.img_tab_item);
	        icon.setImageResource(drawableId);

	        spec.setIndicator(tabIndicator);
	        spec.setContent(intent);
	        mTabHost.addTab(spec);
	    }
}
