package com.J.musics;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.J.adapter.MusicAdapter;
import com.J.biz.MusicBiz;
import com.J.entity.Music;
import com.J.service.DownloadMusicService;

public class MainActivity extends Activity {
	private ListView listView;
	private MusicAdapter adapter;
	private List<Music> musics;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView=(ListView)findViewById(R.id.listView);
		//启动异步任务  异步发送请求获取音乐列表
		new MusicBiz(this).execute();
		//给listView设置监听 
		setListeners();
	}

	private void setListeners() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//启动播放界面Activitiy
				//传递  list   position
				Intent i=new Intent(MainActivity.this, PlayMusicActivity.class);
				i.putExtra("list", (ArrayList<Music>)musics);
				i.putExtra("position", position);
				startActivity(i);
			}
		});
		
		
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				//弹窗
				AlertDialog.Builder builder=new Builder(MainActivity.this);
				builder.setItems(new String[]{"下载","喜欢","删除"}, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							//启动Service 执行音乐的下载
							//获取当前的音乐
							Music m=musics.get(position);
							String path=m.getMusicpath();
							Intent intent=new Intent(MainActivity.this, DownloadMusicService.class);
							//传递参数  path:  musics/gtdg.mp3
							intent.putExtra("path", path);
							startService(intent);
							break;
						}
					}
				});
				builder.create().show();
				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * 更新ListView中的数据
	 * @param musics
	 */
	public void updateListView(List<Music> musics) {
		this.musics=musics;
		//自定义Adapter 
		adapter=new MusicAdapter(this, musics, listView);
		listView.setAdapter(adapter);
	}
	
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//把adapter的线程停掉
		adapter.stopThread();
	}
	
}
