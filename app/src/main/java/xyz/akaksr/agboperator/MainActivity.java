package xyz.akaksr.agboperator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import xyz.akaksr.agboperator.intent.EmuIntent;
import xyz.akaksr.agboperator.usb.GBODevice;
import xyz.akaksr.agboperator.usb.UsbCommunicationManager;

public class MainActivity extends AppCompatActivity {

    private static final String ACTION_USB_PERMISSION = "xyz.akaksr.agboperator.USB_PERMISSION";

    UsbManager usbManager;
    GBODevice device;
    String romPath;

    InputStream romInfo;
    Map<String, Object> CARTRIDGE_INFO_DUMP;
    JSONObject CARTRIDGE_INFO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);

        Context context = this;

        Button connect = findViewById(R.id.connect);
        Button dumpRom = findViewById(R.id.dumpRom);
        Button runGame = findViewById(R.id.runGame);
        ImageView boxArt = findViewById(R.id.boxArt);
        TextView cartridgeInfo = findViewById(R.id.cartridgeInfo);

        boxArt.setImageResource(R.drawable.unknown);

        JSONObject infoMap = new JSONObject();
        try {
            infoMap.put("title", "Unknown");
            infoMap.put("CGB_support", "Unknown");
            infoMap.put("SGB_support", "Unknown");
            infoMap.put("ROM_size", "Unknown");
            infoMap.put("RAM_size", "Unknown");
            infoMap.put("destination", "Unknown");
            setInfo(cartridgeInfo, infoMap);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                device = new GBODevice(usbManager, permissionIntent);
                try {
                    device.getPermission();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                UsbCommunicationManager usbComm = device.connectDevice(10);
                CARTRIDGE_INFO_DUMP = device.getCartridgeInfo(usbComm);
                String epilogueId = device.getEpilogueId(CARTRIDGE_INFO_DUMP);

                try {
                    romInfo = getAssets().open("gb_gbc_roms_info.json");
                    JSONObject ROMS_INFO = device.getRomsInfo(romInfo);
                    CARTRIDGE_INFO = (JSONObject) ROMS_INFO.get(epilogueId);

                    setInfo(cartridgeInfo, CARTRIDGE_INFO);
                } catch (IOException | JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        dumpRom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Rom Dump
                romPath = device.dumpRom(CARTRIDGE_INFO_DUMP, CARTRIDGE_INFO);
            }
        });

        runGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start RetroArch64
                EmuIntent.startIntent(context, romPath);
            }
        });
    }

    public void setInfo(TextView cartridgeInfo, JSONObject replaceMap) throws JSONException {
        String infoStr = getResources().getString(R.string.unknown);
        infoStr = infoStr.replace("{{TITLE}}", Objects.requireNonNull(replaceMap.get("title")).toString());
        infoStr = infoStr.replace("{{GBC_SUPPORT}}", Objects.requireNonNull(replaceMap.get("CGB_support")).toString());
        infoStr = infoStr.replace("{{SGB_SUPPORT}}", Objects.requireNonNull(replaceMap.get("SGB_support")).toString());
        infoStr = infoStr.replace("{{ROM_SIZE}}", Objects.requireNonNull(replaceMap.get("ROM_size")).toString());
        infoStr = infoStr.replace("{{RAM_SIZE}}", Objects.requireNonNull(replaceMap.get("RAM_size")).toString());
        infoStr = infoStr.replace("{{REGION}}", Objects.requireNonNull(replaceMap.get("destination")).toString());

        cartridgeInfo.setText(infoStr);
    }
}