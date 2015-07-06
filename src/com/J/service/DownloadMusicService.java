package com.J.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.apache.http.HttpEntity;

import com.J.musics.R;
import com.J.util.GlobalConsts;
import com.J.util.HttpUtils;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Notification.Builder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class DownloadMusicService extends IntentService{

	public DownloadMusicService() {
		super("download");
	}
	
	public DownloadMusicService(String name) {
		super(name);
	}

	/***
	 * 下载音乐   提示用户下载进度
	 */
	protected void onHandleIntent(Intent intent) {
		//获取下载路径
		String path=intent.getStringExtra("path");
		String httpPath=GlobalConsts.BASEURL+path;
		//sd:  Music/musics/gongtongduguo.mp3
		File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), path);
		if(!file.getParentFile().exists()){
			file.getParentFile().mkdirs();
		}
		if(file.exists()){
			Log.i("info", "文件已存在");
			return;
		}
		//发送http请求执行下载操作
		try {
			HttpEntity entity=HttpUtils.get(HttpUtils.GET, httpPath, null);
			//获取实体的字节数    long
			long total=entity.getContentLength();
			InputStream is=entity.getContent();
			//边读边写 
			byte[] buffer=new byte[1024*100];
			//获取文件输出流
			FileOutputStream fos=
					new FileOutputStream(file);
			int length=0;
			int current=0;
			//提示开始下载
			clear();
			send("音乐开始下载...", "音乐下载", "音乐开始下载");
			while( (length=is.read(buffer)) !=-1){
				fos.write(buffer, 0, length);
				//提示进度
				current+=length;
				String progress=Math.floor(current*1.0/total * 100)+"%";
				send("音乐正在下载...", "音乐下载", "音乐正在下载,当前下载进度："+progress);
			}
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//下载完成  发送通知提示
		//先清除通知 再发送
		clear();
		send("音乐下载完成", "音乐下载", "音乐下载完成");
	}
	
	public void clear(){
		NotificationManager manager=(NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(11);
	}
	
	@SuppressLint("NewApi")
	public void send(String ticker, String title, String text){
		//1.创建 NotificationManager 
		NotificationManager manager=(NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);
		//2.构建Notification对象
		Notification.Builder builder=new Builder(this);
		builder.setContentInfo("")
			.setContentText(text)
			.setContentTitle(title)
			.setLargeIcon(BitmapFactory.decodeResource(
					getResources(), R.drawable.ic_launcher))
			.setShowWhen(true)
			.setWhen(System.currentTimeMillis())
			.setSmallIcon(R.drawable.ic_launcher)
			.setSubText("")
			.setTicker(ticker);
		Notification n=builder.build();
		//调用manager.notify方法发送通知
		manager.notify(11, n);
	}
}
