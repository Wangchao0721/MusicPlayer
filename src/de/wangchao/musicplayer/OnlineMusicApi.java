package de.wangchao.musicplayer;

import java.lang.reflect.Type;
import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.wangchao.musicplayer.http.AbstractHttpApi;
import de.wangchao.musicplayer.http.HttpApi;
import de.wangchao.musicplayer.http.HttpApiWithBasicAuth;
import de.wangchao.musicplayer.type.Music;

public class OnlineMusicApi {
	 public static final boolean DEBUG=true;
	 private final String URL_MUSIC="http://58.53.211.68:8080/api/music.aspx";
	 private final DefaultHttpClient mHttpClient = AbstractHttpApi.createHttpClient();
	 private HttpApi mHttpApi;
	 private Gson gson;
	 
	 public OnlineMusicApi()
	 {
		 mHttpApi = new HttpApiWithBasicAuth(mHttpClient, null);
		 gson = new Gson();
	 }
	 
	 public ArrayList<Music> getMusics() throws Exception {
         HttpGet httpGet = mHttpApi.createHttpGet(URL_MUSIC, //
         new BasicNameValuePair("act", "tl"), //
         new BasicNameValuePair("tp", String.valueOf(1)), //
         new BasicNameValuePair("id", String.valueOf(12)));
         String content = mHttpApi.doHttpRequest(httpGet);
         Type type = new TypeToken<ArrayList<Music>>() {}.getType();
         return gson.fromJson(content, type);
     }
}
