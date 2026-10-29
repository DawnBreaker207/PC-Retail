package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;
import org.dawn.redis.storage.RedisStore;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class IncrCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        try {
            String key = args.get(1);
            long newVal = RedisStore.incr(key);
            out.write((":" + newVal + "\r\n").getBytes(StandardCharsets.UTF_8));
        } catch (NumberFormatException e) {
            out.write("-ERR value is not an integer or out of range\r\n".getBytes(StandardCharsets.UTF_8));
        }
    }
}
