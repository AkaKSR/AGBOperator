package xyz.akaksr.agboperator.prop;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import xyz.akaksr.agboperator.util.ByteUtil;

public class Constants extends ByteUtil {
    public static Map<Integer, String> MBC_TYPES = new HashMap<>();

    public static Map<Integer, Map<String, Object>> ROM_TYPES = new HashMap<>();

    public static Map<Integer, Map<String, Object>> RAM_TYPES = new HashMap<>();

    public static final byte[] TRIGGER_CARTRIDGE_INFO = new byte[]{
            0x04, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00
    };

    public static final byte[] TRIGGER_ACK_BYTES = new byte[]{
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00
    };

    public Constants() {
        MBC_TYPES = setMBC_TYPES();
        ROM_TYPES = setROM_TYPES();
        RAM_TYPES = setRAM_TYPES();
    }

    public Map<String, Object> getData() {
        Map<String, Object> resultMap = new HashMap<>();

        resultMap.put("MBC_TYPES", MBC_TYPES);
        resultMap.put("ROM_TYPES", ROM_TYPES);
        resultMap.put("RAM_TYPES", RAM_TYPES);

        return resultMap;
    }

    private Map<Integer, String> setMBC_TYPES() {
        Map<Integer, String> resultMap = new HashMap<>();
        resultMap.put(0x00, "ROM ONLY");
        resultMap.put(0x01, "MBC1");
        resultMap.put(0x02, "MBC1+RAM");
        resultMap.put(0x03, "MBC1+RAM+BATTERY");
        resultMap.put(0x05, "MBC2");
        resultMap.put(0x06, "MBC2+BATTERY");
        resultMap.put(0x08, "ROM+RAM 1");
        resultMap.put(0x09, "ROM+RAM+BATTERY 1");
        resultMap.put(0x0B, "MMM01");
        resultMap.put(0x0C, "MMM01+RAM");
        resultMap.put(0x0D, "MMM01+RAM+BATTERY");
        resultMap.put(0x0F, "MBC3+TIMER+BATTERY");
        resultMap.put(0x10, "MBC3+TIMER+RAM+BATTERY 2");
        resultMap.put(0x11, "MBC3");
        resultMap.put(0x12, "MBC3+RAM 2");
        resultMap.put(0x13, "MBC3+RAM+BATTERY 2");
        resultMap.put(0x19, "MBC5");
        resultMap.put(0x1A, "MBC5+RAM");
        resultMap.put(0x1B, "MBC5+RAM+BATTERY");
        resultMap.put(0x1C, "MBC5+RUMBLE");
        resultMap.put(0x1D, "MBC5+RUMBLE+RAM");
        resultMap.put(0x1E, "MBC5+RUMBLE+RAM+BATTERY");
        resultMap.put(0x20, "MBC6");
        resultMap.put(0x22, "MBC7+SENSOR+RUMBLE+RAM+BATTERY");
        resultMap.put(0xFC, "POCKET CAMERA");
        resultMap.put(0xFD, "BANDAI TAMA5");
        resultMap.put(0xFE, "HuC3");
        resultMap.put(0xFF, "HuC1+RAM+BATTERY");

        return resultMap;
    }

    private Map<Integer, Map<String, Object>> setROM_TYPES() {
        Map<Integer, Map<String, Object>> resultMap = new HashMap<>();

        int _size = 32;
        int[] code = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };

        for (int j : code) {
            Map<String, Object> ROM_TYPE = new HashMap<>();
            if (_size <= 1024) {
                ROM_TYPE.put("ROM_size_info", _size + " KiB");
                ROM_TYPE.put("num_rom_banks", _size / 16);
            } else {
                ROM_TYPE.put("ROM_size_info", _size + " MiB");
                ROM_TYPE.put("num_rom_banks", _size / 16);
            }
            _size = _size * 2;

            resultMap.put(j, ROM_TYPE);
        }

        return resultMap;
    }

    private Map<Integer, Map<String, Object>> setRAM_TYPES() {
        Map<Integer, Map<String, Object>> resultMap = new HashMap<>();

        int[] code = { 0x00, 0x02, 0x03, 0x04, 0x05 };
        String[] srams = { "0", "8 KiB", "32 KiB", "128 KiB", "64 KiB" };
        String[] infos = { "No RAM", "1 bank", "4 banks of 8 KiB each", "16 banks of 8 KiB each", "8 banks of 8 KiB each" };

        for (int i = 0; i < code.length; i++) {
            Map<String, Object> RAM_TYPE = new HashMap<>();

            RAM_TYPE.put("SRAM_size_info", srams[i]);
            RAM_TYPE.put("info", infos[i]);

            resultMap.put(code[i], RAM_TYPE);
        }

        return resultMap;
    }

    public static byte[] TRIGGER_ROM_READ(Map<String, Object> CARTRIDGE_INFO_DUMP) {
        byte[] tmpArray = BigInteger.valueOf(Long.parseLong(Objects.requireNonNull(CARTRIDGE_INFO_DUMP.get("ROM_size")).toString())).toByteArray();
        byte[] romSizeByteArray = new byte[tmpArray.length];
        byte[] trigger = new byte[]{0x00, 0x00};
//        byte[] trigger = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] triggerRomRead = new byte[romSizeByteArray.length + trigger.length];

        for (int i = tmpArray.length - 1, j = 0; i >= 0; i--, j++) {
            romSizeByteArray[j] = tmpArray[i];
        }

        System.arraycopy(trigger, 0, triggerRomRead, 0, trigger.length);
        System.arraycopy(romSizeByteArray, 0, triggerRomRead, trigger.length, romSizeByteArray.length);

        int padLength = 60 - triggerRomRead.length;
        byte[] newTriggerRomRead = new byte[triggerRomRead.length + padLength];
        System.arraycopy(triggerRomRead, 0, newTriggerRomRead, 0, triggerRomRead.length);

        byte[] padding = new byte[padLength];
        Arrays.fill(padding, (byte) 0x00);
        System.arraycopy(padding, 0, newTriggerRomRead, triggerRomRead.length, padding.length);

        return add_crc32(newTriggerRomRead);
    }
}
