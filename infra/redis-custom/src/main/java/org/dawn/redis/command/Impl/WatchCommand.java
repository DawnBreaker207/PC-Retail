package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class WatchCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        if (args.size() < 2) {
            out.write("-ERR wrong number of arguments for 'watch' command\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }
        if (session.isInTransaction()) {
            out.write("-ERR WATCH inside MULTI is not allowed\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }

        session.watch(args.subList(1, args.size()));
        out.write("+OK\r\n".getBytes(StandardCharsets.UTF_8));
    }
}
