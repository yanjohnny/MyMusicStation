package com.J.service;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.widget.Toast;

import com.J.entity.Music;
import com.J.util.GlobalConsts;

public class PlayMusicService extends Service{
	private ArrayList<Music> musics;
	private int position;
	//���ڲ���ý���ļ���ý�岥����
	private MediaPlayer player=new MediaPlayer();
	private boolean isLoop=true;
	private MusicControlReceiver receiver;
	/**
	 * ��serviceʵ������ʱִ��һ��
	 */
	public void onCreate() {
		new Thread(){
			public void run() {
				while(isLoop){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(player.isPlaying()){
						//��ȡ��ǰ�Ľ�����������ʱ��
						//��activity���㲥
						int current=player.getCurrentPosition();
						int total=player.getDuration();
						Intent i=new Intent();
						i.setAction(GlobalConsts.ACTION_UPDATE_PROGRESS);
						i.putExtra("current", current);
						i.putExtra("total", total);
						sendBroadcast(i);
					}
				}
			}
		}.start();
		
		//ע��㲥������
		receiver=new MusicControlReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction(GlobalConsts.ACTION_NEXT_MUSIC);
		filter.addAction(GlobalConsts.ACTION_PRE_MUSIC);
		filter.addAction(GlobalConsts.ACTION_PLAY_MUSIC);
		filter.addAction(GlobalConsts.ACTION_SEEK_TO);
		this.registerReceiver(receiver, filter);
	}
	
	public void onDestroy() {
		super.onDestroy();
		//ȡ��ע��㲥������
		this.unregisterReceiver(receiver);
	}
	
	public int onStartCommand(Intent i, int flags, int startId) {
		musics=(ArrayList<Music>)i.getSerializableExtra("list");
		position=i.getIntExtra("position", 0);
		playMusic();
		return START_NOT_STICKY;
	}
	
	public void playMusic(){
		//׼����������
		Music m=musics.get(position);
		try {
			player.reset();
			player.setDataSource(GlobalConsts.BASEURL+m.getMusicpath());
			player.prepare();
			player.start();
			//���ڲ��Ÿ���  �ѵ�ǰ��������Ϣ���͸�activity
			Intent i=new Intent();
			i.setAction(GlobalConsts.ACTION_UPDATE_MUSIC_INFO);
			i.putExtra("music", m);
			this.sendBroadcast(i);
			//player.prepareAsync();
			// player.setOnPreparedListener(new OnPreparedListener() {
			//	public void onPrepared(MediaPlayer mp) {
			//		//׼�����  ��ʼ�ɻ�
			//		player.start();
			//	}
			//});
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, "����Դ����", Toast.LENGTH_SHORT).show();
			next();
		}
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void play(){
		if(player.isPlaying()){
			player.pause();
		}else{
			player.start();
		}
	}
	//������һ�׸�
	public void next(){
		//����������һ��
		if(position < musics.size()-1){
			position++;
		}
		playMusic();
	}
	
	//������һ�׸�
	public void pre(){
		//������ǵ�һ��
		if(position > 0){
			position--;
		}
		playMusic();
	}
	
	/**
	 * �л�����Ӧ��λ�ü�������
	 * @param progress
	 */
	public void seekTo(int progress){
		player.seekTo(progress);
	}
	

	class MusicControlReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if(action.equals(GlobalConsts.ACTION_NEXT_MUSIC)){
				next();
			}else if(action.equals(GlobalConsts.ACTION_PLAY_MUSIC)){
				play();
			}else if(action.equals(GlobalConsts.ACTION_PRE_MUSIC)){
				pre();
			}else if(action.equals(GlobalConsts.ACTION_SEEK_TO)){
				int progress=intent.getIntExtra("progress", 0);
				seekTo(progress);
			}
		}
	}
	
}
