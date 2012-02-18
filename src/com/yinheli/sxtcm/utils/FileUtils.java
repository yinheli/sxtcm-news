package com.yinheli.sxtcm.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.os.Environment;

import com.yinheli.sxtcm.Constants;

/**
 * 文件操作工具类
 * 
 * @author yinheli <yinheli@gmail.com>
 *
 */
public class FileUtils {
	
	/**
	 * 取得 SD 卡的存储目录
	 * 
	 * @return
	 */
	public static File getSdCard() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return new File(android.os.Environment.getExternalStorageDirectory().getPath(), Constants.CACHE_FILE_DIR);
		}
		return null;
	}
	
	/**
	 * 取得当前应用的存储位置
	 * 
	 * @param context
	 * @return
	 */
	public static File getAppFileDir(Context context) {
		return new File(context.getFilesDir().getAbsolutePath(), Constants.CACHE_FILE_DIR);
	}
	
	/**
	 * 保存文件
	 * 
	 * @param context
	 * @param file
	 * @return 保存后的文件
	 * @throws IOException
	 */
	public static File saveFile(Context context, File file) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		return saveFile(context, bis, file.getName());
	}
	
	/**
	 * @param context
	 * @param data
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static File saveFile(Context context, byte[] data, String fileName) throws IOException {
		File targetFile = new File(getTargetDir(context), fileName);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile));
		bos.write(data);
		bos.flush();
		bos.close();
		return targetFile;
	}
	
	/**
	 * @param context
	 * @param is
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static File saveFile(Context context, InputStream is, String fileName) throws IOException {
		File targetFile = new File(getTargetDir(context), fileName);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile));
		byte[] buf = new byte[1024];
		int len = -1;
		while ((len = is.read(buf)) != -1) {
			bos.write(buf, 0, len);
		}
		bos.flush();
		bos.close();
		return targetFile;
	}
	
	/**
	 * 取得目标文件夹位置
	 * SD目录不可用的时候使用应用程序的 file 文件夹
	 * 
	 * @param context
	 * @return
	 */
	private static File getTargetDir(Context context) {
		File targetDir = getSdCard();
		if (targetDir == null) {
			targetDir = getAppFileDir(context);
		}
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}
		return targetDir;
	}

}
