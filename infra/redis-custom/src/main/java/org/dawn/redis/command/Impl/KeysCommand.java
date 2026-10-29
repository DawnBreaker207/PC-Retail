package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;
import org.dawn.redis.storage.RedisStore;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class KeysCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        Set<String> keys = RedisStore.getKeys();
        if (keys.isEmpty()) {
            out.write("*0\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(keys.size()).append("\r\n");
        for (String key : keys) {
            byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
            sb.append("$").append(bytes.length).append("\r\n");
            sb.append(key).append("\r\n");
        }
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
