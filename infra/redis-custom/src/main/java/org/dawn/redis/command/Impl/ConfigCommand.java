package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;
import org.dawn.redis.server.ServerConfig;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ConfigCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        if (args.size() >= 3 && args.get(1).equalsIgnoreCase("GET")) {
            String param = args.get(2);
            String value = null;
            if ("dir".equalsIgnoreCase(param)) {
                value = ServerConfig.getDir();
            } else if ("dbfilename".equalsIgnoreCase(param)) {
                value = ServerConfig.getDbFileName();
            } else if ("appendonly".equalsIgnoreCase(param)) {
                value = ServerConfig.getAppendOnly();
            } else if ("appenddirname".equalsIgnoreCase(param)) {
                value = ServerConfig.getAppendDirName();
            } else if ("appendfilename".equalsIgnoreCase(param)) {
                value = ServerConfig.getAppendFileName();
            } else if ("appendfsync".equalsIgnoreCase(param)) {
                value = ServerConfig.getAppendFsync();
            }
            if (value != null) {
                String response = "*2\r\n"
                        + "$" + param.length() + "\r\n" + param + "\r\n"
                        + "$" + value.length() + "\r\n" + value + "\r\n";
                out.write(response.getBytes(StandardCharsets.UTF_8));
                return;
            } else {
                out.write("*0\r\n".getBytes(StandardCharsets.UTF_8));
                return;
            }
        }
    }
}
