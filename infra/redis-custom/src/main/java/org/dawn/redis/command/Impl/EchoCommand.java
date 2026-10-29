package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EchoCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        String message = args.get(1);
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        out.write(("$" + bytes.length + "\r\n" + message + "\r\n").getBytes(StandardCharsets.UTF_8));
    }
}
