package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;
import org.dawn.redis.storage.RedisStore;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LPushCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        String key = args.get(1);
        List<String> values = args.subList(2, args.size());
        int newSize = RedisStore.lPush(key, values);
        out.write((":" + newSize + "\r\n").getBytes(StandardCharsets.UTF_8));
    }
}
