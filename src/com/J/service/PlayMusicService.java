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
	//用于播放媒体文件的媒体播放器
	private MediaPlayer player=new MediaPlayer();
	private boolean isLoop=true;
	private MusicControlReceiver receiver;
	/**
	 * 在service实例创建时执行一次
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
						//获取当前的进度与音乐总时长
						//给activity发广播
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
		
		//注册广播接收器
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
		//取消注册广播接收器
		this.unregisterReceiver(receiver);
	}
	
	public int onStartCommand(Intent i, int flags, int startId) {
		musics=(ArrayList<Music>)i.getSerializableExtra("list");
		position=i.getIntExtra("position", 0);
		playMusic();
		return START_NOT_STICKY;
	}
	
	public void playMusic(){
		//准备播放音乐
		Music m=musics.get(position);
		try {
			player.reset();
			player.setDataSource(GlobalConsts.BASEURL+m.getMusicpath());
			player.prepare();
			player.start();
			//正在播放歌曲  把当前歌曲的信息发送给activity
			Intent i=new Intent();
			i.setAction(GlobalConsts.ACTION_UPDATE_MUSIC_INFO);
			i.putExtra("music", m);
			this.sendBroadcast(i);
			//player.prepareAsync();
			// player.setOnPreparedListener(new OnPreparedListener() {
			//	public void onPrepared(MediaPlayer mp) {
			//		//准备完成  开始干活
			//		player.start();
			//	}
			//});
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, "歌曲源错误", Toast.LENGTH_SHORT).show();
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
	//播放下一首歌
	public void next(){
		//如果不是最后一首
		if(position < musics.size()-1){
			position++;
		}
		playMusic();
	}
	
	//播放上一首歌
	public void pre(){
		//如果不是第一首
		if(position > 0){
			position--;
		}
		playMusic();
	}
	
	/**
	 * 切换到响应的位置继续播放
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
