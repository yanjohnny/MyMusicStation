package com.J.biz;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.J.musics.MainActivity;
import com.J.entity.Music;
import com.J.util.GlobalConsts;
import com.J.util.HttpUtils;

import android.os.AsyncTask;
import android.util.Log;

/** 音乐相关的业务类 **/
public class MusicBiz extends  AsyncTask<String, String, List<Music>> {
	private MainActivity context;
	public MusicBiz(MainActivity context) {
		this.context=context;
	}
	
	/**
	 * 在工作线程中执行  发送http请求   
	 */
	protected List<Music> doInBackground(String... params) {
		String uri=GlobalConsts.BASEURL+"loadMusics.jsp";
		try {
			HttpEntity entity=HttpUtils.get(HttpUtils.GET, uri, null);
			String json=EntityUtils.toString(entity);
			Log.i("info", "entity:"+json);
			//解析json  获取List<Music>
			//json :  {result:ok, data:[{},{},{}]}
			JSONObject obj=new JSONObject(json);
			String result=obj.getString("result");
			if("ok".equals(result)){
				JSONArray ary=obj.getJSONArray("data");
				List<Music> musics=parseJSON(ary);
				return musics;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * doInBackground方法执行完毕后
	 * 在主线程中执行该方法  并且传递参数
	 */
	@Override
	protected void onPostExecute(List<Music> musics) {
		//更新UI  设置Adapter
		context.updateListView(musics);
	}




	/**
	 * id;
		album;
		albumpic;
		author;
		composer;
		downcount;
		durationtime;
		favcount;
		musicpath;
		name;
		singer;
	 * @param ary
	 * @return
	 */
	private List<Music> parseJSON(JSONArray ary)throws JSONException {
		List<Music> musics=new ArrayList<Music>();
		for(int i=0; i<ary.length(); i++){
			JSONObject obj=ary.getJSONObject(i);
			Music m=new Music();
			m.setId(obj.getInt("id"));
			m.setAlbum(obj.getString("album"));
			m.setAlbumpic(obj.getString("albumpic"));
			m.setAuthor(obj.getString("author"));
			m.setComposer(obj.getString("composer"));
			m.setDowncount(obj.getString("downcount"));
			m.setDurationtime(obj.getString("durationtime"));
			m.setFavcount(obj.getString("favcount"));
			m.setMusicpath(obj.getString("musicpath"));
			m.setName(obj.getString("name"));
			m.setSinger(obj.getString("singer"));
			musics.add(m);
		}
		return musics;
	}
	
}




