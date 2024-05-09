package xyz.akaksr.agboperator.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.ImageView;

import java.io.File;

public class ImageUtil {

    public static void setImage(ImageView imageView, String filePath) {
        File dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File imageFile = new File(dirPath.toString() + "/AGBOperator" + filePath);

        if (imageFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

            imageView.setImageBitmap(bitmap);
        }
    }
}
