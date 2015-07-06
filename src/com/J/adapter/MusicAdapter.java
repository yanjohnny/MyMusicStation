package com.J.adapter;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import com.J.musics.R;
import com.J.entity.Music;
import com.J.util.BitmapUtils;
import com.J.util.GlobalConsts;
import com.J.util.HttpUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.UserDictionary.Words;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MusicAdapter extends BaseAdapter{
	private Context context;
	private List<Music> musics;
	private LayoutInflater inflater;
	//�������񼯺�
	private List<ImageLoadTask> tasks=new ArrayList<ImageLoadTask>();
	//���������߳�
	private Thread workThread;
	//
	private boolean isLoop=true;
	private ListView listView;
	//��������ͼƬ����Ҫ�� hashMap
	private HashMap<String, SoftReference<Bitmap>> cache=
			new HashMap<String, SoftReference<Bitmap>>();
	
	//����Handler
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HANDLER_LOAD_IMAGE_SUCCESS:
				//����ImageView
				ImageLoadTask task=(ImageLoadTask)msg.obj;
				//ͨ��tag��ȡ����ǰ��ImageView
				ImageView iv=(ImageView)listView.findViewWithTag(task.position);
				//����ImageView��Bitmap
				if(iv!=null){
					if(task.bitmap!=null){
						iv.setImageBitmap(task.bitmap);
					}else{
						iv.setImageResource(R.drawable.ic_launcher);
					}
				}
				break;
			}
		}
	};
	public static final int HANDLER_LOAD_IMAGE_SUCCESS=0;
	
	public MusicAdapter(Context context, List<Music> musics, ListView listView) {
		this.context=context;
		this.musics=musics;
		this.listView=listView;
		inflater=LayoutInflater.from(context);
		//ʵ���������߳�  ������ѯlist
		workThread=new Thread(){
			public void run() {
				//������ѯTaskList
				while(isLoop){
					if(!tasks.isEmpty()){
						//��ȡ�����еĵ�һ��Ԫ��
						ImageLoadTask task=tasks.remove(0);
						//��ȡpath  �������� �õ�Bitmap
						String path=task.path;
						try {
							Bitmap bitmap=loadBitmap(path);
							task.bitmap=bitmap;
							//��bitmap���õ�ImageView  
							//����Ϣ��handler
							Message msg=new Message();
							msg.what=HANDLER_LOAD_IMAGE_SUCCESS;
							msg.obj=task;
							handler.sendMessage(msg);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else{
						//������û������
						//�ȴ�
						try {
							synchronized (workThread) {
								workThread.wait();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		workThread.start();
	}
	
	/**
	 * @param path   images/junshengjinshi.jpg
	 * @return
	 * @throws Exception 
	 */
	public Bitmap loadBitmap(String path)throws Exception{
		//�ڴ˴���ȡ�ļ�����
		String cachePath=
				new File(context.getCacheDir(), path).getAbsolutePath();
		Bitmap map=BitmapUtils.loadBitmap(cachePath);
		if(map!=null){
			Log.i("info", "��ͼƬ���ļ��ж�ȡ...");
			//�ٴδ����ڴ滺��
			cache.put(path, new SoftReference<Bitmap>(map));
			return map;
		}
		
		//û�еĻ�  ��������
		HttpEntity entity=HttpUtils.get(HttpUtils.GET, GlobalConsts.BASEURL+path, null);
		byte[] bytes=EntityUtils.toByteArray(entity);
		Bitmap bitmap=BitmapUtils.loadBitmap(bytes, 50, 50);
		// ������������ͼƬ���ݴ�����
		cache.put(path, new SoftReference<Bitmap>(bitmap));
		// ���뻺��Ŀ¼   ����save����
		// /data/data/com.tarena.musicclient/cache/images/zuihong.jpg
		File targetFile=new File(context.getCacheDir(), path);
		BitmapUtils.save(targetFile, bitmap);
		
		return bitmap; 
	}
	

	@Override
	public int getCount() {
		return musics.size();
	}

	@Override
	public Object getItem(int position) {
		return musics.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder=null;
		if(convertView==null){
			convertView=inflater.inflate(R.layout.item_lv_music, null);
			holder=new ViewHolder();
			holder.ivAlbum=(ImageView)convertView.findViewById(R.id.ivAlbum);
			holder.tvName=(TextView)convertView.findViewById(R.id.tvName);
			holder.tvSinger=(TextView)convertView.findViewById(R.id.tvSinger);
			holder.tvAuthor=(TextView)convertView.findViewById(R.id.tvAuthor);
			holder.tvDuration=(TextView)convertView.findViewById(R.id.tvDuration);
			convertView.setTag(holder);
		}
		holder=(ViewHolder)convertView.getTag();
		//��holder�еĿؼ����и�ֵ 
		Music m=musics.get(position);
		holder.tvName.setText(m.getName());
		holder.tvSinger.setText(m.getSinger());
		holder.tvAuthor.setText(m.getAuthor());
		holder.tvDuration.setText(m.getDurationtime());
		//����ǰitem��ImageView����Tag 
		holder.ivAlbum.setTag(position);
		
		//����ͼƬ������ɺ�  �ҵ���ǰ��imageView
		//�ڰ�ͼƬ������뼯��֮ǰ ��ȥ�����в���
		//�ȴӻ����в�һ�� �����Ƿ��Ѿ����ع���
		//����Ѿ����ع��ˣ���ֱ��ʹ��
		SoftReference<Bitmap> ref=cache.get(m.getAlbumpic());
		if(ref!=null && ref.get()!=null){
			Bitmap bitmap=ref.get();
			holder.ivAlbum.setImageBitmap(bitmap);
			Log.i("info", "��ͼƬ���ڴ滺���ж�ȡ...");
			return convertView;
		}
		//����ڴ滺����û��ͼƬ �ڴ˴�ֱ�Ӷ�ȡ
		//����Ŀ¼ֱ�Ӽ���ͼƬ ������΢�������߳�
		//���Զ�ȡ�ļ�����Ĺ�������loadBitmap
		//������
		
		//��ͼƬ������ӵ����񼯺�
		ImageLoadTask task=new ImageLoadTask();
		task.path=m.getAlbumpic();
		task.position=position;
		tasks.add(task);
		//���ѹ����߳�  �����ɻ� 
		synchronized (workThread) {
			workThread.notify();
		}
		return convertView;
	}
	
	class ImageLoadTask{
		String path;
		Bitmap bitmap;
		int position;
		@Override
		public String toString() {
			return "ImageLoadTask path:"+path;
		}
	}
	
	class ViewHolder{
		ImageView ivAlbum;
		TextView tvName;
		TextView tvSinger;
		TextView tvAuthor;
		TextView tvDuration;
	}

	public void stopThread() {
		isLoop=false;
		synchronized (workThread) {
			workThread.notify();
		}
	}
	
}



