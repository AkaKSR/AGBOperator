package xyz.akaksr.agboperator.usb;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsbCommunicationManager {

    private static final String TAG = "UsbCommunicationManager";
    private static final int TIMEOUT = 1000;
    private static final int BUFFER_SIZE = 64; // 데이터 버퍼 크기 조절

    private final UsbManager usbManager;
    private final UsbDevice usbDevice;
    private UsbDeviceConnection usbConnection;
    private UsbInterface usbInterface;
    private UsbEndpoint inputEndpoint;
    private UsbEndpoint outputEndpoint;

    public UsbCommunicationManager(UsbManager manager, UsbDevice device) {
        this.usbManager = manager;
        this.usbDevice = device;
    }

    public boolean connectToDevice(int interfaceClass) {
        if (usbDevice == null) {
            Log.e(TAG, "USB device not found.");
            return false;
        }

        for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
            UsbInterface tmpInterface = usbDevice.getInterface(i);

            if (tmpInterface.getInterfaceClass() == interfaceClass) {
                usbInterface = tmpInterface;
            }
        }
        if (usbInterface == null) {
            Log.e(TAG, "Interface not found.");
            return false;
        }

        usbConnection = usbManager.openDevice(usbDevice);
        if (usbConnection == null) {
            Log.e(TAG, "Failed to open USB device connection.");
            return false;
        }

        if (!usbConnection.claimInterface(usbInterface, true)) {
            Log.e(TAG, "Failed to claim interface.");
            return false;
        }

        // 입력 및 출력 엔드포인트 확인
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint endpoint = usbInterface.getEndpoint(i);
            if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                    inputEndpoint = endpoint;
                } else if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                    outputEndpoint = endpoint;
                }
            }
        }

        if (inputEndpoint == null || outputEndpoint == null) {
            Log.e(TAG, "Failed to find input or output endpoint.");
            return false;
        }

        return true;
    }

    public void sendData(byte[] data) {
        if (usbConnection != null && outputEndpoint != null) {
            byte[] bytes;
            bytes = data;
            int bytesSent = usbConnection.bulkTransfer(outputEndpoint, bytes, bytes.length, TIMEOUT);
            Log.d(TAG, "Sent " + bytesSent + " bytes");
        }
    }

    public Map<String, Object> receiveData(boolean ack) {
        Map<String, Object> resultMap = new HashMap<>();

        if (usbConnection != null && inputEndpoint != null) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesReceived = usbConnection.bulkTransfer(inputEndpoint, buffer, BUFFER_SIZE, TIMEOUT);

            if (bytesReceived > 0) {
                byte[] resultBuffer = new byte[bytesReceived];
                System.arraycopy(buffer, 0, resultBuffer, 0, bytesReceived);

//                resultMap.put("buffer", buffer);
                resultMap.put("buffer", resultBuffer);
                resultMap.put("length", bytesReceived);

                return resultMap;
            }
        }
        return null;
    }

    public void closeConnection() {
        if (usbConnection != null) {
            usbConnection.releaseInterface(usbInterface);
            usbConnection.close();
        }
    }
}
