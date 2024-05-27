package xyz.akaksr.agboperator.usb;

import android.app.PendingIntent;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import xyz.akaksr.agboperator.R;
import xyz.akaksr.agboperator.prop.Constants;
import xyz.akaksr.agboperator.util.ByteUtil;
import xyz.akaksr.agboperator.util.FileUtil;

public class GBODevice extends ByteUtil {
    UsbManager usbManager;
    UsbDevice usbDevice;
    UsbCommunicationManager usbComm;

    PendingIntent permissionIntent;

    Constants consts = new Constants();
    Map<Integer, String> MBC_TYPES = (Map<Integer, String>) consts.getData().get("MBC_TYPES");
    Map<Integer, String> ROM_TYPES = (Map<Integer, String>) consts.getData().get("ROM_TYPES");
    Map<Integer, String> RAM_TYPES = (Map<Integer, String>) consts.getData().get("RAM_TYPES");

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public GBODevice(UsbManager usbManager, PendingIntent permissionIntent) {
        this.usbManager = usbManager;
        this.permissionIntent = permissionIntent;

        usbDevice = getDevice();
    }

    public String dumpRom(Map<String, Object> CARTRIDGE_INFO_DUMP, JSONObject CARTRIDGE_INFO) {
        File dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        try {
            Log.i("AGBOperator", ":: FileName = " + CARTRIDGE_INFO.get("full_title"));
            Log.i("AGBOperator", ":: Title = " + CARTRIDGE_INFO.get("title"));
            Log.i("AGBOperator", ":: GameBoy Color Support = " + CARTRIDGE_INFO.get("CGB_support"));
            Log.i("AGBOperator", ":: Super GameBoy Support = " + CARTRIDGE_INFO.get("SGB_support"));
            Log.i("AGBOperator", ":: Carteidge Type = " + CARTRIDGE_INFO.get("cartridge_type"));
            Log.i("AGBOperator", ":: ROM Size = " + CARTRIDGE_INFO.get("ROM_size"));
            Log.i("AGBOperator", ":: RAM Size = " + CARTRIDGE_INFO.get("RAM_size"));
            Log.i("AGBOperator", ":: Region = " + CARTRIDGE_INFO.get("destination"));
            Log.i("AGBOperator", ":: Version = " + CARTRIDGE_INFO.get("ROM_version"));
            Log.i("AGBOperator", ":: header = " + CARTRIDGE_INFO.get("header_checksum"));
            Log.i("AGBOperator", ":: global = " + CARTRIDGE_INFO.get("global_checksum"));

            String filePath = dirPath.toString() + "/AGBOperator/";

            byte[] romData = readRom(usbComm, CARTRIDGE_INFO_DUMP);

            Log.i("AGBOperator", ":: romData length = " + romData.length);

            Log.i("AGBOperator", ":: filePath = " + filePath);

            FileUtil.writeFile(filePath + "cache/" + CARTRIDGE_INFO.get("full_title"), romData);

            return filePath + CARTRIDGE_INFO.get("full_title");
        } catch (IOException e) {
            Log.e("getRomsInfo - IOException", e.getMessage());
            throw new RuntimeException(e);
        } catch (JSONException e) {
            Log.e("getRomsInfo - JSONException", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public UsbDevice getDevice() {

        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        UsbDevice device = null;

        for (String key : deviceList.keySet()) {
            device = deviceList.get(key);
            assert device != null;
            int _vid = device.getVendorId();
            int _pid = device.getProductId();

            if (_vid == 0x16d0 && _pid == 0x123d) {
                return device;
            }
        }

        return device;
    }

    public boolean getPermission() throws InterruptedException {
        boolean usbPermission = usbManager.hasPermission(usbDevice);

        if (!usbPermission) {
            usbManager.requestPermission(usbDevice, permissionIntent);
        }

        while(!usbManager.hasPermission(usbDevice)) {
            Thread.sleep(100);
        }

        return usbPermission;
    }

    public UsbCommunicationManager connectDevice(int interfaceClass) {
        usbComm = new UsbCommunicationManager(usbManager, usbDevice);

        usbComm.connectToDevice(interfaceClass);

        return usbComm;
    }

    public UsbInterface getInterface(UsbDevice device, int interfaceClass) {
        UsbInterface usbInterface = null;

        for (int i = 0; i < device.getInterfaceCount(); i++) {
            usbInterface = device.getInterface(i);

            if (usbInterface.getInterfaceClass() == interfaceClass) {
                return usbInterface;
            }
        }

        return usbInterface;
    }

    public UsbEndpoint[] getEndpoint(UsbInterface usbInterface) {
        UsbEndpoint[] usbEndpoint = new UsbEndpoint[usbInterface.getEndpointCount()];

        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            usbEndpoint[i] = usbInterface.getEndpoint(i);
        }

        return usbEndpoint;
    }

    public Map<String, Object> getCartridgeInfo(UsbCommunicationManager usbComm) {
        // SEND TRIGGER TYPE
        usbComm.sendData(add_crc32(Constants.TRIGGER_CARTRIDGE_INFO));

        // Read Device
        List<Map<String, Object>> readData = new ArrayList<>();

        boolean dFlag = true;
        while (dFlag) {
            Map<String, Object> tmp = usbComm.receiveData(true);
            if (tmp == null) {
                dFlag = false;
            } else {
                readData.add(tmp);
            }
        }

        // Burn the ACK
        readData.subList(0, 2).clear();

        // Read Cartridge
        byte[] cardInfo = (byte[]) readData.get(0).get("buffer");

        Map<String, Object> CARTRIDGE_INFO = new HashMap<>();
        // GB/GBC
        assert cardInfo != null;
        if (cardInfo[2] == 0x20) {
            CARTRIDGE_INFO.put("cartridge_type", "GB/GBC");
            CARTRIDGE_INFO.put("ROM_size", from_bytes(cardInfo, 5, 8, true));
            CARTRIDGE_INFO.put("RAM_size", from_bytes(cardInfo, 9, 12, true));
            CARTRIDGE_INFO.put("title_first_letter", (char) cardInfo[13]);
            CARTRIDGE_INFO.put("MBC_type", MBC_TYPES.get((int) cardInfo[14]));
            CARTRIDGE_INFO.put("ROM_type", ROM_TYPES.get((int) cardInfo[15]));
            CARTRIDGE_INFO.put("RAM_type", RAM_TYPES.get((int) cardInfo[16]));
            CARTRIDGE_INFO.put("headers_checksum", cardInfo[17]);
            CARTRIDGE_INFO.put("global_checksum", Arrays.copyOfRange(cardInfo, 18, 20));
        }

        return CARTRIDGE_INFO;
    }

    public String getEpilogueId(Map<String, Object> CARTRIDGE_INFO) {
        String title_first_letter = Objects.requireNonNull(CARTRIDGE_INFO.get("title_first_letter")).toString();
        String headers_checksum = String.format("%02X", CARTRIDGE_INFO.get("headers_checksum")).toUpperCase();
        String global_checksum = bytesToHex((byte[]) Objects.requireNonNull(CARTRIDGE_INFO.get("global_checksum")));

        return title_first_letter + headers_checksum + global_checksum;
    }

    public JSONObject getRomsInfo(InputStream inputStream) throws IOException, JSONException {
        JSONObject jobj = null;

        int size = inputStream.available();
        byte[] buffer = new byte[size];

        inputStream.read(buffer);
        inputStream.close();

        String json = new String(buffer, StandardCharsets.UTF_8);
        jobj = new JSONObject(json);

        return jobj;
    }

    public byte[] readRom(UsbCommunicationManager usbComm, Map<String, Object> CARTRIDGE_INFO_DUMP) throws JSONException {
        byte[] trigger = Constants.TRIGGER_ROM_READ(CARTRIDGE_INFO_DUMP);

        // SEND TRIGGER TYPE
        usbComm.sendData(trigger);

        // READ ACK DATA
        for (int i = 0; i < 2; i++) {
            usbComm.receiveData(true);
        }

        // SEND ACK COMMAND
        usbComm.sendData(Constants.TRIGGER_ACK_BYTES);

        List<byte[]> byteList = new ArrayList<>();
        for (int i = 0; i < 320; i++) {
            byte[] tmpBytes = readRomBulk(usbComm);

            if (tmpBytes.length == 0) {
                break;
            }

            byteList.add(tmpBytes);
        }

        int romSize = Integer.parseInt(Objects.requireNonNull(CARTRIDGE_INFO_DUMP.get("ROM_size")).toString());

        Log.i("AGBOperator", ":: romSize = " + romSize);

        byte[] romData = new byte[romSize];

        int index = 0;

        for (byte[] bytes : byteList) {
            System.arraycopy(bytes, 0, romData, index, bytes.length);
            index += bytes.length;
        }

        return romData;
    }

    private byte[] readRomBulk(UsbCommunicationManager usbComm) {
        List<Map<String, Object>> readData = new ArrayList<>();
        boolean dFlag = true;

        byte[] resultBytes = null;

        int byteLength = 0;

        while (dFlag) {
            Map<String, Object> tmp = usbComm.receiveData(true);

            if (tmp == null) {
                dFlag = false;
            } else {
                readData.add(tmp);
            }
        }

        // Burn the ACK
        readData.subList(0, 2).clear();

        for (Map<String, Object> tmp : readData) {
            byteLength += ((byte[]) Objects.requireNonNull(tmp.get("buffer"))).length;
        }

        resultBytes = new byte[byteLength];

        int nowLength = 0;
        for (Map<String, Object> tmp : readData) {
            byte[] tmpBuffer = (byte[]) tmp.get("buffer");
            assert tmpBuffer != null;
            System.arraycopy(tmpBuffer, 0, resultBytes, nowLength, tmpBuffer.length);

            nowLength += tmpBuffer.length;
        }

        // SEND ACK COMMAND
        usbComm.sendData(Constants.TRIGGER_ACK_BYTES);

        return resultBytes;
    }
}
