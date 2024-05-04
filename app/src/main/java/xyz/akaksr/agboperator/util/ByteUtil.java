package xyz.akaksr.agboperator.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import xyz.akaksr.agboperator.crc.AlgoParams;
import xyz.akaksr.agboperator.crc.Crc32;
import xyz.akaksr.agboperator.crc.CrcCalculator;

public class ByteUtil {
    public static byte[] add_crc32(byte[] data) {
        AlgoParams params = Crc32.Crc32Mpeg2;

        CrcCalculator calculator = new CrcCalculator(params);
        long crc_long = calculator.Calc(data, 0, data.length);
        byte[] bytesCrc = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(crc_long).array();
        bytesCrc = Arrays.copyOf(bytesCrc, bytesCrc.length - 4);
        byte[] result = new byte[bytesCrc.length + data.length];
        System.arraycopy(data, 0, result, 0, data.length);
        System.arraycopy(bytesCrc, 0, result, data.length, bytesCrc.length);

        return result;
    }

    public int from_bytes(byte[] data, int startPos, int endPos, boolean littleEndian) {
        byte[] tmpArray = Arrays.copyOfRange(data, startPos, endPos);

        byte[] byteArray = new byte[tmpArray.length];
        if (littleEndian) {
            for (int i = tmpArray.length - 1, j = 0; i >= 0; i--, j++) {
                byteArray[j] = tmpArray[i];
            }
        } else {
            byteArray = tmpArray;
        }

        int result = 0;
        for (byte b : byteArray) {
            result = (result << 8) | (b & 0xff);
        }

        System.out.println("Result: " + result);

        return result;
    }

    public String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static byte[] mergeArrays(byte[]... arrays) {
        int totalLength = 0;

        // 모든 배열의 길이를 합산
        for (byte[] array : arrays) {
            totalLength += array.length;
        }

        byte[] mergedArray = new byte[totalLength];
        int index = 0;

        // 모든 배열을 복사하여 합침
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, mergedArray, index, array.length);
            index += array.length;
        }

        return mergedArray;
    }
}
