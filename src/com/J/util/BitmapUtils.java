package com.J.util;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

/***
 * 图片工具类
 */
public class BitmapUtils {
	
	/**
	 * 通过一个路径  加载出一个Bitmap对象
	 * @param path 目标路径 
	 * @return  bitmap对象  
	 * @throws Exception
	 */
	public static Bitmap loadBitmap(String path)throws Exception{
		File file=new File(path);
		if(!file.exists()){
			return null;
		}
		return BitmapFactory.decodeFile(path);
	}
	
	/**
	 * 保存一张图片
	 * @param targetFile  目标图片文件
	 * @param bitmap  目标图片
	 * @throws Exception
	 */
	public static void save(File targetFile, Bitmap bitmap)throws Exception{
		if(!targetFile.getParentFile().exists()){
			targetFile.getParentFile().mkdirs();
		}
		if(!targetFile.exists()){
			targetFile.createNewFile();
		}
		//保存
		FileOutputStream  fos=new FileOutputStream(targetFile);
		//压缩图片并且输出
		bitmap.compress(CompressFormat.JPEG, 100, fos);
	}
	
	
	/***
	 * 把图片按照要求进行压缩 并且返回Bitmap
	 * @param bytes 图片数据源字节数组
	 * @param width 目标宽度
	 * @param height  目标高度
	 * @return
	 */
	public static Bitmap loadBitmap(byte[] bytes, int width, int height){
		Options opt=new Options();
		//是否仅仅加载图片的边界属性
		opt.inJustDecodeBounds=true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);
		//获取图片的原始宽度和高度
		int w=opt.outWidth/width;
		int h=opt.outHeight/height;
		int scale=w>h ? w : h;
		//设置图片的压缩比例
		opt.inSampleSize=scale;
		opt.inJustDecodeBounds=false;
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);
	}	
}




