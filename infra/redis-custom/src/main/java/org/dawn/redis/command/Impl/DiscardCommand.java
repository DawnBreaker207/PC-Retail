package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DiscardCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        if (!session.isInTransaction()) {
            out.write("-ERR DISCARD without MULTI\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }
        session.clearCommandQueue();
        session.setInTransaction(false);
        session.unwatch();
        out.write("+OK\r\n".getBytes(StandardCharsets.UTF_8));
    }
}
