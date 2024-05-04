package xyz.akaksr.agboperator.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

public class UsbReceiver extends BroadcastReceiver {

    private static final String ACTION_USB_PERMISSION = "xyz.akaksr.agboperator.USB_PERMISSION";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        // 권한이 부여되었을 때의 동작
                        Toast.makeText(context, "USB 권한이 부여되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        // 권한이 거부되었을 때의 동작
                        Toast.makeText(context, "USB 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}
