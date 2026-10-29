package org.dawn.redis.aof;

import org.dawn.redis.command.Command;
import org.dawn.redis.command.CommandRegistry;
import org.dawn.redis.protocol.RespParser;
import org.dawn.redis.server.ClientSession;
import org.dawn.redis.server.ServerConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class AofManager {
    private static boolean isReplaying = false;


    public static void init() {
        try {
            if (ServerConfig.getAppendOnly().equalsIgnoreCase("yes")) {
                Path targetDir = getTargetDir();
                Files.createDirectories(targetDir);
                Path aofFile = targetDir.resolve(ServerConfig.getAppendFileName() + ".1.incr.aof");
                Path manifestFile = targetDir.resolve(ServerConfig.getAppendFileName() + ".manifest");
                if (!Files.exists(aofFile)) {
                    Files.createFile(aofFile);
                }
                if (!Files.exists(manifestFile)) {
                    String content = "file " + ServerConfig.getAppendFileName() + ".1.incr.aof seq 1 type i\n";
                    Files.writeString(manifestFile, content);
                }
                replay();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void append(List<String> command) {
        try {
            if (isReplaying == true) return;
            if (!ServerConfig.getAppendOnly().equalsIgnoreCase("yes")) {
                return;
            }
            if (command.isEmpty()) return;
            String cmdName = command.getFirst();

            if (cmdName.equalsIgnoreCase("GET")
                    || cmdName.equalsIgnoreCase("PING")
                    || cmdName.equalsIgnoreCase("ECHO")
                    || cmdName.equalsIgnoreCase("CONFIG")) return;


            Path targetDir = getTargetDir();
            Path manifestFile = targetDir.resolve(ServerConfig.getAppendFileName() + ".manifest");
            String incrFileName = null;

            for (String line : Files.readAllLines(manifestFile)) {
                if (line.contains("type i")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        incrFileName = parts[1];
                        break;
                    }
                }
            }

            if (incrFileName == null) return;

            StringBuilder sb = new StringBuilder();
            sb.append("*").append(command.size()).append("\r\n");
            for (String arg : command) {
                sb.append("$").append(arg.getBytes(StandardCharsets.UTF_8).length).append("\r\n");
                sb.append(arg).append("\r\n");
            }
            byte[] payload = sb.toString().getBytes(StandardCharsets.UTF_8);

            Path aofFile = targetDir.resolve(incrFileName);
            try (FileOutputStream fos = new FileOutputStream(aofFile.toFile(), true)) {
                fos.write(payload);
                fos.flush();


                if ("always".equalsIgnoreCase(ServerConfig.getAppendFsync())) {
                    fos.getFD().sync();
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path getTargetDir() {
        String baseDir = ServerConfig.getDir();
        String appendDirName = ServerConfig.getAppendDirName();
        if (baseDir != null && !baseDir.isBlank()) {
            return Paths.get(baseDir, appendDirName);
        } else {
            return Paths.get(appendDirName);
        }
    }

    private static void replay() {
        Path targetDir = getTargetDir();
        Path manifestFile = targetDir.resolve(ServerConfig.getAppendFileName() + ".manifest");
        if (!Files.exists(manifestFile)) return;

        try {
            String incrFileName = null;
            for (String line : Files.readAllLines(manifestFile)) {
                if (line.contains("type i")) {
                    String[] pars = line.trim().split("\\s+");
                    if (pars.length >= 2) {
                        incrFileName = pars[1];
                        break;
                    }
                }
            }
            if (incrFileName == null) return;
            Path aofFile = targetDir.resolve(incrFileName);
            if (!Files.exists(aofFile) || Files.size(aofFile) == 0) return;

            isReplaying = true;
            try (BufferedReader reader = Files.newBufferedReader(aofFile, StandardCharsets.UTF_8)) {
                OutputStream dummyOut = OutputStream.nullOutputStream();
                ClientSession dummySession = new ClientSession();

                List<String> command;
                while ((command = RespParser.parseCommand(reader)) != null) {
                    if (command.isEmpty()) continue;
                    String cmdName = command.getFirst();
                    Command cmd = CommandRegistry.get(cmdName);
                    if (cmd != null) {
                        cmd.execute(command, dummyOut, dummySession);
                    }
                }
            } finally {
                isReplaying = false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error replaying AOF: " + e.getMessage(), e);
        }
    }
}
