package org.dawn.redis.server;

public class ServerConfig {
    private static String dir = "";
    private static String dbFileName = "";
    private static String appendOnly = "no";
    private static String appendDirName = "appendonlydir";
    private static String appendFileName = "appendonly.aof";
    private static String appendFsync = "everysec";


    public static void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("--dir".equalsIgnoreCase(args[i])) {
                ServerConfig.setDir(args[++i]);
            } else if ("--dbfilename".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                ServerConfig.setDbFileName(args[++i]);
            } else if ("--appendonly".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                ServerConfig.setAppendOnly(args[++i]);
            } else if ("--appenddirname".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                ServerConfig.setAppendDirName(args[++i]);
            } else if ("--appendfilename".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                ServerConfig.setAppendFileName(args[++i]);
            } else if ("--appendfsync".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                ServerConfig.setAppendFsync(args[++i]);
            }
        }
    }

    public static String getDir() {
        return dir;
    }

    public static void setDir(String dir) {
        ServerConfig.dir = dir;
    }

    public static String getDbFileName() {
        return dbFileName;
    }

    public static void setDbFileName(String dbFileName) {
        ServerConfig.dbFileName = dbFileName;
    }

    public static String getAppendOnly() {
        return appendOnly;
    }

    public static void setAppendOnly(String appendOnly) {
        ServerConfig.appendOnly = appendOnly;
    }

    public static String getAppendDirName() {
        return appendDirName;
    }

    public static void setAppendDirName(String appendDirName) {
        ServerConfig.appendDirName = appendDirName;
    }

    public static String getAppendFileName() {
        return appendFileName;
    }

    public static void setAppendFileName(String appendFileName) {
        ServerConfig.appendFileName = appendFileName;
    }

    public static String getAppendFsync() {
        return appendFsync;
    }

    public static void setAppendFsync(String appendFsync) {
        ServerConfig.appendFsync = appendFsync;
    }
}
