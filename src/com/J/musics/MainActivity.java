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
		//�����첽����  �첽���������ȡ�����б�
		new MusicBiz(this).execute();
		//��listView���ü��� 
		setListeners();
	}

	private void setListeners() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//�������Ž���Activitiy
				//����  list   position
				Intent i=new Intent(MainActivity.this, PlayMusicActivity.class);
				i.putExtra("list", (ArrayList<Music>)musics);
				i.putExtra("position", position);
				startActivity(i);
			}
		});
		
		
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				//����
				AlertDialog.Builder builder=new Builder(MainActivity.this);
				builder.setItems(new String[]{"����","ϲ��","ɾ��"}, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							//����Service ִ�����ֵ�����
							//��ȡ��ǰ������
							Music m=musics.get(position);
							String path=m.getMusicpath();
							Intent intent=new Intent(MainActivity.this, DownloadMusicService.class);
							//���ݲ���  path:  musics/gtdg.mp3
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
	 * ����ListView�е�����
	 * @param musics
	 */
	public void updateListView(List<Music> musics) {
		this.musics=musics;
		//�Զ���Adapter 
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
		//��adapter���߳�ͣ��
		adapter.stopThread();
	}
	
}
