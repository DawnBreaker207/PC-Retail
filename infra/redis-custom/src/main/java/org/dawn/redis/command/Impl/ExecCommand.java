package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.command.CommandRegistry;
import org.dawn.redis.server.ClientSession;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExecCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        if (!session.isInTransaction()) {
            out.write("-ERR EXEC without MULTI\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }

        if (session.isDirty()) {
            session.clearCommandQueue();
            session.setInTransaction(false);
            session.setDirty(false);
            session.unwatch();
            out.write("*-1\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }

        List<List<String>> queue = session.getCommandQueue();
        session.setInTransaction(false);

        if (queue.isEmpty()) {
            session.unwatch();
            out.write("*0\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        buffer.write(("*" + queue.size() + "\r\n").getBytes(StandardCharsets.UTF_8));
        for (List<String> cmdArgs : queue) {
            Command cmd = CommandRegistry.get(cmdArgs.getFirst());
            if (cmd != null) {
                try {
                    cmd.execute(cmdArgs, buffer, session);
                } catch (Exception e) {
                    buffer.write(("-ERR " + e.getMessage() + "\r\n").getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        out.write(buffer.toByteArray());
        session.clearCommandQueue();
        session.unwatch();
    }
}
