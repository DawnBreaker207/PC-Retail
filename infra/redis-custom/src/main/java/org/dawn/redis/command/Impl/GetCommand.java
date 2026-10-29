package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;
import org.dawn.redis.storage.RedisStore;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GetCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        String key = args.get(1);
        String value = RedisStore.get(key);
        if (value == null) {
            out.write("$-1\r\n".getBytes(StandardCharsets.UTF_8));
        } else {
            byte[] valBytes = value.getBytes(StandardCharsets.UTF_8);
            out.write(("$" + valBytes.length + "\r\n" + value + "\r\n").getBytes(StandardCharsets.UTF_8));
        }
    }
}
