package com.example.echomu.ovulationmaster.ovulation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * author : echoMu
 * date   : 2019/3/26
 * desc   :
 */
public class ImageUtil {
    public static final String DIR = Environment.getExternalStorageDirectory()
            .getPath() + File.separator + "pregnant" + File.separator;

    /**
     * 保存文件
     *
     * @param bmp
     * @param fileName
     * @return
     */
    public static boolean saveBitmapFile(Context mContext, Bitmap bmp,
                                         String fileName) {
        boolean falg = false;
        FileOutputStream localFileOutputStream = null;
        try {
            if (bmp == null) {
                return false;
            }
            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100,
                    localByteArrayOutputStream);
            File localFile = new File(DIR);
            if (!localFile.exists()) {
                localFile.mkdirs();
            }
            String str2 = DIR + fileName + ".jpeg";
            localFileOutputStream = new FileOutputStream(str2);
            localFileOutputStream.write(localByteArrayOutputStream
                    .toByteArray());
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(new File(str2));
            intent.setData(uri);
            mContext.sendBroadcast(intent);
            falg = true;
        } catch (Exception e) {
            falg = false;
        } finally {
            if (localFileOutputStream != null) {
                try {
                    localFileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return falg;
    }

}
