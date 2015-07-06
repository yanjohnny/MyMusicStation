package com.J.util;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

/***
 * ͼƬ������
 */
public class BitmapUtils {
	
	/**
	 * ͨ��һ��·��  ���س�һ��Bitmap����
	 * @param path Ŀ��·�� 
	 * @return  bitmap����  
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
	 * ����һ��ͼƬ
	 * @param targetFile  Ŀ��ͼƬ�ļ�
	 * @param bitmap  Ŀ��ͼƬ
	 * @throws Exception
	 */
	public static void save(File targetFile, Bitmap bitmap)throws Exception{
		if(!targetFile.getParentFile().exists()){
			targetFile.getParentFile().mkdirs();
		}
		if(!targetFile.exists()){
			targetFile.createNewFile();
		}
		//����
		FileOutputStream  fos=new FileOutputStream(targetFile);
		//ѹ��ͼƬ�������
		bitmap.compress(CompressFormat.JPEG, 100, fos);
	}
	
	
	/***
	 * ��ͼƬ����Ҫ�����ѹ�� ���ҷ���Bitmap
	 * @param bytes ͼƬ����Դ�ֽ�����
	 * @param width Ŀ����
	 * @param height  Ŀ��߶�
	 * @return
	 */
	public static Bitmap loadBitmap(byte[] bytes, int width, int height){
		Options opt=new Options();
		//�Ƿ��������ͼƬ�ı߽�����
		opt.inJustDecodeBounds=true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);
		//��ȡͼƬ��ԭʼ��Ⱥ͸߶�
		int w=opt.outWidth/width;
		int h=opt.outHeight/height;
		int scale=w>h ? w : h;
		//����ͼƬ��ѹ������
		opt.inSampleSize=scale;
		opt.inJustDecodeBounds=false;
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);
	}	
}




