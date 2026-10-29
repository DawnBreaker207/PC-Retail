package org.dawn.redis.rdb;

import org.dawn.redis.storage.RedisStore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RdbReader {
    public static void load(String dir, String dbFileName) {
        if (dir == null || dbFileName == null || dir.isEmpty() || dbFileName.isEmpty()) return;

        File file = new File(dir, dbFileName);
        if (!file.exists() || !file.isFile()) {
            return;
        }

        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            byte[] header = in.readNBytes(9);
            if (header.length < 9) return;
            Long expireAtMs = null;
            int b;
            while ((b = in.read()) != -1) {
                if (b == 0xFF) {
                    break;
                } else if (b == 0xFA) {
                    readString(in);
                    readString(in);
                } else if (b == 0xFB) {
                    readLength(in);
                    readLength(in);
                } else if (b == 0xFC) {
                    byte[] bytes = in.readNBytes(8);
                    expireAtMs = (bytes[0] & 0xFFL)
                            | ((bytes[1] & 0xFFL) << 8)
                            | ((bytes[2] & 0xFFL) << 16)
                            | ((bytes[3] & 0xFFL) << 24)
                            | ((bytes[4] & 0xFFL) << 32)
                            | ((bytes[5] & 0xFFL) << 40)
                            | ((bytes[6] & 0xFFL) << 48)
                            | ((bytes[7] & 0xFFL) << 56);
                } else if (b == 0xFD) {
                    byte[] bytes = in.readNBytes(4);
                    long sec = (bytes[0] & 0xFFL)
                            | ((bytes[1] & 0xFFL) << 8)
                            | ((bytes[2] & 0xFFL) << 16)
                            | ((bytes[3] & 0xFFL) << 24);
                    expireAtMs = sec * 1000L;
                } else if (b == 0xFE) {
                    readLength(in);
                } else if (b == 0x00) {
                    String key = readString(in);
                    String value = readString(in);
                    if (key != null && value != null) {
                        if (expireAtMs != null) {
                            if (expireAtMs > System.currentTimeMillis()) {
                                RedisStore.setWithAbsoluteExpiry(key, value, expireAtMs);
                            }
                        } else {
                            RedisStore.set(key, value);
                        }
                    }
                    expireAtMs = null;
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading RDB: " + e.getMessage());
        }
    }

    private static int readLength(InputStream in) {
        try {
            int first = in.read();
            if (first == -1) return 0;
            int flag = (first & 0xC0) >> 6;
            if (flag == 0) {
                return first & 0X3F;
            } else if (flag == 1) {
                return ((first & 0x3F) << 8) | in.read();
            } else if (flag == 2) {
                byte[] b = in.readNBytes(4);
                return ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | (b[3] & 0xFF);
            }
            return 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String readString(InputStream in) {
        try {
            int first = in.read();
            if (first == -1) return null;
            int flag = (first & 0xC0) >> 6;
            if (flag == 0) {
                int len = first & 0x3F;
                byte[] data = in.readNBytes(len);
                return new String(data, StandardCharsets.UTF_8);
            } else if (flag == 1) {
                int len = ((first & 0x3F) << 8) | in.read();
                byte[] data = in.readNBytes(len);
                return new String(data, StandardCharsets.UTF_8);
            } else if (flag == 2) {
                byte[] b = in.readNBytes(4);
                int len = ((b[0] & 0xFF) << 24)
                        | ((b[1] & 0xFF) << 16)
                        | ((b[2] & 0xFF) << 8)
                        | (b[3] & 0xFF);
                byte[] data = in.readNBytes(len);
                return new String(data, StandardCharsets.UTF_8);
            } else {
                int type = first & 0x3F;
                if (type == 0) {
                    return String.valueOf(in.read());
                } else if (type == 1) {
                    byte[] b = in.readNBytes(2);
                    int val = (b[0] & 0xFF) | ((b[1] & 0xFF) << 8);
                    return String.valueOf(val);
                } else if (type == 2) {
                    byte[] b = in.readNBytes(4);
                    int val = (b[0] & 0xFF)
                            | ((b[1] & 0xFF) << 8)
                            | ((b[2] & 0xFF) << 16)
                            | ((b[3] & 0xFF << 24));
                    return String.valueOf(val);
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
