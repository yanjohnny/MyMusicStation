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
	 * ��������   ��ʾ�û����ؽ���
	 */
	protected void onHandleIntent(Intent intent) {
		//��ȡ����·��
		String path=intent.getStringExtra("path");
		String httpPath=GlobalConsts.BASEURL+path;
		//sd:  Music/musics/gongtongduguo.mp3
		File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), path);
		if(!file.getParentFile().exists()){
			file.getParentFile().mkdirs();
		}
		if(file.exists()){
			Log.i("info", "�ļ��Ѵ���");
			return;
		}
		//����http����ִ�����ز���
		try {
			HttpEntity entity=HttpUtils.get(HttpUtils.GET, httpPath, null);
			//��ȡʵ����ֽ���    long
			long total=entity.getContentLength();
			InputStream is=entity.getContent();
			//�߶���д 
			byte[] buffer=new byte[1024*100];
			//��ȡ�ļ������
			FileOutputStream fos=
					new FileOutputStream(file);
			int length=0;
			int current=0;
			//��ʾ��ʼ����
			clear();
			send("���ֿ�ʼ����...", "��������", "���ֿ�ʼ����");
			while( (length=is.read(buffer)) !=-1){
				fos.write(buffer, 0, length);
				//��ʾ����
				current+=length;
				String progress=Math.floor(current*1.0/total * 100)+"%";
				send("������������...", "��������", "������������,��ǰ���ؽ��ȣ�"+progress);
			}
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//�������  ����֪ͨ��ʾ
		//�����֪ͨ �ٷ���
		clear();
		send("�����������", "��������", "�����������");
	}
	
	public void clear(){
		NotificationManager manager=(NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(11);
	}
	
	@SuppressLint("NewApi")
	public void send(String ticker, String title, String text){
		//1.���� NotificationManager 
		NotificationManager manager=(NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);
		//2.����Notification����
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
		//����manager.notify��������֪ͨ
		manager.notify(11, n);
	}
}
