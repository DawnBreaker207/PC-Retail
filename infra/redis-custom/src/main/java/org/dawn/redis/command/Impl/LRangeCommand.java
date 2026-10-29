package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;
import org.dawn.redis.storage.RedisStore;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LRangeCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        String key = args.get(1);
        int start = Integer.parseInt(args.get(2));
        int stop = Integer.parseInt(args.get(3));

        List<String> values = RedisStore.lRange(key, start, stop);

        StringBuilder sb = new StringBuilder();
        sb.append("*").append(values.size()).append("\r\n");
        for (String value : values) {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            sb.append("$").append(bytes.length).append("\r\n").append(value).append("\r\n");
        }
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
