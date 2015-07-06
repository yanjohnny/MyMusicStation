package com.J.musics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.J.entity.Music;
import com.J.service.PlayMusicService;
import com.J.util.GlobalConsts;

public class PlayMusicActivity extends Activity {
	private TextView title;
	private ImageView ivAlbum;
	private SeekBar seekBar;
	private TextView tvCurrent;
	private TextView tvTotal;
	private UpdateProgressReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_music);
		//���ÿؼ�
		setViews();
		//��ȡlist  position
		Intent i=getIntent();
		ArrayList<Music> musics=
				(ArrayList<Music>)i.getSerializableExtra("list");
		int position=i.getIntExtra("position", 0);
		//����service
		Intent i2=new Intent(this, PlayMusicService.class);
		i2.putExtra("list", musics);
		i2.putExtra("position", position);
		startService(i2);
		//ע��㲥������
		receiver=new UpdateProgressReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction(GlobalConsts.ACTION_UPDATE_PROGRESS);
		filter.addAction(GlobalConsts.ACTION_UPDATE_MUSIC_INFO);
		this.registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(receiver);
	}

	private void setViews() {
		title=(TextView)findViewById(R.id.tvName);
		ivAlbum=(ImageView)findViewById(R.id.imageView3);
		seekBar=(SeekBar)findViewById(R.id.seekBar1);
		tvCurrent=(TextView)findViewById(R.id.textView3);
		tvTotal=(TextView)findViewById(R.id.textView4);
		//��seekBar���ü���
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					Intent i=new Intent();
					i.setAction(GlobalConsts.ACTION_SEEK_TO);
					i.putExtra("progress", progress);
					sendBroadcast(i);
				}
			}
		});	
	}
	public void doClick(View view){
		Intent intent=new Intent();
		switch (view.getId()) {
		case R.id.ivPlay:
			intent.setAction(GlobalConsts.ACTION_PLAY_MUSIC);
			break;
		case R.id.ivPre:
			intent.setAction(GlobalConsts.ACTION_PRE_MUSIC);
			break;
		case R.id.ivNext:
			intent.setAction(GlobalConsts.ACTION_NEXT_MUSIC);
			break;
		}
		sendBroadcast(intent);
	}	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.play_music, menu);
		return true;
	}
	/**
	 * ���ڽ��ո��½��ȵĹ㲥
	 */
	class UpdateProgressReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if(action.equals(GlobalConsts.ACTION_UPDATE_PROGRESS)){
				int current=intent.getIntExtra("current", 0);
				int total=intent.getIntExtra("total", 0);
				//����UI
				seekBar.setMax(total);
				seekBar.setProgress(current);
				SimpleDateFormat format=new SimpleDateFormat("mm:ss");
				tvCurrent.setText(format.format(new Date(current)));
				tvTotal.setText(format.format(new Date(total)));
			}else if(action.equals(GlobalConsts.ACTION_UPDATE_MUSIC_INFO)){
				//����������Ϣ
				Music m=(Music)intent.getSerializableExtra("music");
				String name=m.getName();
				m.getAlbumpic();
				//ͨ��·����ȥ�����в���ͼƬ
				//���û����ȡ����������
				//�����ڹ����߳���ִ��
				//����ImageView��Ҫ������Ϣ��handler
				title.setText(name);
			}						
		}
	}
}
