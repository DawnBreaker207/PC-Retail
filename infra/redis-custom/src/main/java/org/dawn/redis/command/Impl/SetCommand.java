package org.dawn.redis.command.Impl;

import org.dawn.redis.aof.AofManager;
import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;
import org.dawn.redis.storage.RedisStore;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SetCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        String key = args.get(1);
        String value = args.get(2);
        if (args.size() >= 5 && args.get(3).equalsIgnoreCase("PX")) {
            long ttlMs = Long.parseLong(args.get(4));
            RedisStore.setWithExpiry(key, value, ttlMs);
        } else {
            RedisStore.set(key, value);
        }
        AofManager.append(args);
        out.write("+OK\r\n".getBytes(StandardCharsets.UTF_8));
    }
}
