package xyz.akaksr.agboperator.util;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    public static void writeFile(String filePath, byte[] data) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        DataOutputStream dos = new DataOutputStream(fos);

        dos.write(data);

        dos.flush();
        dos.close();
        fos.close();
    }

    public static void writeFile(String filePath, Bitmap bitmap) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

        fos.flush();
        fos.close();
    }

    public static boolean existsFile(String fileName) {
        File dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        String filePath = dirPath.toString() + "/AGBOperator" + fileName;

        return new File(filePath).exists();
    }

    public static void initDir() {
        File dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        String filePath = dirPath.toString() + "/AGBOperator/";

        File fileDir = new File(filePath);
        File cacheDir = new File(filePath + "cache");
        File boxartDir = new File(filePath + "image/boxart");

        if (!fileDir.exists()) {
            fileDir.mkdir();
        }

        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }

        if (!boxartDir.exists()) {
            boxartDir.mkdirs();
        }
    }
}
