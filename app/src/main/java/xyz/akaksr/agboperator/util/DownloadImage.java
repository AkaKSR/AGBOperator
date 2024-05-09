package xyz.akaksr.agboperator.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
    private ImageView imageView;
    private String fileName;

    public DownloadImage(ImageView imageView, String fileName) {
        this.imageView = imageView;
        this.fileName = fileName;
    }

    protected Bitmap doInBackground(String... urls) {
        String url = urls[0];
        Bitmap bitmap = null;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();

            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    protected void onPostExecute(Bitmap result) {
        File dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        String filePath = dirPath.toString() + "/AGBOperator";

        if (result != null) {
            imageView.setImageBitmap(result);
            try {
                FileUtil.writeFile(filePath + fileName, result);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}