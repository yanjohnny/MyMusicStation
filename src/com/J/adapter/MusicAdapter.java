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
	//声明任务集合
	private List<ImageLoadTask> tasks=new ArrayList<ImageLoadTask>();
	//声明工作线程
	private Thread workThread;
	//
	private boolean isLoop=true;
	private ListView listView;
	//声明缓存图片所需要的 hashMap
	private HashMap<String, SoftReference<Bitmap>> cache=
			new HashMap<String, SoftReference<Bitmap>>();
	
	//声明Handler
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HANDLER_LOAD_IMAGE_SUCCESS:
				//设置ImageView
				ImageLoadTask task=(ImageLoadTask)msg.obj;
				//通过tag获取到当前的ImageView
				ImageView iv=(ImageView)listView.findViewWithTag(task.position);
				//设置ImageView的Bitmap
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
		//实例化工作线程  不断轮询list
		workThread=new Thread(){
			public void run() {
				//不断轮询TaskList
				while(isLoop){
					if(!tasks.isEmpty()){
						//获取集合中的第一个元素
						ImageLoadTask task=tasks.remove(0);
						//获取path  发送请求 得到Bitmap
						String path=task.path;
						try {
							Bitmap bitmap=loadBitmap(path);
							task.bitmap=bitmap;
							//把bitmap设置到ImageView  
							//发消息给handler
							Message msg=new Message();
							msg.what=HANDLER_LOAD_IMAGE_SUCCESS;
							msg.obj=task;
							handler.sendMessage(msg);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else{
						//队列里没有任务
						//等待
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
		//在此处读取文件缓存
		String cachePath=
				new File(context.getCacheDir(), path).getAbsolutePath();
		Bitmap map=BitmapUtils.loadBitmap(cachePath);
		if(map!=null){
			Log.i("info", "该图片从文件中读取...");
			//再次存入内存缓存
			cache.put(path, new SoftReference<Bitmap>(map));
			return map;
		}
		
		//没有的话  重新下载
		HttpEntity entity=HttpUtils.get(HttpUtils.GET, GlobalConsts.BASEURL+path, null);
		byte[] bytes=EntityUtils.toByteArray(entity);
		Bitmap bitmap=BitmapUtils.loadBitmap(bytes, 50, 50);
		// 把下载下来的图片数据存起来
		cache.put(path, new SoftReference<Bitmap>(bitmap));
		// 存入缓存目录   调用save方法
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
		//给holder中的控件进行赋值 
		Music m=musics.get(position);
		holder.tvName.setText(m.getName());
		holder.tvSinger.setText(m.getSinger());
		holder.tvAuthor.setText(m.getAuthor());
		holder.tvDuration.setText(m.getDurationtime());
		//给当前item的ImageView设置Tag 
		holder.ivAlbum.setTag(position);
		
		//用于图片下载完成后  找到当前的imageView
		//在吧图片任务存入集合之前 先去缓存中查找
		//先从缓存中查一遍 看看是否已经下载过了
		//如果已经下载过了，就直接使用
		SoftReference<Bitmap> ref=cache.get(m.getAlbumpic());
		if(ref!=null && ref.get()!=null){
			Bitmap bitmap=ref.get();
			holder.ivAlbum.setImageBitmap(bitmap);
			Log.i("info", "该图片从内存缓存中读取...");
			return convertView;
		}
		//如果内存缓存中没有图片 在此处直接读取
		//缓存目录直接加载图片 将会稍微阻塞主线程
		//所以读取文件缓存的工作放在loadBitmap
		//方法中
		
		//把图片任务添加到任务集合
		ImageLoadTask task=new ImageLoadTask();
		task.path=m.getAlbumpic();
		task.position=position;
		tasks.add(task);
		//唤醒工作线程  起来干活 
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



