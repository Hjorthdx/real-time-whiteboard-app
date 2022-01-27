package com.whiteboardapp.common;


import android.graphics.Bitmap;
import android.os.Environment;

import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class AppUtils {

    public static byte[] getBuffer(Mat mat) {
        byte[] buffer = new byte[(int) (mat.total() * mat.channels())];
        mat.get(0, 0, buffer);
        return buffer;
    }

    // Saves file to storage -> emulated -> 0 -> Pictures -> saved_images
    public static void saveImageToExternalStorage(Bitmap finalBitmap) {
        String picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(picturesDir + "/saved_images");
        myDir.mkdirs();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS");
        String time = format.format(new Date());
        String fname = "img_" + time + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Saves file to storage -> emulated -> 0 -> Pictures -> saved_images
    public static void saveWithName(Bitmap finalBitmap, String name) {
        String picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(picturesDir + "/saved_images");
        myDir.mkdirs();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS");
//        String time = format.format(new Date());
        String fname = name + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
