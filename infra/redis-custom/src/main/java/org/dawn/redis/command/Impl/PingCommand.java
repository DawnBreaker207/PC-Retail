package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PingCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        if (!session.isSubscribed()) {
            out.write("+PONG\r\n".getBytes(StandardCharsets.UTF_8));

        } else {
            String res = "*2\r\n"
                    + "$4\r\npong\r\n"
                    + "$0\r\n\r\n";
            out.write(res.getBytes(StandardCharsets.UTF_8));
        }

    }
}
