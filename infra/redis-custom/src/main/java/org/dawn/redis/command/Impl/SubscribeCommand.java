package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;
import org.dawn.redis.server.PubSubManager;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubscribeCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        if (args.size() < 2) {
            out.write("-ERR wrong number of arguments for 'subscribe' command\r\n".getBytes(StandardCharsets.UTF_8));
            out.flush();
            return;
        }

        for (int i = 1; i < args.size(); i++) {
            String channel = args.get(i);
            session.subscribe(channel);
            int count = session.getSubscribedChannelCount();
            PubSubManager.subscribe(channel, session);
            String response = "*3\r\n"
                    + "$9\r\nsubscribe\r\n"
                    + "$" + channel.getBytes(StandardCharsets.UTF_8).length + "\r\n" + channel + "\r\n"
                    + ":" + count + "\r\n";

            out.write(response.getBytes(StandardCharsets.UTF_8));
        }
        out.flush();
    }
}
