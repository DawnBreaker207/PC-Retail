package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;
import org.dawn.redis.storage.RedisStore;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BLPopCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        String key = args.get(1);
        double timeoutSec = Double.parseDouble(args.get(2));
        long timeoutMs = (long) (timeoutSec * 1000);
        String val = RedisStore.blPop(key, timeoutMs);
        if (val == null) {
            out.write("*-1\r\n".getBytes(StandardCharsets.UTF_8));
        } else {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] valBytes = val.getBytes(StandardCharsets.UTF_8);
            String resp =
                    "*2\r\n"
                            + "$" + keyBytes.length + "\r\n" + key + "\r\n"
                            + "$" + valBytes.length + "\r\n" + val + "\r\n";
            out.write((resp).getBytes(StandardCharsets.UTF_8));
        }
    }
}
