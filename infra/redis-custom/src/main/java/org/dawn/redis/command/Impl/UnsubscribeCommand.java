package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;
import org.dawn.redis.server.PubSubManager;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class UnsubscribeCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        for (int i = 1; i < args.size(); i++) {
            String channel = args.get(i);
            session.unsubscribe(channel);
            PubSubManager.unsubscribe(channel, session);
            int remaining = session.getSubscribedChannelCount();
            String response = "*3\r\n"
                    + "$11\r\nunsubscribe\r\n"
                    + "$" + channel.getBytes(StandardCharsets.UTF_8).length + "\r\n" + channel + "\r\n"
                    + ":" + remaining + "\r\n";
            out.write(response.getBytes(StandardCharsets.UTF_8));
        }
        out.flush();
    }
}
