package xyz.akaksr.agboperator.util;

import android.os.Environment;
import android.util.Log;

public class FileUtil {

    public static void storage() {
        boolean sFlag = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED;

        Log.i("AGBOperator", ":: sFlag = " + sFlag);
    }
}
