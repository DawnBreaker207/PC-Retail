package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;
import org.dawn.redis.storage.RedisStore;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LPopCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        String key = args.get(1);
        if (args.size() == 2) {
            String pop = RedisStore.lPop(key);
            if (pop == null) {
                out.write(("$-1\r\n").getBytes(StandardCharsets.UTF_8));
            } else {
                byte[] bytes = pop.getBytes(StandardCharsets.UTF_8);
                out.write(("$" + bytes.length + "\r\n" + pop + "\r\n").getBytes(StandardCharsets.UTF_8));
            }
        }
        if (args.size() >= 3) {
            int count = Integer.parseInt(args.get(2));
            List<String> popped = RedisStore.lPop(key, count);
            if (popped == null) {
                out.write(("$-1\r\n").getBytes(StandardCharsets.UTF_8));
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("*").append(popped.size()).append("\r\n");
                for (String value : popped) {
                    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                    sb.append("$").append(bytes.length).append("\r\n").append(value).append("\r\n");
                }
                out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
