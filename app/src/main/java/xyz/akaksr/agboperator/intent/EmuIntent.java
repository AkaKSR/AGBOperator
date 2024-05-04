package xyz.akaksr.agboperator.intent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EmuIntent {

    public static void startIntent(Context context, String romPath) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName("com.retroarch.aarch64", "com.retroarch.browser.retroactivity.RetroActivityFuture"));
            intent.setAction("com.retroarch.aarch64/.browser.retroactivity.RetroActivityFuture");
            intent.putExtra("ROM", romPath);
            intent.putExtra("LIBRETRO", "/data/data/com.retroarch.aarch64/cores/mgba_libretro_android.so");
            intent.putExtra("CONFIGFILE", "/storage/emulated/0/Android/data/com.retroarch.aarch64/files/retroarch.cfg");
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            context.startActivity(intent);
        } catch (Exception e) {
            Log.d("RetroArchDebugError", e.getMessage());
        }
    }
}
